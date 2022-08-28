package fy.CDS.solver.cfg.edit;

import fy.CDS.data.SliceManager;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TryNodeFlowEditor extends FlowEditor{

    //provide
    CFNode tryNode;
    PDNode tryPDNode;
    ControlFlowGraph graph;
    // parse
    Edge<CFNode, CFEdge> tryBranch;
    CFNode endTryNode;
    List<CFNode> validChildren;

    public TryNodeFlowEditor(PDGInfo pdgInfo, Set<CFNode> skeletonNodes, SliceManager sliceManager, CFNode tryNode) {
        super(pdgInfo, skeletonNodes, sliceManager);
        this.tryNode = tryNode;
        this.tryPDNode = pdgInfo.findCDNode(tryNode);
        this.graph = pdgInfo.cfg;
    }

    public TryNodeFlowEditor(PDGInfo pdgInfo, CFNode tryNode) {
        super(pdgInfo);
        this.tryNode = tryNode;
        this.tryPDNode = pdgInfo.findCDNode(tryNode);
        this.graph = pdgInfo.cfg;
    }

    public void parse() {
        tryBranch = graph.copyOutgoingEdges(tryNode).stream()
                .findFirst().orElse(null);
        assert tryBranch != null;
        List<PDNode> validPDChildren = findValidChildrenForBrNode(tryPDNode);
        validChildren = validPDChildren.stream()
                .map(pdgInfo::findCFNodeByCDNode)
                .collect(Collectors.toList());
        Deque<Edge<CFNode, CFEdge>> visiting = new ArrayDeque<>();
        visiting.add(tryBranch);
        while (!visiting.isEmpty()) {
            Edge<CFNode, CFEdge> edge = visiting.pop();
            CFNode node = edge.target;
            if (node.getCode().equals("end-try")) {
                endTryNode = node;
                break;
            }
            visiting.addAll(graph.copyOutgoingEdges(node));
        }
    }

    public void edit() {
        analyzeBranch(tryNode, validChildren, CFEdge.Type.EPSILON, endTryNode);
    }
}
