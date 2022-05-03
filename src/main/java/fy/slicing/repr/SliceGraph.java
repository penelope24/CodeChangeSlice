package fy.slicing.repr;

import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.AbstractProgramGraph;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class SliceGraph extends AbstractProgramGraph<CFNode, CFEdge> {

    List<SliceSubGraph> sliceSubGraphList;
    Set<PDNode> dataNodes;
    Set<Edge<PDNode, DDEdge>> dataFlowEdges;

    public SliceGraph(List<SliceSubGraph> sliceSubGraphList) {
        super();
        this.sliceSubGraphList = sliceSubGraphList;
        sliceSubGraphList.forEach(sliceSubGraph -> {
            this.addGraph(sliceSubGraph);
            dataNodes.addAll(sliceSubGraph.getDataNodes());
            dataFlowEdges.addAll(sliceSubGraph.getDataFLowEdges());
        });
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
}
