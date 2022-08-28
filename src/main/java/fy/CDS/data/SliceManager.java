package fy.CDS.data;

import com.google.common.collect.Sets;
import fy.CDS.result.CDGTrackResult;
import fy.CDS.result.DDGTrackResult;
import fy.CDS.result.PaletteResult;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.*;
import org.eclipse.jgit.diff.Edit;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SliceManager {
    // input
    public PDGInfo pdgInfo;
    public CFNode entryNode;
    public List<PDNode> startNodes;
    public Edit.Type editType;
    public DDGTrackResult<PDNode, DDEdge> ddgTrackResult;
    public CDGTrackResult<PDNode> cdgTrackResult;
    // graph
    public ProgramDependeceGraph graph;
    public DataDependenceGraph ddg;
    public ControlDependenceGraph cdg;
    public ControlFlowGraph cfg;
    public String fileName;
    // parse
    public List<Integer> chLines;
    public Set<CFNode> startCFNodes;
    public Set<CFNode> exitNodes = new HashSet<>();
    public Set<CFNode> dataBindNodes = new HashSet<>();
    public Set<CFNode> controlBindNodes = new HashSet<>();
    public Set<CFNode> callsites = new HashSet<>();
    public Set<CFNode> skeletonNodes;
    public Set<PDNode> skeletonPDNodes;
    public Set<CFNode> worklist;
    public Set<CFNode> worklist_for_test;
    // result
    public Set<PDNode> resDataFlowNodes = new HashSet<>();
    public Set<Edge<PDNode, DDEdge>> resDataFlowEdges = new HashSet<>();
    public Set<CFNode> resControlFlowNodes = new HashSet<>();
    public Set<Edge<CFNode, CFEdge>> resControlFlowEdges = new HashSet<>();
    // check
    public boolean is_valid_track = true;


    public SliceManager(PDGInfo pdgInfo, CFNode entryNode, List<PDNode> startNodes, Edit.Type editType) {
        this.pdgInfo = pdgInfo;
        this.entryNode = entryNode;
        this.startNodes = startNodes;
        this.editType = editType;
        init();
    }

    private void init() {
        // graph
        this.graph = pdgInfo.pdg;
        this.ddg = this.graph.DDS;
        this.cdg = this.graph.CDS;
        this.cfg = this.graph.DDS.getCFG();
        this.fileName = graph.FILE_NAME.getName().replaceAll(".java", "");
        this.startCFNodes = startNodes.stream()
                .map(pdgInfo::findCFNodeByDDNode)
                .collect(Collectors.toSet());
        this.chLines = startNodes.stream()
                .map(PDNode::getLineOfCode)
                .collect(Collectors.toList());
        if (startCFNodes.isEmpty()) {
            is_valid_track = false;
        }
    }

    public void updateAfterDDGTrack(DDGTrackResult<PDNode, DDEdge> result) {
        this.ddgTrackResult = result;
        this.dataBindNodes = result.getResDataNodes().stream()
                .map(pdgInfo::findCFNodeByDDNode)
                .collect(Collectors.toSet());
        this.resDataFlowNodes.addAll(result.getResDataNodes());
        this.resDataFlowEdges.addAll(result.getResDataFlowEdges());
        this.resControlFlowNodes.addAll(
                result.getResDataNodes().stream()
                    .map(pdgInfo::findCFNodeByDDNode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
        );
    }

    public void updateAfterCDGTrack(CDGTrackResult<PDNode> result) {
        this.cdgTrackResult = result;
        this.controlBindNodes = result.getControlBindingNodes().stream()
                .map(pdgInfo::findCFNodeByCDNode)
                .collect(Collectors.toSet());
        this.resControlFlowNodes.addAll(
                result.getControlBindingNodes().stream()
                        .map(pdgInfo::findCFNodeByCDNode)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        );
    }

    public void updateBeforeCFGTrack() {
        this.skeletonNodes = Sets.union(this.dataBindNodes, this.controlBindNodes);
        this.skeletonPDNodes = this.skeletonNodes.stream()
                .map(pdgInfo::findCDNode)
                .collect(Collectors.toSet());
        this.worklist = skeletonNodes.stream()
                .filter(CFNode::isBranch)
                .collect(Collectors.toSet());
        this.worklist_for_test = pdgInfo.cfg.copyVertexSet().stream()
                .filter(CFNode::isBranch)
                .collect(Collectors.toSet());
        assert Stream.of(resDataFlowNodes, resDataFlowEdges, resControlFlowNodes, resControlFlowEdges)
                .noneMatch(t -> t.contains(null));
    }

    public void updateAfterCFGTrack() {
        this.exitNodes = resControlFlowNodes.stream()
                .filter(node -> node.isTerminal() || node.getLineOfCode() == -1)
                .collect(Collectors.toSet());
        assert Stream.of(resDataFlowNodes, resDataFlowEdges, resControlFlowNodes, resControlFlowEdges)
                .noneMatch(t -> t.contains(null));
    }


    public boolean is_ready_for_slice () {
        return Stream.of(graph, startNodes, entryNode,
                dataBindNodes, controlBindNodes, skeletonNodes, worklist).allMatch(Objects::nonNull);
    }

    public boolean is_ready_for_palette() {
        boolean complete = is_ready_for_slice() && !exitNodes.isEmpty();
        boolean not_contain_null = Stream.of(resDataFlowNodes, resDataFlowEdges, resControlFlowNodes, resControlFlowEdges)
                .noneMatch(t -> t.contains(null));
        boolean res_not_null = Stream.of(resControlFlowNodes, resControlFlowEdges, resDataFlowNodes)
                .noneMatch(Set::isEmpty);
        return complete && not_contain_null && res_not_null;
    }

    public PaletteResult setPalette() {
//        this.startCFNodes.forEach(node -> node.setPalette("start", true));
//        this.entryNode.setPalette("entry", true);
//        this.dataBindNodes.forEach(node -> node.setPalette("data_bind", true));
//        this.controlBindNodes.forEach(node -> node.setPalette("control_bind", true));
//        this.callsites.forEach(node -> node.setPalette("callsite", true));
//        this.exitNodes.forEach(node -> node.setPalette("exit", true));
        PaletteResult result = new PaletteResult(this.startCFNodes, this.entryNode, this.dataBindNodes,
                this.controlBindNodes, this.callsites, this.exitNodes);
        return result;
    }
}
