package fy.CDS.data;

import fy.CDS.export.DotPalette;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.nanologger.Logger;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.utils.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SliceSubPath extends Slice {
    public SliceManager sliceManager;
    public PDGInfo pdgInfo;


    public SliceSubPath(SliceManager sliceManager) {
        super(sliceManager);
    }


    @Override
    public void exportDOT(String dotFileName) throws IOException {

    }

}
