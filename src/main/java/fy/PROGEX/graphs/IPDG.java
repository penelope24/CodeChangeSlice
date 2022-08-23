package fy.PROGEX.graphs;

import fy.CDS.data.Slice;
import fy.CDS.export.DotPalette;
import ghaffarian.graphs.Edge;
import ghaffarian.nanologger.Logger;
import ghaffarian.progex.graphs.AbstractProgramGraph;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.utils.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 *  inter-procedure program dependency graph
 */
public class IPDG extends AbstractProgramGraph<CFNode, CFEdge> {
    ControlFlowGraph icfg = new ControlFlowGraph("");
    List<Slice> slices;
    public Set<PDNode> dataNodes = new LinkedHashSet<>();
    public Set<Edge<PDNode, DDEdge>> dataFlowEdges = new LinkedHashSet<>();

    public IPDG(List<Slice> slices) {
        super();
        slices.forEach(slice -> {
            this.icfg.addGraph(slice.pdgInfo.cfg);
        });
        this.slices = slices;
        this.addGraph(this.icfg);
        for (Slice slice : slices) {
            DataDependenceGraph ddg = slice.pdgInfo.ddg;
            dataFlowEdges.addAll(ddg.copyEdgeSet());
            dataNodes.addAll(ddg.copyVertexSet());
        }
    }

    @Override
    public void exportDOT(String dotFilePath) throws IOException {

    }

    @Override
    public void exportGML(String s) throws IOException {

    }

    @Override
    public void exportJSON(String s) throws IOException {

    }

}
