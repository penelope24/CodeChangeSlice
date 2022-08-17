package fy.CDS.solver.cdg;

import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ControlBindNodeSolver extends ControlDepSolver{
    private PDNode startNode;
    private int level_limit;
    private Set<String> varUseNames;
    private Set<String> varDefNames;
    Set<PDNode> controlBindingNodes = new LinkedHashSet<>();

    public ControlBindNodeSolver(ControlDependenceGraph graph, PDNode startNode, int level_limit) {
        super(graph);
        this.startNode = startNode;
        this.level_limit = level_limit;
        this.varUseNames = Arrays.stream(startNode.getAllUSEs()).collect(Collectors.toSet());
        this.varDefNames = Arrays.stream(startNode.getAllDEFs()).collect(Collectors.toSet());
    }

    public void track() {
        ParentSolver parentSolver = new ParentSolver(graph);
        // parent nodes at all levels
        PDNode instant_par = parentSolver.find_first_par(startNode);
        Set<PDNode> allParentBrNodes = parentSolver.find_all_par_with_limit(startNode, level_limit);
        allParentBrNodes.forEach(brNode -> {
            if (is_binding(brNode) || brNode == instant_par) {
                    controlBindingNodes.add(brNode);
            }
        });
    }

    private boolean is_binding(PDNode brNode) {
        SiblingSolver siblingSolver = new SiblingSolver(graph);
        List<PDNode> predSiblings = siblingSolver.find_pred_siblings(brNode);
        return predSiblings.stream().anyMatch(node -> {
            Set<String> allDefs = Arrays.stream(node.getAllDEFs()).collect(Collectors.toSet());
            return has_intersection(allDefs, this.varUseNames);
        });
    }

    private boolean has_intersection(Set s1, Set s2) {
        s1.retainAll(s2);
        return !s1.isEmpty();
    }

    public Set<PDNode> getControlBindingNodes() {
        return controlBindingNodes;
    }
}
