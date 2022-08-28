package fy.app;

import fy.CDS.CodeDiffSlicer;
import fy.CDS.data.Slice;
import fy.CDS.export.DotExporter;
import fy.GW.GitWalker;
import fy.PROGEX.build.MyPDGBuilder;
import fy.PROGEX.parse.PDGInfo;
import fy.PROGEX.parse.PDGInfoParser;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProgramGraphsBugRep {

    static String base = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs";

    @Test
    void bug1() {
        String f1 = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs/bug1/buggy.java";
        String f2 = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs/bug1/fixed.java";
        List<Integer> ll1 = Collections.singletonList(65);
        List<Integer> ll2 = Collections.singletonList(69);
        // v1
        {
            ProgramDependeceGraph graph = MyPDGBuilder.build(new File(f1));
            PDGInfo pdgInfo = new PDGInfo(graph);
            PDGInfoParser.parse(pdgInfo);
            Slice slice = CodeDiffSlicer.slice(pdgInfo, 65, null,"brNode");
            DotExporter.exportDot(slice, base + "/buggy.dot");
        }
        // v2
        {
            ProgramDependeceGraph graph = MyPDGBuilder.build(new File(f2));
            PDGInfo pdgInfo = new PDGInfo(graph);
            PDGInfoParser.parse(pdgInfo);
            Slice slice = CodeDiffSlicer.slice(pdgInfo, 69, null,"node");
            DotExporter.exportDot(slice, base + "/fixed.dot");
        }
    }

    @Test
    void tmp() throws FileNotFoundException {
        String f1 = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs/bug1/buggy.java";
        String f2 = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs/bug1/fixed.java";
        ProgramDependeceGraph graph1 = MyPDGBuilder.build(new File(f1));
        ProgramDependeceGraph graph2 = MyPDGBuilder.build(new File(f2));
        graph1.DDS.getCFG().exportDOT(base);
        graph2.DDS.getCFG().exportDOT(base);
        graph1.CDS.exportDOT(base);
        graph2.CDS.exportDOT(base);
    }

}
