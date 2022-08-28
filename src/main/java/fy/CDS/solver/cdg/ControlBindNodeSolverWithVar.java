package fy.CDS.solver.cdg;

import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ControlBindNodeSolverWithVar extends ControlBindNodeSolver{

    String var;

    public ControlBindNodeSolverWithVar(ControlDependenceGraph graph, PDNode startNode, int level_limit, String var) {
        super(graph, startNode, level_limit);
        this.var = var;
    }

    @Override
    public boolean is_binding(PDNode brNode) {
        SiblingSolver siblingSolver = new SiblingSolver(graph);
        List<PDNode> predSiblings = siblingSolver.find_pred_siblings(brNode);
        return predSiblings.stream()
                .anyMatch(node -> Arrays.asList(node.getAllDEFs()).contains(var)
        );
    }
}
