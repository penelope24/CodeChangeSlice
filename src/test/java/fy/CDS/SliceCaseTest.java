package fy.CDS;

import fy.CDS.data.Slice;
import fy.CDS.export.DotExporter;
import fy.PROGEX.build.MyPDGBuilder;
import fy.PROGEX.parse.PDGInfo;
import fy.PROGEX.parse.PDGInfoParser;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SliceCaseTest {

    @Test
    void basic_case_1 () throws IOException {
        String javaFile = "/Users/fy/Documents/MyProjects/CodeChangeCases/slicing_cases/main/java/basic/case1.java";
        String output = "/Users/fy/Documents/MyProjects/CodeChangeCases/slicing_cases/main/java/basic/";
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(javaFile));
        PDGInfo pdgInfo = new PDGInfo(graph);
        PDGInfoParser.parse(pdgInfo);
        List<Integer> chLines = Arrays.asList(20);
        List<Slice> slices = CodeDiffSlicer.slice(pdgInfo, chLines, null);
        Slice slice = slices.get(0);
        DotExporter.exportDot(slice, output + "case1.dot");
//        graph.DDS.exportDOT(output);
//        graph.CDS.exportDOT(output);
    }

    @Test
    void nested_if_cdg() {
        String f = "/Users/fy/Documents/MyProjects/CodeChangeCases/slicing_cases/main/java/nested_cases/NestedIFCDGCase.java";
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(f));
        System.out.println(graph);
    }
}
