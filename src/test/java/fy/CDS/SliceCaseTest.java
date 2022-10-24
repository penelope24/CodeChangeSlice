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
import java.util.concurrent.atomic.AtomicInteger;

public class SliceCaseTest {

    @Test
    void basic_case_1 () throws IOException {
        String javaFile = "/Users/fy/Documents/MyProjects/CodeChangeCases/slicing_cases/main/java/basic/PaperExample2.java";
        String output = "/Users/fy/Documents/MyProjects/CodeChangeCases/slicing_cases/main/java/basic/";
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(javaFile));
        PDGInfo pdgInfo = new PDGInfo(graph);
        PDGInfoParser.parse(pdgInfo);
        List<Integer> chLines = Arrays.asList(13);
        List<Slice> slices = CodeDiffSlicer.slice(pdgInfo, chLines, null);
        Slice slice = slices.get(0);
        DotExporter.exportDot(slice, output + "paperExample2.dot");
        graph.DDS.exportDOT(output);
        graph.CDS.exportDOT(output);
        CFGPathSlicer pathSlicer = new CFGPathSlicer(slice);
        pathSlicer.slice();
        AtomicInteger k = new AtomicInteger();
        System.out.println(pathSlicer.cfgPaths.size());
        pathSlicer.cfgPaths.forEach(p -> {
            p.setPaletteResult(slice.paletteResult);
            DotExporter.exportDot(p, output + "/path_" + k.getAndIncrement() + ".dot");
        });
    }

    @Test
    void nested_if_cdg() {
        String f = "/Users/fy/Documents/MyProjects/CodeChangeCases/slicing_cases/main/java/nested_cases/NestedIFCDGCase.java";
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(f));
        System.out.println(graph);
    }
}
