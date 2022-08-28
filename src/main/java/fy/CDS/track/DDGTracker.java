package fy.CDS.track;

import fy.CDS.result.DDGTrackResult;
import fy.CDS.solver.ddg.BackwardDataFlowSolver;
import fy.CDS.solver.ddg.BackwardDataFlowSolverWithVar;
import fy.CDS.solver.ddg.ForwardDataFlowSolver;
import fy.CDS.solver.ddg.ForwardDataFlowSolverWithVar;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DDGTracker {

    public static DDGTrackResult<PDNode, DDEdge> track(PDGInfo pdgInfo, PDNode startNode) {
        DDGTrackResult<PDNode,DDEdge> result = new DDGTrackResult<>();
        // backward
        BackwardDataFlowSolver t1 = new BackwardDataFlowSolver(pdgInfo.ddg);
        t1.track(startNode);
        result.addDDGTrackResult(t1.getResult());
        // forward
        ForwardDataFlowSolver t2 = new ForwardDataFlowSolver(pdgInfo.ddg);
        t2.track(startNode);
        result.addDDGTrackResult(t2.getResult());
        return result;
    }

    public static DDGTrackResult<PDNode, DDEdge> track(PDGInfo pdgInfo, PDNode startNode, String var) {
        DDGTrackResult<PDNode,DDEdge> result = new DDGTrackResult<>();
        // backward
        BackwardDataFlowSolverWithVar t1 = new BackwardDataFlowSolverWithVar(pdgInfo.ddg);
        t1.track(startNode, var);
        // forward
        ForwardDataFlowSolverWithVar t2 = new ForwardDataFlowSolverWithVar(pdgInfo.ddg);
        t2.track(startNode, var);
        result.addDDGTrackResult(t2.getResult());
        return result;
    }

    public static DDGTrackResult<PDNode,DDEdge> track(PDGInfo pdgInfo, List<PDNode> startNodes) {
        DDGTrackResult<PDNode,DDEdge> result = new DDGTrackResult<>();
        for (PDNode startNode : startNodes) {
            // backward
            BackwardDataFlowSolver t1 = new BackwardDataFlowSolver(pdgInfo.ddg);
            t1.track(startNode);
            result.addDDGTrackResult(t1.getResult());
            // forward
            ForwardDataFlowSolver t2 = new ForwardDataFlowSolver(pdgInfo.ddg);
            t2.track(startNode);
            result.addDDGTrackResult(t2.getResult());
        }
        return result;
    }
}
