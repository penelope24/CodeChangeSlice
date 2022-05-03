package fy.slicing.solver.cfg.edit;

import fy.progex.parse.PDGInfo;
import fy.slicing.result.CFGTrackResult;
import fy.slicing.solver.cdg.ChildNodeSolver;
import fy.slicing.solver.cfg.ControlFlowSolver;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.CDEdge;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.*;
import java.util.stream.Collectors;

public class FlowEditor extends ControlFlowSolver {
    Set<CFNode> skeletonNodes;
    Set<PDNode> skeletonPDNodes;
    CFGTrackResult cfgResult;
    boolean for_test;


    public FlowEditor(PDGInfo pdgInfo, Set<CFNode> skeletonNodes,
                      CFGTrackResult<PDNode, DDEdge,CFNode,CFEdge> cfgResult) {
        super(pdgInfo);
        this.pdgInfo = pdgInfo;
        this.skeletonNodes = skeletonNodes;
        this.skeletonPDNodes = skeletonNodes.stream()
                .map(pdgInfo::findCDNode)
                .collect(Collectors.toSet());
        this.cfgResult = cfgResult;
        this.for_test = false;
    }

    public void parse() {}

    public void edit() {}

    /**
     *  找到一个branch节点下所有符合要求的孩子节点
     * @param startNode
     * @return
     */
    public List<PDNode> findValidChildren(PDNode startNode) {
        ChildNodeSolver childNodeSolver = new ChildNodeSolver(pdgInfo.cdg);
        List<PDNode> first_level_children = childNodeSolver.find_first_level_children(startNode);
        if (startNode.getProperty("cascade_contain") != null) {
            List<PDNode> cascadeNodes = (List<PDNode>) startNode.getProperty("cascade_children");
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
                List<List<PDNode>> levels = childNodeSolver.find_all_children_level_order(startNode);
                for (List<PDNode> level : levels) {
                    if (level.stream().anyMatch(this::is_valid)) {
                        validLevel = level;
                        break;
                    }
                }
//                assert validLevel != null;
                if (validLevel != null) {
                    validLevel.removeIf(node1 -> !is_valid(node1));
                    validPDChildren.addAll(validLevel);
                }
            }
        }
        return validPDChildren;
    }

    public List<PDNode> findValidChildren(PDNode startNode, CDEdge.Type filter) {
        ChildNodeSolver childNodeSolver = new ChildNodeSolver(pdgInfo.cdg);
        List<PDNode> first_level_children = childNodeSolver.find_first_level_children(startNode, filter);
        if (startNode.getProperty("cascade_contain") != null) {
            List<PDNode> cascadeNodes = (List<PDNode>) startNode.getProperty("cascade_children");
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
                List<List<PDNode>> levels = childNodeSolver.find_all_children_level_order(startNode);
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
        if (for_test) {
            return true;
        }
        if (node.getType() == NodeType.BREAK) {
            return true;
        }
        if (node.getType() == NodeType.CONTINUE) {
            return true;
        }
        return skeletonPDNodes.contains(node);
    }

    public boolean is_valid(CFNode node) {
        if (for_test) {
            return true;
        }
        if (node.getType() == NodeType.BREAK) {
            return true;
        }
        if (node.getType() == NodeType.CONTINUE) {
            return true;
        }
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
                    addEdge(transfer(src), tgt, CFEdge.Type.EPSILON);
                }
            }
            CFNode last = childNodes.get(size - 1);
            if (!last.isTerminal()) {
                CFNode last_end = transfer(last);
                if (last_end != null) {
                    addEdge(transfer(last), endNode, CFEdge.Type.EPSILON);
                }
            }
            // 如果分支以return结束，则连到return即停止
            else if (last.getType() == NodeType.RETURN) {

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
                return (CFNode) cfNode.getProperty("endnode");
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
        cfgResult.addControlNode(src);
        cfgResult.addControlNode(tgt);
        Edge<CFNode, CFEdge> newEdge = new Edge<>(src, new CFEdge(type), tgt);
        if (!contains_edge(cfgResult.getResControlFlowEdges(), newEdge)) {
            cfgResult.addResControlFlowEdge(newEdge);
        }
    }

    public void setFor_test(boolean for_test) {
        this.for_test = for_test;
    }
}
