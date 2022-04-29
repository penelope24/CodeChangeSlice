package fy.slicing.solver.cfg.edit;

import fy.progex.parse.PDGInfo;
import fy.slicing.result.CFGTrackResult;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RootNodeEditor extends FlowEditor{
    ControlFlowGraph cfg;
    ControlDependenceGraph cdg;
    CFNode root;
    PDNode rootPDNode;
    CFNode exitNode;
    // parse
    List<CFNode> validChildren = new ArrayList<>();

    public RootNodeEditor(PDGInfo pdgInfo, Set<CFNode> skeletonNodes, CFGTrackResult<PDNode, DDEdge,CFNode,CFEdge> cfgResult, CFNode root) {
        super(pdgInfo, skeletonNodes, cfgResult);
        this.cfg = pdgInfo.cfg;
        this.cdg = pdgInfo.cdg;
        this.root = root;
        this.rootPDNode = pdgInfo.findCDNode(root);
        this.skeletonPDNodes = skeletonNodes.stream()
                .map(pdgInfo::findCDNode)
                .collect(Collectors.toSet());
        this.exitNode = new CFNode();
        exitNode.setCode("exit");
        exitNode.setLineOfCode(-1);
        exitNode.setProperty("exit", true);
    }

    public void parse() {
        List<PDNode> validPDChildren = findValidChildren(rootPDNode);
        validChildren = validPDChildren.stream()
                .map(node -> pdgInfo.findCFNodeByCDNode(node))
                .collect(Collectors.toList());
    }

    public void edit() {
        analyzeBranch(root, validChildren, CFEdge.Type.EPSILON, exitNode);
    }
}
