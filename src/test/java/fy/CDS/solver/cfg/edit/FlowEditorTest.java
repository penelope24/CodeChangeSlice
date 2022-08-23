package fy.CDS.solver.cfg.edit;

import fy.CDS.data.Slice;
import fy.CDS.data.SliceManager;
import fy.CDS.export.DotExporter;
import fy.CDS.result.CDGTrackResult;
import fy.CDS.result.DDGTrackResult;
import fy.CDS.result.PaletteResult;
import fy.CDS.track.CDGTracker;
import fy.CDS.track.CFGTracker;
import fy.CDS.track.DDGTracker;
import fy.PROGEX.build.MyPDGBuilder;
import fy.PROGEX.parse.PDGInfo;
import fy.PROGEX.parse.PDGInfoParser;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.diff.Edit;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

class FlowEditorTest {
    String javaFile = "/Users/fy/Documents/MyProjects/CodeChangeCases/reproduce/rep1/fixed.java";
    String output = "/Users/fy/Documents/MyProjects/CodeChangeCases/reproduce/rep1";

    @Test
    void test() throws IOException {
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(javaFile));
        int start = 53;
        PDNode startNode = graph.DDS.copyVertexSet().stream()
                .filter(node -> node.getLineOfCode() == start)
                .findFirst().get();
        int entry = 38;
        PDNode entryNode = graph.DDS.copyVertexSet().stream()
                .filter(node -> node.getLineOfCode() == entry)
                .findFirst().get();
        PDGInfo pdgInfo = new PDGInfo(graph);
        PDGInfoParser.parse(pdgInfo);
        CFNode entryCFNode = pdgInfo.findCFNodeByDDNode(entryNode);
        SliceManager sliceManager = new SliceManager(pdgInfo, entryCFNode, Arrays.asList(startNode), Edit.Type.INSERT);
        DDGTrackResult<PDNode, DDEdge> ddgTrackResult = DDGTracker.track(pdgInfo, startNode);
        sliceManager.updateAfterDDGTrack(ddgTrackResult);
        CDGTrackResult<PDNode> cdgTrackResult = CDGTracker.track(pdgInfo, ddgTrackResult);
        sliceManager.updateAfterCDGTrack(cdgTrackResult);
        sliceManager.updateBeforeCFGTrack();
        CFGTracker cfgTracker = new CFGTracker(sliceManager);
        cfgTracker.track();
        sliceManager.updateAfterCFGTrack();
        PaletteResult palette = sliceManager.setPalette();
        if (sliceManager.is_ready_for_palette()) {
            Slice slice = new Slice(sliceManager);
            slice.setPaletteResult(palette);
            DotExporter.exportDot(slice, output + "/test.dot");
        }
    }

    @Test
    void single_node_reproduce() {
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(javaFile));
        PDGInfo pdgInfo = new PDGInfo(graph);
        PDGInfoParser.parse(pdgInfo);
        CFNode target = graph.DDS.getCFG().copyVertexSet().stream()
                .filter(node -> node.getLineOfCode() == 48)
                .findFirst().get();
        System.out.println(target);
        SliceManager sliceManager = new SliceManager(pdgInfo, null, new LinkedList<>(), null);
        IfNodeFlowEditor editor = new IfNodeFlowEditor(pdgInfo, new HashSet<>(), sliceManager, target);
        editor.setTest(true);
        editor.parse();
        editor.edit();
        PaletteResult paletteResult = sliceManager.setPalette();
        Slice rep = new Slice(sliceManager);
        rep.setPaletteResult(paletteResult);
        DotExporter.exportDot(rep, output + "/rep.dot");
    }

    @Test
    void generateDot() throws IOException {
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(javaFile));
        graph.DDS.exportDOT(output);
        graph.DDS.getCFG().exportDOT(output);
        graph.CDS.exportDOT(output);
    }

}