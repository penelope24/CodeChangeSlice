package fy.CDS;

import com.github.javaparser.ast.CompilationUnit;
import fy.CDS.data.EditLinesManager;
import fy.CDS.data.Slice;
import fy.CDS.data.SliceManager;
import fy.CDS.export.DotExporter;
import fy.CDS.result.CDGTrackResult;
import fy.CDS.result.DDGTrackResult;
import fy.CDS.result.PaletteResult;
import fy.CDS.track.CDGTracker;
import fy.CDS.track.CFGTracker;
import fy.CDS.track.DDGTracker;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;
import org.eclipse.jgit.diff.Edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeDiffSlicer {

    /**
     * 一段edit有可能由横跨多个不同方法的行组成，
     * 为了保证每个slice都有完整的entry / exit node，
     * 需要分析该edit在每个方法上的分量，并对每个分量生成slice，
     * 因此返回list of slices
     * */
    public static List<Slice> slice (PDGInfo pdgInfo, List<Integer> chLines, Edit.Type editType) {
        List<Slice> slices = new ArrayList<>();
        EditLinesManager editLinesManager = new EditLinesManager(pdgInfo, chLines);
        Map<CFNode, List<PDNode>> entry2startNodes = editLinesManager.entry2startNodes;
        for (Map.Entry<CFNode, List<PDNode>> entry : entry2startNodes.entrySet()) {
            CFNode node = entry.getKey();
            List<PDNode> pdNodes = entry.getValue();
            SliceManager manager = new SliceManager(pdgInfo, node, pdNodes, editType);
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
                    PaletteResult paletteResult = manager.setPalette();
                    if (manager.is_ready_for_palette()) {
                        Slice slice = new Slice(manager);
                        slice.setPaletteResult(paletteResult);
                        slices.add(slice);
                    } else {
                        throw new IllegalStateException("not ready for export");
                    }
                }
            }
        }
        return slices;
    }



}
