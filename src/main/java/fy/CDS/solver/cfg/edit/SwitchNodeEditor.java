package fy.CDS.solver.cfg.edit;

import fy.CDS.data.SliceManager;
import fy.CDS.solver.cdg.SiblingSolver;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.CDEdge;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.*;
import java.util.stream.Collectors;

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
    List<Integer> chLines;
    //parse
    List<CFNode> caseNodesList = new ArrayList<>();
    CFNode defaultNode;
    CFNode endSwitchNode;
    List<CFNode> defaultNodeValidChildren = new ArrayList<>();

    public SwitchNodeEditor(PDGInfo pdgInfo, Set<CFNode> skeletonNodes, SliceManager sliceManager, CFNode switchNode) {
        super(pdgInfo, skeletonNodes, sliceManager);
        this.switchNode = switchNode;
        this.switchPDNode = pdgInfo.findCDNode(switchNode);
        this.cfg = pdgInfo.cfg;
        this.cdg = pdgInfo.cdg;
        this.chLines = sliceManager.chLines;
    }

    public void parse() {
        // find all case nodes
        Deque<Edge<CFNode, CFEdge>> visiting = new ArrayDeque<>(cfg.copyOutgoingEdges(switchNode)); // 实际上只有一条：switch -> case 1
        Deque<Edge<CFNode, CFEdge>> visited = new ArrayDeque<>();
        while (!visiting.isEmpty()) {
            Edge<CFNode, CFEdge> edge = visiting.pop();
            visited.add(edge);
            CFNode node = edge.target;
            if (node.getCode().startsWith("case ")) {
                caseNodesList.add(node);
            }
            if (node.getCode().equals("default")) {
                defaultNode = node;
            }
            if (node.getCode().equals("end-switch")) {
                endSwitchNode = node;
            }
            cfg.copyOutgoingEdges(node).forEach(e -> {
                if (!visited.contains(e)) {
                    visiting.add(e);
                }
            });
        }
        assert !caseNodesList.isEmpty();
        caseNodesList = caseNodesList.stream().distinct().collect(Collectors.toList());
        switchNode.setProperty("endnode", endSwitchNode);
    }

    public void edit() {
        // sketch
        addEdge(switchNode, caseNodesList.get(0), CFEdge.Type.EPSILON);
        for (int i=0; i<caseNodesList.size()-1; i++) {
            CFNode src = caseNodesList.get(i);
            CFNode tgt = caseNodesList.get(i+1);
            addEdge(src, tgt, CFEdge.Type.FALSE);
        }
        if (defaultNode != null) {
            addEdge(caseNodesList.get(caseNodesList.size()-1), defaultNode, CFEdge.Type.FALSE);
        }
        // inside each case node
        for (int i=0; i<caseNodesList.size(); i++) {
            CFNode caseNode = caseNodesList.get(i);
            CFNode nextCaseNode = i + 1 < caseNodesList.size() ?
                    caseNodesList.get(i+1)
                    :
                    defaultNode;
            PDNode casePDNode = pdgInfo.findCDNode(caseNode);
            List<PDNode> casePDChildren = findValidChildrenForBrNode(casePDNode, CDEdge.Type.TRUE);
            List<CFNode> validCaseChildren = casePDChildren.stream()
                    .map(pdgInfo::findCFNodeByCDNode)
                    .collect(Collectors.toList());
            if (validCaseChildren.isEmpty()) {
                continue;
            }
            CFNode lastChild = validCaseChildren.get(validCaseChildren.size()-1);
            if (lastChild.getType() != NodeType.BREAK) {
                analyzeBranch(caseNode, validCaseChildren, CFEdge.Type.TRUE, nextCaseNode);
            }
            else {
                analyzeBranch(caseNode, validCaseChildren, CFEdge.Type.TRUE, endSwitchNode);
            }
        }
        if (defaultNode != null) {
            PDNode defaultPDNode = pdgInfo.findCDNode(defaultNode);
            SiblingSolver siblingSolver = new SiblingSolver(this.cdg);
            List<PDNode> PDSiblings = siblingSolver.find_siblings(defaultPDNode);
            List<CFNode> validSiblings = PDSiblings.stream()
                    .map(pdgInfo::findCFNodeByCDNode)
                    .filter(super::is_valid)
                    .collect(Collectors.toList());
            analyzeBranch(defaultNode, validSiblings, CFEdge.Type.EPSILON, endSwitchNode);
        }
    }

    private boolean is_valid_defaultNode (CFNode defaultNode) {
        if (defaultNode == null) {
            return false;
        }
        if (defaultNodeValidChildren.isEmpty()) {
            return false;
        }
        if (defaultNodeValidChildren.size() == 1) {
            CFNode child = defaultNodeValidChildren.get(0);
            if (child.getType() == NodeType.BREAK || child.getType() == NodeType.CONTINUE) {
                return false;
            }
        }
        return true;
    }
}
