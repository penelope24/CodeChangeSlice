package fy.CDS.solver.cfg.edit;

import fy.CDS.data.Slice;
import fy.CDS.data.SliceManager;
import fy.CDS.solver.cdg.ChildNodeSolver;
import fy.CDS.solver.cfg.ControlFlowSolver;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.CDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.*;
import java.util.stream.Collectors;

public class FlowEditor extends ControlFlowSolver {
    Set<CFNode> skeletonNodes;
    Set<PDNode> skeletonPDNodes;
    SliceManager sliceManager;
    boolean test = false;
    // tmp flow edit result for test use.
    Slice tmpFlowEditResult = new Slice();

    public FlowEditor(PDGInfo pdgInfo, Set<CFNode> skeletonNodes, SliceManager sliceManager) {
        super(pdgInfo);
        this.pdgInfo = pdgInfo;
        this.skeletonNodes = skeletonNodes;
        this.skeletonPDNodes = skeletonNodes.stream()
                .map(pdgInfo::findCDNode)
                .collect(Collectors.toSet());
        this.sliceManager = sliceManager;
    }

    public void parse() {}

    public void edit() {}

    /**
     *  找到一个branch节点下所有符合要求的孩子节点
     * @param brNode
     * @return
     */
    public List<PDNode> findValidChildrenForBrNode(PDNode brNode) {
        ChildNodeSolver childNodeSolver = new ChildNodeSolver(pdgInfo.cdg);
        List<PDNode> first_level_children = childNodeSolver.find_first_level_children(brNode);
        // 如果是级联的if节点，那么直接找出所有级联的子节点
        if (brNode.getProperty("cascade_contain") != null) {
            List<PDNode> cascadeNodes = (List<PDNode>) brNode.getProperty("cascade_children");
            for (PDNode casNode : cascadeNodes) {
                if (!first_level_children.contains(casNode)) {
                    first_level_children.add(casNode);
                }
            }
            // 去除list中重复项并返回
            Set<PDNode> set = new LinkedHashSet<>(first_level_children);
            first_level_children.clear();
            first_level_children.addAll(set);
            return first_level_children;
        }
        // 如果不是级联情形
        List<PDNode> validPDChildren = new ArrayList<>();
        for (PDNode node : first_level_children) {
            if (is_valid(node)) {
                validPDChildren.add(node);
            }
            else if (node.isBranch()) {
                List<PDNode> validLevel = null;
                // todo a bug brnode should be node
                List<List<PDNode>> levels = childNodeSolver.find_all_children_level_order(node);
                for (List<PDNode> level : levels) {
                    if (level.stream().anyMatch(this::is_valid)) {
                        validLevel = level;
                        break;
                    }
                }
                if (validLevel != null) {
                    validLevel.removeIf(node1 -> !is_valid(node1));
                    validPDChildren.addAll(validLevel);
                }
            }
        }
        return validPDChildren;
    }

    public List<PDNode> findValidChildrenForBrNode(PDNode beNode, CDEdge.Type filter) {
        ChildNodeSolver childNodeSolver = new ChildNodeSolver(pdgInfo.cdg);
        List<PDNode> first_level_children = childNodeSolver.find_first_level_children(beNode, filter);
        if (beNode.getProperty("cascade_contain") != null) {
            List<PDNode> cascadeNodes = (List<PDNode>) beNode.getProperty("cascade_children");
            for (PDNode casNode : cascadeNodes) {
                if (!first_level_children.contains(casNode)) {
                    first_level_children.add(casNode);
                }
            }
            Set<PDNode> set = new LinkedHashSet<>(first_level_children);
            first_level_children.clear();
            first_level_children.addAll(set);
            return first_level_children;
        }
        List<PDNode> validPDChildren = new ArrayList<>();
        for (PDNode node : first_level_children) {
            if (is_valid(node)) {
                validPDChildren.add(node);
            }
            else if (node.isBranch()) {
                List<PDNode> validLevel = null;
                List<List<PDNode>> levels = childNodeSolver.find_all_children_level_order(node);
                for (List<PDNode> level : levels) {
                    if (level.stream().anyMatch(this::is_valid)) {
                        validLevel = level;
                        break;
                    }
                }
                // 下面这条assert是不安全的（如果以brnode为起始点不一定满足下面的assert）
//                assert validLevel != null;
                if (validLevel != null) {
                    validLevel.removeIf(node1 -> !is_valid(node1));
                    validPDChildren.addAll(validLevel);
                }
            }
        }
        return validPDChildren;
    }

    public boolean is_valid(PDNode node) {
        if (this.test) {
            return true;
        }
        if (node.getType() == NodeType.BREAK) {
            return true;
        }
        if (node.getType() == NodeType.CONTINUE) {
            return true;
        }
//        if (node.getType() == NodeType.TRY) {
//            return true;
//        }
        return skeletonPDNodes.contains(node);
    }

    public boolean is_valid(CFNode node) {
        if (this.test) {
            return true;
        }
        if (node.getType() == NodeType.BREAK) {
            return true;
        }
        if (node.getType() == NodeType.CONTINUE) {
            return true;
        }
//        if (node.getType() == NodeType.TRY) {
//            return true;
//        }
        return skeletonNodes.contains(node);
    }

    public void analyzeBranch(CFNode brNode, List<CFNode> childNodes, CFEdge.Type type, CFNode endNode) {
        childNodes.removeIf(cfNode -> !is_valid(cfNode));
        int size = childNodes.size();
        if (size == 0) {
            // do nothing
        }
        else {
            addEdge(brNode, childNodes.get(0), type);
            for (int i=0; i<size-1; i++) {
                CFNode src = childNodes.get(i);
                CFNode tgt = childNodes.get(i+1);
                CFNode src_end = transfer(src);
                if (src_end != null) {
                    addEdge(src_end, tgt, CFEdge.Type.EPSILON);
                }
            }
            CFNode last = childNodes.get(size - 1);
            if (!last.isTerminal()) {
                CFNode last_end = transfer(last);
                if (last_end != null) {
                    addEdge(last_end, endNode, CFEdge.Type.EPSILON);
                }
            }
            // 如果分支以return结束，则连到return即停止
            else if (last.getType() == NodeType.RETURN || last.getType() == NodeType.THROW) {

            }
            else if (last.getType() == NodeType.BREAK) {
                CFNode next = pdgInfo.cfg.copyOutgoingEdges(last).stream()
                        .map(edge -> edge.target)
                        .findFirst().orElse(null);
                assert next != null;
                assert next.getType() == NodeType.HELP;
                addEdge(last, next, CFEdge.Type.EPSILON);
            }
            else if (last.getType() == NodeType.CONTINUE) {
                CFNode next = pdgInfo.cfg.copyOutgoingEdges(last).stream()
                        .map(edge -> edge.target)
                        .findFirst().orElse(null);
                assert next != null;
                assert next.isBranch();
                addEdge(last, next, CFEdge.Type.EPSILON);
            }
        }
    }

    public CFNode transfer(CFNode cfNode) {
        if (cfNode.getType() == null) {
            return cfNode;
        }
        switch (cfNode.getType()) {
            case SWITCH:
            case IF:
            case WHILE:
            case FOR:
                if (cfNode.getProperty("endnode") != null) {
                    return (CFNode) cfNode.getProperty("endnode");
                }
                else {
                    return cfNode;
                }
            case CONTINUE: {
                CFNode next = pdgInfo.cfg.copyOutgoingEdges(cfNode).stream()
                        .map(edge -> edge.target)
                        .findFirst().orElse(null);
                assert next != null;
                return next;
            }
            case BREAK: {
                CFNode next = pdgInfo.cfg.copyOutgoingEdges(cfNode).stream()
                        .map(edge -> edge.target)
                        .findFirst().orElse(null);
                assert next != null;
                return (CFNode) next.getProperty("endnode");
            }
            default:
                return cfNode;
        }
    }

    public boolean contains_edge(Set<Edge<CFNode, CFEdge>> edges ,Edge<CFNode, CFEdge> edge) {
        return edges.stream().anyMatch(
                edge1 -> Objects.equals(edge1.target, edge.target) && Objects.equals(edge1.source, edge.source)
        );
    }

    public void addEdge(CFNode src, CFNode tgt, CFEdge.Type type) {
        if (src == null || tgt == null) return;
        sliceManager.resControlFlowNodes.add(src);
        sliceManager.resControlFlowNodes.add(tgt);
        Edge<CFNode, CFEdge> newEdge = new Edge<>(src, new CFEdge(type), tgt);
        if (!contains_edge(sliceManager.resControlFlowEdges, newEdge)) {
            sliceManager.resControlFlowEdges.add(newEdge);
        }
        // also add to tmp flow edit result
        tmpFlowEditResult.addVertex(src);
        tmpFlowEditResult.addVertex(tgt);
        if (!tmpFlowEditResult.containsEdge(newEdge)) {
            tmpFlowEditResult.addEdge(newEdge);
        }
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public Slice getTmpFlowEditResult() {
        return tmpFlowEditResult;
    }
}
