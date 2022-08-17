package fy.CDS;

import com.github.javaparser.ast.CompilationUnit;
import fy.CDS.data.InterSliceManager;
import fy.CDS.data.Slice;
import fy.CDS.data.SliceManager;
import fy.CDS.result.CDGTrackResult;
import fy.CDS.result.DDGTrackResult;
import fy.CDS.track.*;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeDiffSlicer {

    // it is an comment
    public static Slice slice (PDGInfo pdgInfo, List<Integer> chLines, AbstractSyntaxTree ast, CompilationUnit cu) {
        Slice totalSlice = new Slice();
        List<Slice> slices = new ArrayList<>();
        InterSliceManager interSliceManager = new InterSliceManager(pdgInfo, chLines, cu);
        Map<CFNode, List<PDNode>> entry2startNodes = interSliceManager.entry2startNodes;
        entry2startNodes.forEach((node, pdNodes) -> {
            SliceManager manager = new SliceManager(pdgInfo, node, pdNodes);
            if (manager.is_valid_track) {
                // data bind track
                DDGTrackResult<PDNode, DDEdge> ddgTrackResult = DDGTracker.track(manager.pdgInfo, manager.startNodes);
                manager.updateAfterDDGTrack(ddgTrackResult);
                // control bind track
                CDGTrackResult<PDNode> cdgTrackResult = CDGTracker.track(manager.pdgInfo, ddgTrackResult);
                manager.updateAfterCDGTrack(cdgTrackResult);
                // cfg track
                manager.updateBeforeCFGTrack();
                if (manager.is_ready_for_slice()) {
                    CFGTracker cfgTracker = new CFGTracker(manager);
                    cfgTracker.track();
                    manager.updateAfterCFGTrack();
                    manager.setPalette();
                    Slice slice = manager.getSliceResult();
                    slices.add(slice);
                }
            }
        });
        // add to total slice
        slices.forEach(totalSlice::add);
        // add extra nodes
        slices.forEach(slice -> {
            CFNode entryNode = slice.sliceManager.entryNode;
            if (entryNode.getProperty("par_cls") != null) {
                CFNode parClsNode = (CFNode) entryNode.getProperty("par_cls");
                totalSlice.addVertex(parClsNode);
                totalSlice.addEdge(new Edge<>(parClsNode, new CFEdge(CFEdge.Type.CONTAIN), entryNode));
            }
        });
        return totalSlice;
    }



}
