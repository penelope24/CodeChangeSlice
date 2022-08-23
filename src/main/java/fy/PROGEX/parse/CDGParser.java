package fy.PROGEX.parse;

import fy.CDS.solver.cdg.ChildNodeSolver;
import fy.CDS.solver.cdg.ParentSolver;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.pdg.CDEdge;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.*;
import java.util.stream.Collectors;

public class CDGParser{
    // provide
    PDGInfo pdgInfo;
    Set<PDNode> dataBindCDNodes;
    ControlDependenceGraph cdg;

    public CDGParser(PDGInfo pdgInfo) {
        this.pdgInfo = pdgInfo;
        this.cdg = pdgInfo.cdg;
    }

    public void parse() {
        for (PDNode node : cdg.copyVertexSet()) {
            analyze_node(node);
            analyze_node2(node);
        }
        // 分析级联情形
        ParentSolver parentSolver = new ParentSolver(pdgInfo.cdg);
        ChildNodeSolver childNodeSolver = new ChildNodeSolver(pdgInfo.cdg);
        Set<PDNode> roots = cdg.copyVertexSet().stream()
                .filter(node -> node.getType() == NodeType.ROOT)
                .collect(Collectors.toSet());
        for (PDNode root : roots) {
            PDNode cascade_start_node = null;
            List<PDNode> cascadeNodes = new ArrayList<>();
            // bfs
            Deque<Edge<PDNode, CDEdge>> visiting = new ArrayDeque<>();
            Edge<PDNode, CDEdge> dummy = new Edge<>(null, null, root);
            visiting.add(dummy);
            while (!visiting.isEmpty()) {
                Edge<PDNode, CDEdge> edge = visiting.pop();
                PDNode node = edge.target;
                String code = node.getCode();
                if (code.startsWith("FOLLOW-")) {
                    List<PDNode> casNodes = childNodeSolver.find_first_level_children(node);
                    for (PDNode casNode : casNodes) {
                        casNode.setProperty("cascade", true);
                        cascadeNodes.add(casNode);
                    }
                }
                if (code.equals("FOLLOW-1")) {
                    cascade_start_node = parentSolver.find_first_par_loose(node);
                    cascade_start_node.setProperty("cascade_start", true);

                }
                visiting.addAll(cdg.copyOutgoingEdges(node));
            }
            if (cascade_start_node != null) {
                PDNode cascade_containing_node = parentSolver.find_first_par(cascade_start_node);
                cascade_containing_node.setProperty("cascade_contain", true);
                cascade_containing_node.setProperty("cascade_children", cascadeNodes);
            }
        }

    }

    private void analyze_node(PDNode node) {
        if (node.getType() != NodeType.HELP && node.getType() != NodeType.ISO) {
            PDNode ddNode = pdgInfo.uid2ddNodes.get(node.getUid());
            if (ddNode != null) {
                Arrays.asList(ddNode.getAllDEFs()).forEach(node::addDEF);
                Arrays.asList(ddNode.getAllUSEs()).forEach(node::addUSE);
            }
        }

        int in_num = cdg.getInDegree(node);
        int out_num = cdg.getOutDegree(node);
        String code = node.getCode().toLowerCase();
        if (in_num == 0 && out_num == 0) {
            node.setType(NodeType.ISO);
            return;
        }
        // 存在一些孤岛help节点，所以要加入行号判断
        if (in_num == 0 && node.getLineOfCode() != 0) {
            node.setType(NodeType.ROOT);
            return;
        }
        if (node.getLineOfCode() == 0) {
            node.setType(NodeType.HELP);
            return;
        }
        if (out_num == 0) {
            if (code.startsWith("return ") || code.equals("return") || code.equals("return;")) {
                node.setType(NodeType.RETURN);
                return;
            }
            if (code.equals("break") || code.equals("break;")) {
                node.setType(NodeType.BREAK);
                return;
            }
            if (code.startsWith("throw ")) {
                node.setType(NodeType.THROW);
                return;
            }
            if (code.equals("continue") || code.equals("continue;")) {
                node.setType(NodeType.CONTINUE);
                return;
            }
            if (code.startsWith("case ")) {
                node.setType(NodeType.CASE);
                return;
            }
            if (code.equals("default")) {
                node.setType(NodeType.DEFAULT);
                return;
            }
            if (code.startsWith("catch ")) {
                node.setType(NodeType.CATCH);
                return;
            }
            node.setType(NodeType.NORMAL);
        }
        if (out_num > 0 ) {
            if (code.equals("try") || code.startsWith("try ")) {
                node.setType(NodeType.TRY);
                return;
            }
            if (code.startsWith("catch ")) {
                node.setType(NodeType.CATCH);
                return;
            }
            if (code.startsWith("switch ")) {
                node.setType(NodeType.SWITCH);
                return;
            }
            if (code.startsWith("case ")) {
                node.setType(NodeType.CASE);
                return;
            }
            if (code.startsWith("while ")) {
                node.setType(NodeType.WHILE);
                return;
            }
            if (code.startsWith("for ")) {
                node.setType(NodeType.FOR);
                return;
            }
            if (code.startsWith("if ")) {
                node.setType(NodeType.IF);
                return;
            }
            if (code.startsWith("synchronized ") || code.equals("synchronized")) {
                node.setType(NodeType.SYNCHRONIZED);
            }

//            throw new IllegalStateException("new unseen branch node type");
        }
    }

    private void analyze_node2(PDNode node) {
        if (node.getType() == null) {
            node.setBranch(false);
            node.setTerminal(false);
            return;
        }
        switch (node.getType()) {
            case FOR:
            case WHILE:
            case IF:
            case SWITCH:
            case CASE:
            case TRY:
                node.setBranch(true);
                node.setTerminal(false);
                break;
            case BREAK:
            case THROW:
            case RETURN:
            case CONTINUE:
                node.setTerminal(true);
                node.setBranch(false);
                break;
            default:
                node.setBranch(false);
                node.setTerminal(false);
                break;
        }
    }

}
