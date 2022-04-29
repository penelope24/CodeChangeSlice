package fy.slicing.repr;

import fy.progex.parse.PDGInfo;
import fy.slicing.result.CFGTrackResult;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.AbstractProgramGraph;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.io.IOException;
import java.util.Set;

public class SliceSubGraph extends AbstractProgramGraph<CFNode, CFEdge> {
    public PDGInfo pdgInfo;
    Set<PDNode> dataNodes;
    Set<Edge<PDNode, DDEdge>> dataFLowEdges;

    public SliceSubGraph(PDGInfo pdgInfo, CFGTrackResult<PDNode,DDEdge,CFNode,CFEdge> result) {
        super();
        this.pdgInfo = pdgInfo;
        result.getResControlNodes().forEach(this::addVertex);
        result.getResControlFlowEdges().forEach(this::addEdge);
        dataNodes = result.getResDataNodes();
        dataFLowEdges = result.getResDataFlowEdges();
    }

    @Override
    public void exportDOT(String s) throws IOException {

    }

    @Override
    public void exportGML(String s) throws IOException {

    }

    @Override
    public void exportJSON(String s) throws IOException {

    }

    public Set<PDNode> getDataNodes() {
        return dataNodes;
    }

    public Set<Edge<PDNode, DDEdge>> getDataFLowEdges() {
        return dataFLowEdges;
    }
}
