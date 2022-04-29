package fy.slicing.solver.cdg;

import fy.slicing.solver.Solver;
import ghaffarian.progex.graphs.pdg.CDEdge;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

/**
 * 在CDG上的解析通常只涉及一层，不涉及堆栈
 * 只有解析所有parent/children的时候需要提供辅助栈
 */
public class ControlDepSolver implements Solver<PDNode, CDEdge> {
    public ControlDependenceGraph graph;

    public ControlDepSolver(ControlDependenceGraph graph) {
        this.graph = graph;
    }
}
