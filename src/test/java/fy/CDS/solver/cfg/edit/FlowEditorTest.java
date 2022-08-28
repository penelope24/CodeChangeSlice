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
import java.util.stream.Collectors;

class FlowEditorTest {
    String javaFile = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs/bug1/buggy.java";
    String output = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs/bug1";


    @Test
    void test_rep_original_node() {
        int tgt_line = 67;
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(javaFile));
        PDGInfo pdgInfo = new PDGInfo(graph);
        PDGInfoParser.parse(pdgInfo);
        CFNode targetNode = graph.DDS.getCFG().copyVertexSet().stream()
                .filter(node -> node.getLineOfCode() == tgt_line)
                .findFirst().get();
        System.out.println("tgt node: " + targetNode);
        Set<CFNode> worklist = pdgInfo.cfg.copyVertexSet().stream()
                .filter(cfNode -> cfNode.getLineOfCode() >= 39)
                .filter(cfNode -> cfNode.getLineOfCode() <= 74)
                .collect(Collectors.toSet());
        CFGTracker tracker = new CFGTracker(pdgInfo);
        FlowEditor editor = tracker.parse(targetNode, worklist);
        editor.edit();
        Slice res = editor.getTmpFlowEditResult();
        DotExporter.exportDotTest(res, output + "/rep_node_" + tgt_line + ".dot");
    }

    @Test
    void generateDot() throws IOException {
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(javaFile));
        graph.DDS.exportDOT(output);
        graph.DDS.getCFG().exportDOT(output);
        graph.CDS.exportDOT(output);
    }

}