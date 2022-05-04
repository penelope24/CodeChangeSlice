package fy.slicing.track;

import com.google.common.collect.Sets;
import fy.progex.parse.PDGInfo;
import fy.slicing.result.CDGTrackResult;
import fy.slicing.result.CFGTrackResult;
import fy.slicing.result.DDGTrackResult;
import fy.slicing.solver.cfg.edit.*;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.*;
import java.util.stream.Collectors;

public class CFGTracker {

    PDGInfo pdgInfo;
    List<Integer> editedLines;
    DDGTrackResult<PDNode, DDEdge> ddgTrackResult;
    CDGTrackResult<PDNode> cdgTrackResult;
    CFGTrackResult<PDNode,DDEdge,CFNode,CFEdge> cfgTrackResult = new CFGTrackResult<>();
    boolean for_test = false;
    CFNode root;
    Set<CFNode> dataBindNodes;
    Set<CFNode> controlBindNodes;
    Set<CFNode> skeletonNodes;
    Set<PDNode> skeletonPDNodes;
    Set<CFNode> worklist;

    public CFGTracker(PDGInfo pdgInfo, List<Integer> editedLines, DDGTrackResult<PDNode, DDEdge> ddgTrackResult,
                      CDGTrackResult<PDNode> cdgTrackResult) {
        this.pdgInfo = pdgInfo;
        this.editedLines = editedLines;
        this.ddgTrackResult = ddgTrackResult;
        this.cdgTrackResult = cdgTrackResult;
        this.dataBindNodes = ddgTrackResult.getResDataNodes().stream()
                .map(pdgInfo::findCFNodeByDDNode)
                .collect(Collectors.toSet());
        this.controlBindNodes = cdgTrackResult.getControlBindingNodes().stream()
                .map(pdgInfo::findCFNodeByCDNode)
                .collect(Collectors.toSet());
        this.skeletonNodes = Sets.union(dataBindNodes, controlBindNodes);
        this.skeletonPDNodes = skeletonNodes.stream()
                .map(pdgInfo::findCDNode)
                .collect(Collectors.toSet());
        cfgTrackResult.addResDataNodes(ddgTrackResult.getResDataNodes());
        cfgTrackResult.addResDataFlowEdges(ddgTrackResult.getResDataFlowEdges());
        cfgTrackResult.addResControlNodes(ddgTrackResult.getResDataNodes().stream()
                .map(pdgInfo::findCFNodeByDDNode)
                .collect(Collectors.toSet()));
        cfgTrackResult.addResControlNodes(cdgTrackResult.getControlBindingNodes().stream()
                .map(pdgInfo::findCFNodeByCDNode)
                .collect(Collectors.toSet()));
        if (for_test) {
            this.root = pdgInfo.cfg.copyVertexSet().stream()
                    .findFirst().orElse(null);
            assert this.root != null;
            root.setProperty("root", true);
            worklist = pdgInfo.cfg.copyVertexSet().stream()
                    .filter(CFNode::isBranch)
                    .collect(Collectors.toSet());
            return;
        }
        // 离发生改变最近的root node
        this.root = pdgInfo.cfg.copyVertexSet().stream()
                .filter(cfNode -> cfNode.getType() == NodeType.ROOT)
                .filter(cfNode -> cfNode.getLineOfCode() < this.editedLines.get(0))
                .min(Comparator.comparing(cfNode -> Math.abs(cfNode.getLineOfCode() - this.editedLines.get(0))))
                .orElse(null);
        assert this.root != null;
        root.setProperty("root", true);
        worklist = skeletonNodes.stream()
                .filter(CFNode::isBranch)
                .collect(Collectors.toSet());
        this.root.setProperty("root", true);
        // start
        ddgTrackResult.getResDataNodes().stream()
                .filter(node -> node.getProperty("start") != null)
                .forEach(node -> {
                    pdgInfo.findCFNodeByDDNode(node).setProperty("start", true);
                });
        // data bind
        this.dataBindNodes.forEach(cfNode -> cfNode.setProperty("data_bind", true));
        // control bind
        this.controlBindNodes.forEach(cfNode -> cfNode.setProperty("control_bind", true));
    }

    public void track () {
        // parse
        Map<CFNode, FlowEditor> editorMap = new LinkedHashMap<>();
        for (CFNode cfNode : worklist) {
            switch (cfNode.getType()) {
                case SWITCH: {
                    FlowEditor editor = new SwitchNodeEditor(pdgInfo, skeletonNodes, cfgTrackResult, cfNode);
                    editor.setFor_test(for_test);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                case CASE: {
                    FlowEditor editor = new CaseNodeEditor(pdgInfo, skeletonNodes, cfgTrackResult, cfNode);
                    editor.setFor_test(for_test);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                case WHILE: {
                    FlowEditor editor = new WhileNodeFlowEditor(pdgInfo, skeletonNodes, cfgTrackResult, cfNode);
                    editor.setFor_test(for_test);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                case FOR: {
                    FlowEditor editor = new ForNodeFlowEditor(pdgInfo, skeletonNodes, cfgTrackResult, cfNode);
                    editor.setFor_test(for_test);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                case IF: {
                    FlowEditor editor = new IfNodeFlowEditor(pdgInfo, skeletonNodes, cfgTrackResult, cfNode);
                    editor.setFor_test(for_test);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                default:
                    throw new IllegalStateException("new branch node type");
            }
        }
        // edit
        for (CFNode cfNode : editorMap.keySet()) {
            FlowEditor editor = editorMap.get(cfNode);
            editor.edit();
        }
        RootNodeEditor rootNodeEditor = new RootNodeEditor(pdgInfo, skeletonNodes, cfgTrackResult, root);
        rootNodeEditor.parse();
        rootNodeEditor.edit();
    }
}
