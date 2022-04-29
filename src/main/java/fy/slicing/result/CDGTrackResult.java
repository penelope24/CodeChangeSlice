package fy.slicing.result;

import java.util.LinkedHashSet;
import java.util.Set;

// TODO: 2022/4/29 extending AbstractProgramGraph
public class CDGTrackResult<V> {
    Set<V> controlBindingNodes= new LinkedHashSet<>();

    public Set<V> getControlBindingNodes() {
        return controlBindingNodes;
    }

    public void setControlBindingNodes(Set<V> controlBindingNodes) {
        this.controlBindingNodes = controlBindingNodes;
    }

    public void addControlBindingNode(V node) {
        this.controlBindingNodes.add(node);
    }

    public void addControlBindingNodes(Set<V> nodes) {
        this.controlBindingNodes.addAll(nodes);
    }
}
