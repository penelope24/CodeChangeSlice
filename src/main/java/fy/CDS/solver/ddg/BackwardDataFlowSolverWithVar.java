package fy.CDS.solver.ddg;

import fy.CDS.result.DDGTrackResult;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 从start node开始向上追溯，只考虑start node中指定的变量
 */
public class BackwardDataFlowSolverWithVar extends DataFlowSolver {
    private Deque<Edge<PDNode, DDEdge>> visiting = new ArrayDeque<>();
    private Deque<Edge<PDNode, DDEdge>> visited= new ArrayDeque<>();
    DDGTrackResult<PDNode,DDEdge> result = new DDGTrackResult<>();

    public BackwardDataFlowSolverWithVar(DataDependenceGraph graph) {
        super(graph);
    }

    public void track(PDNode startNode, String var) {
        // find instant data bind nodes
        Set<Edge<PDNode, DDEdge>> instantDataBindEdges = graph.copyIncomingEdges(startNode).stream()
                .filter(edge -> edge.label.var.equals(var))
                .collect(Collectors.toSet());
        result.addDataNode(startNode);
        // bfs
        visiting.addAll(instantDataBindEdges);
        while (!visiting.isEmpty()) {
            Edge<PDNode, DDEdge> curEdge = visiting.pop();
            PDNode curNode = curEdge.source;
            result.addDataNode(curNode);
            if (curEdge.target != null)
                result.addDataFlowEdge(curEdge);
            // populating by data flow
            if (visited.add(curEdge) && graph.getInDegree(curNode) > 0) {
                graph.copyIncomingEdges(curNode).forEach(edge -> {
                    if (!visited.contains(edge)) {
                        visiting.add(edge);
                    }
                });
            }
        }
    }

    public DDGTrackResult<PDNode,DDEdge> getResult() {
        return result;
    }
}
