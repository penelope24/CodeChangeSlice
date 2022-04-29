package fy.slicing.repr;

import fy.progex.parse.PDGInfo;
import fy.slicing.result.CFGTrackResult;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

public class SlicePath extends SliceSubGraph{
    StringBuilder pathRecordWithEdge;
    StringBuilder pathRecordWithoutEdge;
    int pathNum;

    public SlicePath(PDGInfo pdgInfo, CFGTrackResult<PDNode, DDEdge, CFNode, CFEdge> result) {
        super(pdgInfo, result);
    }

    public void setPathRecordWithEdge(StringBuilder pathRecordWithEdge) {
        this.pathRecordWithEdge = pathRecordWithEdge;
    }

    public void setPathRecordWithoutEdge(StringBuilder pathRecordWithoutEdge) {
        this.pathRecordWithoutEdge = pathRecordWithoutEdge;
    }

    public void setPathNum(int pathNum) {
        this.pathNum = pathNum;
    }
}
