package fy.CDS.solver.ddg;

import fy.CDS.result.DDGTrackResult;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * 从start node开始向下追溯，考虑start node中所有的变量
 */
public class ForwardDataFlowSolver extends DataFlowSolver {

    private Deque<Edge<PDNode, DDEdge>> visiting = new ArrayDeque<>();
    private Deque<Edge<PDNode, DDEdge>> visited= new ArrayDeque<>();
    DDGTrackResult<PDNode,DDEdge> result = new DDGTrackResult<>();

    public ForwardDataFlowSolver(DataDependenceGraph graph) {
        super(graph);
    }

    public void track(PDNode startNode) {
        // find instant data bind nodes
        Set<Edge<PDNode, DDEdge>> instantDataBindEdges = graph.copyOutgoingEdges(startNode);
        result.addDataNode(startNode);
        // bfs
        visiting.addAll(instantDataBindEdges);
        while (!visiting.isEmpty()) {
            Edge<PDNode, DDEdge> curEdge = visiting.pop();
            PDNode curNode = curEdge.target;
            result.addDataNode(curNode);
            if (curEdge.source != null)
                result.addDataFlowEdge(curEdge);
            // populating by data flow
            if (visited.add(curEdge)) {
                graph.copyOutgoingEdges(curNode).forEach(edge -> {
                    if (!visited.contains(edge)) {
                        visiting.add(edge);
                    }
                });
            }
        }
    }

    public DDGTrackResult<PDNode, DDEdge> getResult() {
        return result;
    }
}
