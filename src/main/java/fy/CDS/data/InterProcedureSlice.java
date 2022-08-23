package fy.CDS.data;

import fy.CDS.export.DotPalette;
import ghaffarian.graphs.Edge;
import ghaffarian.nanologger.Logger;
import ghaffarian.progex.graphs.AbstractProgramGraph;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.utils.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class InterProcedureSlice extends AbstractProgramGraph<CFNode, CFEdge> {

    public List<Slice> slices;
    public Set<PDNode> dataNodes = new HashSet<>();
    public Set<Edge<PDNode, DDEdge>> dataFlowEdges = new HashSet<>();

    public InterProcedureSlice(List<Slice> slices) {
        this.slices = slices;
        slices.forEach(this::add);
    }

    public void add(Slice slice) {
        this.addGraph(slice);
        this.dataNodes.addAll(slice.dataNodes);
        this.dataFlowEdges.addAll(slice.dataFlowEdges);
    }

    @Override
    public void exportDOT(String dotFileName) throws IOException {
    }

    @Override
    public void exportGML(String s) throws IOException {

    }

    @Override
    public void exportJSON(String s) throws IOException {

    }
}
