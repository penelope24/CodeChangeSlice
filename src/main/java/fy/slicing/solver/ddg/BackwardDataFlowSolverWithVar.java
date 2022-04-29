package fy.slicing.solver.ddg;

import fy.slicing.result.DDGTrackResult;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

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
        // bfs
        Edge<PDNode, DDEdge> dummy = new Edge<>(startNode, null, null);
        visiting.add(dummy);
        while (!visiting.isEmpty()) {
            Edge<PDNode, DDEdge> curEdge = visiting.pop();
            PDNode curNode = curEdge.source;
            result.addDataNode(curNode);
            if (curEdge.target != null)
                result.addDataFlowEdge(curEdge);
            // populating by data flow
            if (visited.add(curEdge)) {
                graph.copyIncomingEdges(curNode).forEach(edge -> {
                    List<String> allDefs = Arrays.asList(edge.source.getAllDEFs());
                    if (!visited.contains(edge) && allDefs.contains(var)) {
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
