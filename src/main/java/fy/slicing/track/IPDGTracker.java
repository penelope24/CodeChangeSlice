package fy.slicing.track;

import fy.commit.repr.AtomEdit;
import fy.progex.graphs.IPDG;
import fy.slicing.repr.SliceGraph;
import fy.slicing.repr.SliceSubGraph;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IPDGTracker {

    public static SliceGraph track(IPDG ipdg, RevCommit commit, int type) {
        List<SliceSubGraph> subGraphs = new ArrayList<>();
        ipdg.pdgInfoList.forEach(pdgInfo -> {
            pdgInfo.atomEdits.forEach(atomEdit -> {
                if (!atomEdit.editLines.isEmpty()) {
                    SliceSubGraph subGraph = AtomEditTracker.track(atomEdit);
                    subGraphs.add(subGraph);
                }
            });
        });
        SliceGraph sliceGraph = new SliceGraph(subGraphs, commit, type);
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
