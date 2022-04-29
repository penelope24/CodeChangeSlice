package fy.slicing.result;

import ghaffarian.graphs.Edge;

import java.util.LinkedHashSet;
import java.util.Set;

public class CFGTrackResult<V1, E1, V2, E2> {
    Set<V1> resDataNodes = new LinkedHashSet<>();
    Set<Edge<V1,E1>> resDataFlowEdges = new LinkedHashSet<>();
    Set<V2> resControlNodes = new LinkedHashSet<>();
    Set<Edge<V2, E2>> resControlFlowEdges = new LinkedHashSet<>();

    public Set<V1> getResDataNodes() {
        return resDataNodes;
    }

    public void setResDataNodes(Set<V1> resDataNodes) {
        this.resDataNodes = resDataNodes;
    }

    public Set<Edge<V1, E1>> getResDataFlowEdges() {
        return resDataFlowEdges;
    }

    public void setResDataFlowEdges(Set<Edge<V1, E1>> resDataFlowEdges) {
        this.resDataFlowEdges = resDataFlowEdges;
    }

    public Set<V2> getResControlNodes() {
        return resControlNodes;
    }

    public void setResControlNodes(Set<V2> resControlNodes) {
        this.resControlNodes = resControlNodes;
    }

    public Set<Edge<V2, E2>> getResControlFlowEdges() {
        return resControlFlowEdges;
    }

    public void setResControlFlowEdges(Set<Edge<V2, E2>> resControlFlowEdges) {
        this.resControlFlowEdges = resControlFlowEdges;
    }

    public void addResDataNode(V1 node) {
        this.resDataNodes.add(node);
    }

    public void addResDataNodes(Set<V1> nodes) {
        this.resDataNodes.addAll(nodes);
    }

    public void addResDataFlowEdge(Edge<V1, E1> edge) {
        this.resDataFlowEdges.add(edge);
    }

    public void addResDataFlowEdges(Set<Edge<V1, E1>> edges) {
        this.resDataFlowEdges.addAll(edges);
    }

    public void addControlNode(V2 node) {
        this.resControlNodes.add(node);
    }

    public void addResControlNodes(Set<V2> nodes) {
        this.resControlNodes.addAll(nodes);
    }

    public void addResControlFlowEdge(Edge<V2, E2> edge) {
        this.resControlFlowEdges.add(edge);
    }

    public void addResControlFlowEdges(Set<Edge<V2, E2>> edges ) {
        this.resControlFlowEdges.addAll(edges);
    }
}
