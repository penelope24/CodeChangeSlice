package fy.slicing.result;

import ghaffarian.graphs.Edge;

import java.util.LinkedHashSet;
import java.util.Set;

public class DDGTrackResult<V, E> {
    Set<V> resDataNodes = new LinkedHashSet<>();
    Set<Edge<V,E>> resDataFlowEdges = new LinkedHashSet<>();

    public Set<V> getResDataNodes() {
        return resDataNodes;
    }

    public void setResDataNodes(Set<V> resDataNodes) {
        this.resDataNodes = resDataNodes;
    }

    public Set<Edge<V, E>> getResDataFlowEdges() {
        return resDataFlowEdges;
    }

    public void setResDataFlowEdges(Set<Edge<V, E>> resDataFlowEdges) {
        this.resDataFlowEdges = resDataFlowEdges;
    }

    public void addDataNode(V node ) {
        this.resDataNodes.add(node);
    }

    public void addDataNodes(Set<V> nodes) {
        this.resDataNodes.addAll(nodes);
    }

    public void addDataFlowEdge(Edge<V,E> edge) {
        this.resDataFlowEdges.add(edge);
    }

    public void addDataFLowEdges(Set<Edge<V,E>> edges) {
        this.resDataFlowEdges.addAll(edges);
    }

    public void addDDGTrackResult(DDGTrackResult<V, E> result) {
        this.resDataNodes.addAll(result.getResDataNodes());
        this.resDataFlowEdges.addAll(result.getResDataFlowEdges());
    }
}
