package fy.slicing.solver.cfg;

import fy.progex.parse.PDGInfo;
import org.graalvm.compiler.nodes.cfg.ControlFlowGraph;

public class ControlFlowSolver {
    public PDGInfo pdgInfo;

    public ControlFlowSolver(PDGInfo pdgInfo) {
        this.pdgInfo = pdgInfo;
    }
}
