package fy.slicing.solver.cdg;

import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.List;
import java.util.stream.Collectors;

public class SiblingSolver extends ControlDepSolver {

    public SiblingSolver(ControlDependenceGraph graph) {
        super(graph);
    }

    /**
     * 无论在CDG的哪一层，找兄弟节点都只需要追溯first level par
     * 注意过滤掉aux node（行号为0）
     */
    public List<PDNode> find_siblings(PDNode node) {
        PDNode first_level_par = graph.copyIncomingEdges(node).stream()
                .map(edge -> edge.source)
                .findFirst().orElse(null);
        assert first_level_par != null;
        return graph.copyOutgoingEdges(first_level_par).stream()
                .map(edge -> edge.target)
                .filter(node1 -> node1 != node)
                .filter(node1 -> node1.getLineOfCode() != 0)
                .collect(Collectors.toList());
    }

    /**
     * 利用行号甄别当前node之前的兄弟节点
     */
    public List<PDNode> find_pred_siblings(PDNode node) {
        PDNode first_level_par = graph.copyIncomingEdges(node).stream()
                .map(edge -> edge.source)
                .findFirst().orElse(null);
        assert first_level_par != null;
        return graph.copyOutgoingEdges(first_level_par).stream()
                .map(edge -> edge.target)
                .filter(node1 -> node1 != node)
                .filter(node1 -> node1.getLineOfCode() != 0)
                .filter(node1 -> node1.getLineOfCode() < node.getLineOfCode())
                .collect(Collectors.toList());
    }

    /**
     * 利用行号甄别当前node之后的兄弟节点
     */
    public List<PDNode> find_succeed_siblings(PDNode node) {
        PDNode first_level_par = graph.copyIncomingEdges(node).stream()
                .map(edge -> edge.source)
                .findFirst().orElse(null);
        assert first_level_par != null;
        return graph.copyOutgoingEdges(first_level_par).stream()
                .map(edge -> edge.target)
                .filter(node1 -> node1 != node)
                .filter(node1 -> node1.getLineOfCode() != 0)
                .filter(node1 -> node1.getLineOfCode() > node.getLineOfCode())
                .collect(Collectors.toList());
    }
}
