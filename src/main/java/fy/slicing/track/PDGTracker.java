package fy.slicing.track;

import fy.progex.parse.PDGInfo;
import fy.slicing.repr.SliceSubGraph;
import fy.slicing.result.CDGTrackResult;
import fy.slicing.result.CFGTrackResult;
import fy.slicing.result.DDGTrackResult;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.ArrayList;
import java.util.List;

public class PDGTracker {

    public static SliceSubGraph track(PDGInfo pdgInfo, List<Integer> editLines) {
        List<PDNode> startNodes = new ArrayList<>();
        editLines.forEach(line -> {
            PDNode startNode = pdgInfo.ddg.copyVertexSet().stream()
                    .filter(node -> node.getLineOfCode() == line)
                    .findFirst().orElse(null);
            if (startNode != null) {
                startNode.setProperty("start", true);
                startNodes.add(startNode);
            }
        });
        DDGTrackResult<PDNode, DDEdge> ddgTrackResult =
                DDGTracker.track(pdgInfo, startNodes);
        CDGTrackResult<PDNode> cdgTrackResult = CDGTracker.track(pdgInfo, ddgTrackResult);
        CFGTracker cfgTracker = new CFGTracker(pdgInfo, editLines, ddgTrackResult, cdgTrackResult);
        cfgTracker.track();
        return new SliceSubGraph(pdgInfo, cfgTracker.cfgTrackResult);
    }
}
