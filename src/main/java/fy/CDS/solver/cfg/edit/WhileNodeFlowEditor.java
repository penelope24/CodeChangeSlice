package fy.CDS.solver.cfg.edit;

import fy.CDS.data.SliceManager;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WhileNodeFlowEditor extends FlowEditor {
    // provide
    CFNode whileNode;
    ControlFlowGraph graph;

    // parse
    PDNode whilePDNode;
    CFNode endNode;
    Edge<CFNode, CFEdge> brTrue;
    Edge<CFNode, CFEdge> brFalse;
    List<CFNode> validChildren = new ArrayList<>();

    public WhileNodeFlowEditor(PDGInfo pdgInfo, Set<CFNode> skeletonNodes, SliceManager sliceManager, CFNode whileNode) {
        super(pdgInfo, skeletonNodes, sliceManager);
        this.whileNode = whileNode;
        this.graph = pdgInfo.cfg;
        this.whilePDNode = pdgInfo.findCDNode(whileNode);
        this.skeletonPDNodes = skeletonNodes.stream()
                .map(pdgInfo::findCDNode)
                .collect(Collectors.toSet());
    }

    public WhileNodeFlowEditor(PDGInfo pdgInfo, CFNode whileNode) {
        super(pdgInfo);
        this.whileNode = whileNode;
        this.graph = pdgInfo.cfg;
        this.whilePDNode = pdgInfo.findCDNode(whileNode);
        this.skeletonPDNodes = skeletonNodes.stream()
                .map(pdgInfo::findCDNode)
                .collect(Collectors.toSet());
    }

    public void parse() {
        brTrue = graph.copyOutgoingEdges(whileNode).stream()
                .filter(edge -> edge.label.type == CFEdge.Type.TRUE)
                .findFirst().orElse(null);
        assert brTrue != null;
        brFalse = graph.copyOutgoingEdges(whileNode).stream()
                .filter(edge -> edge.label.type == CFEdge.Type.FALSE)
                .findFirst().orElse(null);
        assert brFalse != null;
        endNode = brFalse.target;
        assert endNode != null;
        List<PDNode> validPDChildren = findValidChildrenForBrNode(whilePDNode);
        validChildren = validPDChildren.stream()
                .map(pdgInfo::findCFNodeByCDNode)
                .collect(Collectors.toList());
        whileNode.setProperty("endnode", endNode);
    }

    public void edit() {
        // true branch
        analyzeBranch(whileNode, validChildren, CFEdge.Type.TRUE, whileNode);
        // false branch
        addEdge(whileNode, endNode, CFEdge.Type.FALSE);
    }

}
