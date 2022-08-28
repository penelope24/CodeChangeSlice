package fy.CDS.solver.cfg.edit;

import fy.CDS.data.SliceManager;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ForNodeFlowEditor extends FlowEditor {

    // provide
    CFNode forNode;
    PDNode forPDNode;
    ControlFlowGraph graph;
    // parse
    CFNode endNode;
    Edge<CFNode, CFEdge> brTrue;
    Edge<CFNode, CFEdge> brFalse;
    List<CFNode> validChildren;

    public ForNodeFlowEditor(PDGInfo pdgInfo, Set<CFNode> skeletonNodes,
                             SliceManager sliceManager, CFNode forNode) {
        super(pdgInfo, skeletonNodes, sliceManager);
        this.forNode = forNode;
        this.forPDNode = pdgInfo.findCDNode(forNode);
        this.graph = pdgInfo.cfg;
    }

    public ForNodeFlowEditor(PDGInfo pdgInfo, CFNode forNode) {
        super(pdgInfo);
        this.forNode = forNode;
        this.forPDNode = pdgInfo.findCDNode(forNode);
        this.graph = pdgInfo.cfg;
    }

    public void parse() {
        brTrue = graph.copyOutgoingEdges(forNode).stream()
                .filter(edge -> edge.label.type == CFEdge.Type.TRUE)
                .findFirst().orElse(null);
        assert brTrue != null;
        brFalse = graph.copyOutgoingEdges(forNode).stream()
                .filter(edge -> edge.label.type == CFEdge.Type.FALSE)
                .findFirst().orElse(null);
        assert brFalse != null;
        endNode = brFalse.target;
        assert endNode != null;
        List<PDNode> validPDChildren = findValidChildrenForBrNode(forPDNode);
        validChildren = validPDChildren.stream()
                .map(pdgInfo::findCFNodeByCDNode)
                .collect(Collectors.toList());
        forNode.setProperty("endnode", endNode);
    }

    public void edit() {
        // true branch
        analyzeBranch(forNode, validChildren, CFEdge.Type.TRUE, forNode);
        // false branch
        addEdge(forNode, endNode, CFEdge.Type.FALSE);
        // before brNode

    }

}
