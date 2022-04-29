package fy.slicing.track;

import fy.progex.parse.PDGInfo;
import fy.slicing.repr.SliceGraph;
import fy.slicing.repr.SliceSubGraph;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InterPDGTracker {

    public static SliceGraph track(List<SliceSubGraph> subGraphs) {
        SliceGraph sliceGraph = new SliceGraph(subGraphs);
        subGraphs.forEach(sliceSubGraph -> {
            String path = sliceSubGraph.pdgInfo.abs_path;
            Map<CFNode,CFNode> callMap = sliceSubGraph.pdgInfo.callingMap;
            sliceSubGraph.copyVertexSet().forEach(node -> {
                if (callMap.containsKey(node)) {
                    CFNode callee = callMap.get(node);
                    sliceGraph.addEdge(node, callee);
                    for (CFNode exitNode : (ArrayList<CFNode>) callee.getProperty("exits")) {
                        sliceGraph.addEdge(new Edge<>(exitNode, new CFEdge(CFEdge.Type.RETURN), node));
                    }
                }
            });
        });
        return sliceGraph;
    }
}
