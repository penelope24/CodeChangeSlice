package fy.CDS.track;

import fy.CDS.result.CDGTrackResult;
import fy.CDS.result.DDGTrackResult;
import fy.CDS.solver.cdg.ControlBindNodeSolver;
import fy.CDS.solver.cdg.ControlBindNodeSolverWithVar;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.Set;
import java.util.stream.Collectors;

public class CDGTracker {

    public static CDGTrackResult<PDNode> track (PDGInfo pdgInfo, DDGTrackResult<PDNode, DDEdge> ddgTrackResult) {
        CDGTrackResult<PDNode> result = new CDGTrackResult<>();
        Set<PDNode> dataBindingNodes = ddgTrackResult.getResDataNodes().stream()
                .map(pdgInfo::findCDNode)
                .collect(Collectors.toSet());
        for (PDNode node : dataBindingNodes) {
            ControlBindNodeSolver solver = new ControlBindNodeSolver(pdgInfo.cdg, node, 3);
            solver.track();
            result.addControlBindingNodes(solver.getControlBindingNodes());
        }
        return result;
    }

    public static CDGTrackResult<PDNode> track (PDGInfo pdgInfo, DDGTrackResult<PDNode, DDEdge> ddgTrackResult, String var) {
        CDGTrackResult<PDNode> result = new CDGTrackResult<>();
        Set<PDNode> dataBindingNodes = ddgTrackResult.getResDataNodes().stream()
                .map(pdgInfo::findCDNode)
                .collect(Collectors.toSet());
        for (PDNode node : dataBindingNodes) {
            ControlBindNodeSolverWithVar solver = new ControlBindNodeSolverWithVar(pdgInfo.cdg, node, 3, var);
            solver.track();
            result.addControlBindingNodes(solver.getControlBindingNodes());
        }
        return result;
    }
}
