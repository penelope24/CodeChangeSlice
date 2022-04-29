package fy.slicing.solver.cdg;

import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.pdg.CDEdge;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class ChildNodeSolver extends ControlDepSolver {

    public ChildNodeSolver(ControlDependenceGraph graph) {
        super(graph);
    }

    private List<PDNode> find_first_level_children(Edge<PDNode, CDEdge> branch) {
        Deque<Edge<PDNode, CDEdge>> visiting = new ArrayDeque<>();
        visiting.add(branch);
        while (!visiting.isEmpty()) {
            int n = visiting.size();
            List<PDNode> level = new LinkedList<>();
            for (int i=0; i<n; i++) {
                Edge<PDNode, CDEdge> edge = visiting.pop();
                PDNode node = edge.target;
                if (node.getLineOfCode() != 0) {
                    level.add(node);
                }
                visiting.addAll(graph.copyOutgoingEdges(node));
            }
            if (!level.isEmpty()) {
                return level;
            }
        }
        return new LinkedList<>();
    }

    public List<PDNode> find_first_level_children(PDNode startNode) {
        List<PDNode> res = new LinkedList<>();
        graph.copyOutgoingEdges(startNode).forEach(edge -> {
            List<PDNode> brRes = find_first_level_children(edge);
            res.addAll(brRes);
        });
        return res;
    }

    public List<PDNode> find_first_level_children(PDNode startNode, CDEdge.Type edge_filter) {
        List<PDNode> res = new LinkedList<>();
        graph.copyOutgoingEdges(startNode).stream()
                .filter(edge -> edge.label.type == edge_filter)
                .forEach(edge -> {
                    List<PDNode> brRes = find_first_level_children(edge);
                    res.addAll(brRes);
                });
        return res;
    }

    public List<List<PDNode>> find_all_children_level_order(PDNode startNode) {
        Deque<Edge<PDNode, CDEdge>> visiting = new ArrayDeque<>();
        Edge<PDNode, CDEdge> dummy = new Edge<>(null, null, startNode);
        List<List<PDNode>> res = new LinkedList<>();
        visiting.add(dummy);
        while (!visiting.isEmpty()) {
            int n = visiting.size();
            List<PDNode> level = new LinkedList<>();
            for (int i=0; i<n; i++) {
                Edge<PDNode, CDEdge> edge = visiting.pop();
                PDNode node = edge.target;
                if (edge.source != null && node.getLineOfCode() != 0) {
                    level.add(node);
                }
                visiting.addAll(graph.copyOutgoingEdges(node));
            }
            if (!level.isEmpty()) {
                res.add(level);
            }
        }
        return res;
    }

    public List<List<PDNode>> find_all_children_level_order(PDNode startNode, int level_limit) {
        Deque<Edge<PDNode, CDEdge>> visiting = new ArrayDeque<>();
        Edge<PDNode, CDEdge> dummy = new Edge<>(null, null, startNode);
        List<List<PDNode>> res = new LinkedList<>();
        visiting.add(dummy);
        int level_count = 0;
        while (!visiting.isEmpty()) {
            if (level_count >= level_limit) {
                break;
            }
            int n = visiting.size();
            List<PDNode> level = new LinkedList<>();
            for (int i=0; i<n; i++) {
                Edge<PDNode, CDEdge> edge = visiting.pop();
                PDNode node = edge.target;
                if (edge.source != null && node.getLineOfCode() != 0) {
                    level.add(node);
                }
                visiting.addAll(graph.copyOutgoingEdges(node));
            }
            if (!level.isEmpty()) {
                res.add(level);
                level_count++;
            }
        }
        return res;
    }

    public List<PDNode> find_all_children(PDNode startNode) {
        List<PDNode> res = new LinkedList<>();
        Deque<Edge<PDNode, CDEdge>> visiting = new ArrayDeque<>();
        Edge<PDNode, CDEdge> dummy = new Edge<>(null, null, startNode);
        visiting.add(dummy);
        while (!visiting.isEmpty()) {
            Edge<PDNode, CDEdge> edge = visiting.pop();
            PDNode node = edge.target;
            if (edge.source != null && node.getLineOfCode() != 0) {
                res.add(node);
            }
            visiting.addAll(graph.copyOutgoingEdges(node));
        }
        return res;
    }
}
