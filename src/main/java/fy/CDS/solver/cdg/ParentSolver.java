package fy.CDS.solver.cdg;

import ghaffarian.graphs.Edge;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.pdg.CDEdge;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

public class ParentSolver extends ControlDepSolver {

    public ParentSolver(ControlDependenceGraph graph) {
        super(graph);
    }

    public PDNode find_first_par(PDNode start) {
        Deque<Edge<PDNode, CDEdge>> visiting = new ArrayDeque<>();
        Edge<PDNode, CDEdge> dummy = new Edge<>(start, null, null);
        visiting.add(dummy);
        while (!visiting.isEmpty()) {
            Edge<PDNode, CDEdge> edge = visiting.pop();
            PDNode node = edge.source;
            if (edge.target != null && is_valid_par(node)) {
                return node;
            }
            visiting.addAll(graph.copyIncomingEdges(node));
        }
        return null;
    }

    public PDNode find_first_par_loose(PDNode start) {
        Deque<Edge<PDNode, CDEdge>> visiting = new ArrayDeque<>();
        Edge<PDNode, CDEdge> dummy = new Edge<>(start, null, null);
        visiting.add(dummy);
        while (!visiting.isEmpty()) {
            Edge<PDNode, CDEdge> edge = visiting.pop();
            PDNode node = edge.source;
            if (edge.target != null && node.getLineOfCode() != 0) {
                return node;
            }
            visiting.addAll(graph.copyIncomingEdges(node));
        }
        return null;
    }

    public Set<PDNode> find_all_par(PDNode start) {
        Set<PDNode> res = new LinkedHashSet<>();
        Deque<Edge<PDNode, CDEdge>> visiting = new ArrayDeque<>();
        Edge<PDNode, CDEdge> dummy = new Edge<>(start, null, null);
        visiting.add(dummy);
        int par_count = 0;
        while (!visiting.isEmpty()) {
            Edge<PDNode, CDEdge> edge = visiting.pop();
            PDNode node = edge.source;
            if (node.getType() == NodeType.ROOT) {
                break;
            }
            if (edge.target != null && node.getLineOfCode() != 0) {
                res.add(node);
                par_count ++;
            }
            visiting.addAll(graph.copyIncomingEdges(node));
        }
        return res;
    }

    public Set<PDNode> find_all_par_with_limit(PDNode start, int level_limit) {
        Set<PDNode> res = new LinkedHashSet<>();
        Deque<Edge<PDNode, CDEdge>> visiting = new ArrayDeque<>();
        Edge<PDNode, CDEdge> dummy = new Edge<>(start, null, null);
        visiting.add(dummy);
        int par_count = 0;
        while (!visiting.isEmpty()) {
            Edge<PDNode, CDEdge> edge = visiting.pop();
            PDNode node = edge.source;
            if (node.getType() == NodeType.ROOT || par_count >= level_limit) {
                break;
            }
            if (edge.target != null && node.getLineOfCode() != 0) {
                res.add(node);
                par_count ++;
            }
            visiting.addAll(graph.copyIncomingEdges(node));
        }
        return res;
    }

    private boolean is_valid_par(PDNode par) {
        if (par.getLineOfCode() == 0) {
            return false;
        }
        // fixme type FINALLY
        if (par.getType() == null) {
            return false;
        }
        switch (par.getType()) {
            case HELP:
            case CATCH:
            case TRY:
            case CASE:
            case SYNCHRONIZED:
                return false;
            default:
        }
        return true;
    }
}
