package fy.slicing.solver.cfg.edit;

import fy.progex.parse.PDGInfo;
import fy.slicing.result.CFGTrackResult;
import fy.slicing.solver.cdg.ChildNodeSolver;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;


import java.util.*;

/**
 *  switch结构是选择执行而非顺序执行
 *  因此只可能有一个case是满足要求的
 *  另外由于case节点本身不含任何变量名，因此不会是control bind
 *  所以只有两种情况需要考虑：
 *  1. case节点是某个data bind node的直接父节点
 *  2. case节点不是control bind，但是内涵control bind节点
 *  因此我们的策略是：检查每一个case节点是否满足上述两种情况，连接所有满足条件的case节点
 *  case节点内部后续再分析
 */
public class SwitchNodeEditor extends FlowEditor{

    CFNode switchNode;
    PDNode switchPDNode;
    ControlFlowGraph cfg;
    ControlDependenceGraph cdg;
    //parse
    List<CFNode> caseNodesList = new ArrayList<>();
    CFNode defaultNode;
    CFNode endSwitchNode;

    public SwitchNodeEditor(PDGInfo pdgInfo, Set<CFNode> skeletonNodes, CFGTrackResult<PDNode, DDEdge,CFNode,CFEdge> cfgResult, CFNode switchNode) {
        super(pdgInfo, skeletonNodes, cfgResult);
        this.switchNode = switchNode;
        this.switchPDNode = pdgInfo.findCDNode(switchNode);
        this.cfg = pdgInfo.cfg;
        this.cdg = pdgInfo.cdg;
    }

    public void parse() {
        // find all case nodes
        Deque<Edge<CFNode, CFEdge>> visiting = new ArrayDeque<>(cfg.copyOutgoingEdges(switchNode)); // 实际上只有一条：switch -> case 1
        while (!visiting.isEmpty()) {
            Edge<CFNode, CFEdge> edge = visiting.pop();
            CFNode node = edge.target;
            if (node.getCode().startsWith("case ")) {
                caseNodesList.add(node);
            }
            if (node.getCode().equals("default")) {
                defaultNode = node;
                break;
            }
            cfg.copyOutgoingEdges(node).stream()
                    .filter(edge1 -> edge1.label.type == CFEdge.Type.FALSE || 
                            edge1.label.type == CFEdge.Type.EPSILON)
                    .forEach(visiting::add);
        }
        assert !caseNodesList.isEmpty();
        if (defaultNode == null) {
            CFNode lastCase = caseNodesList.get(caseNodesList.size()-1);
            Deque<Edge<CFNode, CFEdge>> visiting2 = new ArrayDeque<>(cfg.copyOutgoingEdges(lastCase));
            while (!visiting2.isEmpty()) {
                Edge<CFNode, CFEdge> edge = visiting2.pop();
                CFNode node = edge.target;
                if (node.getCode().equals("end-switch")) {
                    endSwitchNode = node;
                    break;
                }
                visiting2.addAll(cfg.copyOutgoingEdges(node));
            }
        }
        else {
            Deque<Edge<CFNode, CFEdge>> visiting2 = new ArrayDeque<>(cfg.copyOutgoingEdges(defaultNode));
            while (!visiting2.isEmpty()) {
                Edge<CFNode, CFEdge> edge = visiting2.pop();
                CFNode node = edge.target;
                if (node.getCode().equals("end-switch")) {
                    endSwitchNode = node;
                    break;
                }
                visiting2.addAll(cfg.copyOutgoingEdges(node));
            }
        }
        assert endSwitchNode != null;
        switchNode.setProperty("endnode", endSwitchNode);
    }

    // FIXME: 2022/3/31 缺少对连续无跳出的case情况的支持
    public void edit() {
        CFNode validNode = null;
        for (CFNode caseNode : caseNodesList) {
            if (is_valid(caseNode)) {
                validNode = caseNode;
            }
            else {
                PDNode casePDNode = pdgInfo.findCDNode(caseNode);
                ChildNodeSolver childNodeSolver = new ChildNodeSolver(pdgInfo.cdg);
                List<PDNode> casePDChildren = childNodeSolver.find_all_children(casePDNode);
                if (casePDChildren.stream().anyMatch(node -> skeletonPDNodes.contains(node))) {
                    validNode = caseNode;
                }
            }
            break;
        }
        if (validNode != null) {
            addEdge(switchNode, validNode, CFEdge.Type.EPSILON);
            addEdge(validNode, endSwitchNode, CFEdge.Type.FALSE);
        }
        // 如果valid node为空则代表data node在default中
        else {
            if (defaultNode == null) {
                throw new IllegalStateException("default node & valid node are both null");
            }
            List<CFNode> validChildren = new ArrayList<>();
            Deque<Edge<CFNode, CFEdge>> visiting = new ArrayDeque<>(cfg.copyOutgoingEdges(defaultNode));
            while (!visiting.isEmpty()) {
                Edge<CFNode, CFEdge> edge = visiting.pop();
                CFNode node = edge.target;
                if (node.getCode().equals("end-switch")) {
                    break;
                }
                if (skeletonNodes.contains(node)) {
                    validChildren.add(node);
                }
                visiting.addAll(cfg.copyOutgoingEdges(node));
            }
            analyzeBranch(switchNode, validChildren, CFEdge.Type.EPSILON, endSwitchNode);
        }
    }
}
