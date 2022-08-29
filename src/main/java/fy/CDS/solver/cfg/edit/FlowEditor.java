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

    // for test only
    public FlowEditor(PDGInfo pdgInfo) {
        super(pdgInfo);
        this.test = true;
        this.skeletonNodes = new LinkedHashSet<>();
        this.skeletonPDNodes = new LinkedHashSet<>();
    }

    public void parse() {}

    public void edit() {}

//    public List<PDNode> find(PDNode brNode) {
//        ChildNodeSolver childNodeSolver = new ChildNodeSolver(pdgInfo.cdg);
//
//    }

    /**
     *  找到一个branch节点下所有符合要求的孩子节点
     * @param startNode
     * @return
     */
    public List<PDNode> findValidChildrenForBrNode(PDNode startNode) {
        ChildNodeSolver solver = new ChildNodeSolver(pdgInfo.cdg);
        Set<PDNode> resChildren = new LinkedHashSet<>();
        List<PDNode> firstLevel = solver.find_first_level_children(startNode);
        for (PDNode node : firstLevel) {
            if (is_valid(node)) {
                resChildren.add(node);
            }
            else if (node.isBranch()) {
                List<List<PDNode>> levels = solver.find_all_children_level_order(startNode);
                for (List<PDNode> level : levels) {
                    if (level.stream().anyMatch(this::is_valid)) {
                        level.stream()
                                .filter(this::is_valid)
                                .forEach(resChildren::add);
                        break;
                    }
                }
            }
        }
        return new ArrayList<>(resChildren);
    }

    public List<PDNode> findValidChildrenForBrNode(PDNode brNode, CDEdge.Type filter) {
        ChildNodeSolver solver = new ChildNodeSolver(pdgInfo.cdg);
        Set<PDNode> resChildren = new LinkedHashSet<>();
        List<PDNode> firstLevel = solver.find_first_level_children(brNode, filter);
        for (PDNode node : firstLevel) {
            if (is_valid(node)) {
                resChildren.add(node);
            }
            else if (node.isBranch()) {
                List<List<PDNode>> levels = solver.find_all_children_level_order(node);
                for (List<PDNode> level : levels) {
                    if (level.stream().anyMatch(this::is_valid)) {
                        level.stream()
                                .filter(this::is_valid)
                                .forEach(resChildren::add);
                        break;
                    }
                }
            }
        }
        return new ArrayList<>(resChildren);
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
        if (sliceManager != null) {
            if (src == null || tgt == null) return;
            sliceManager.resControlFlowNodes.add(src);
            sliceManager.resControlFlowNodes.add(tgt);
            Edge<CFNode, CFEdge> newEdge = new Edge<>(src, new CFEdge(type), tgt);
            if (!contains_edge(sliceManager.resControlFlowEdges, newEdge)) {
                sliceManager.resControlFlowEdges.add(newEdge);
            }
        }
        else {
            // test mode
            if (src == null || tgt == null) return;
            tmpFlowEditResult.addVertex(src);
            tmpFlowEditResult.addVertex(tgt);
            Edge<CFNode, CFEdge> newEdge = new Edge<>(src, new CFEdge(type), tgt);
            if (!tmpFlowEditResult.containsEdge(newEdge)) {
                tmpFlowEditResult.addEdge(newEdge);
            }
        }
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public Slice getTmpFlowEditResult() {
        return tmpFlowEditResult;
    }
}
