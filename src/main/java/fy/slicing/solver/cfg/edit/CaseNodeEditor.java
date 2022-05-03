package fy.slicing.solver.cfg.edit;

import fy.progex.parse.PDGInfo;
import fy.slicing.result.CFGTrackResult;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.CDEdge;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * case外部的连线已经在switch editor中定义，因此只需要关注case的true分支即可
 */
public class CaseNodeEditor extends FlowEditor{

    CFNode caseNode;
    PDNode casePDNode;
    ControlFlowGraph cfg;
    ControlDependenceGraph cdg;
    // parse
    List<CFNode> validChildren;
    CFNode endSwitchNode;

    public CaseNodeEditor(PDGInfo pdgInfo, Set<CFNode> skeletonNodes, CFGTrackResult<PDNode, DDEdge, CFNode, CFEdge> cfgResult, CFNode caseNode) {
        super(pdgInfo, skeletonNodes, cfgResult);
        this.caseNode = caseNode;
        this.casePDNode = pdgInfo.findCDNode(caseNode);
        this.cfg = pdgInfo.cfg;
        this.cdg = pdgInfo.cdg;
    }

    public void parse() {
        List<PDNode> validPDChildren = findValidChildren(casePDNode, CDEdge.Type.TRUE);
        validChildren = validPDChildren.stream()
                .map(pdgInfo::findCFNodeByCDNode)
                .collect(Collectors.toList());
        assert !validPDChildren.isEmpty();
        // find end switch node
        Deque<Edge<CFNode, CFEdge>> visiting = new ArrayDeque<>();
        Edge<CFNode, CFEdge> brFalse = cfg.copyOutgoingEdges(caseNode).stream()
                .filter(edge -> edge.label.type == CFEdge.Type.FALSE)
                .findFirst().orElse(null);
        assert brFalse != null;
        visiting.add(brFalse);
        while (!visiting.isEmpty()) {
            Edge<CFNode, CFEdge> edge = visiting.pop();
            CFNode node = edge.target;
            if (node.getCode().equals("end-switch")) {
                endSwitchNode = node;
                break;
            }
            cfg.copyOutgoingEdges(node).stream()
                    .filter(edge1 -> edge1.label.type == CFEdge.Type.FALSE || edge1.label.type == CFEdge.Type.EPSILON)
                    .forEach(visiting::add);
        }
        assert endSwitchNode != null;
    }

    public void edit() {
        CFNode lastChild = validChildren.get(validChildren.size() -1);
        if (lastChild.getType() == NodeType.RETURN || lastChild.getType() == NodeType.THROW) {
            analyzeBranch(caseNode, validChildren.subList(0, validChildren.size()-1), CFEdge.Type.TRUE, lastChild);
        }
        else {
            analyzeBranch(caseNode, validChildren, CFEdge.Type.TRUE, endSwitchNode);
        }
    }

}
