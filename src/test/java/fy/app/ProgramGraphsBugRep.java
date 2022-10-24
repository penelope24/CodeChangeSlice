package fy.app;

import fy.CDS.CFGPathSlicer;
import fy.CDS.CodeDiffSlicer;
import fy.CDS.data.Slice;
import fy.CDS.export.DotExporter;
import fy.PROGEX.build.MyPDGBuilder;
import fy.PROGEX.parse.PDGInfo;
import fy.PROGEX.parse.PDGInfoParser;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgramGraphsBugRep {

    static String base = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs";

    @Test
    void bug1() {
        String f1 = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs/bug1/buggy.java";
        String f2 = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs/bug1/fixed.java";
        String output = base + "/" + "bug1";
        ProgramDependeceGraph graph1 = MyPDGBuilder.build(new File(f1));
        PDGInfo pdgInfo1 = new PDGInfo(graph1);
        PDGInfoParser.parse(pdgInfo1);
        ProgramDependeceGraph graph2 = MyPDGBuilder.build(new File(f2));
        PDGInfo pdgInfo2 = new PDGInfo(graph2);
        PDGInfoParser.parse(pdgInfo2);
        // hunk1
        {
            File outputFile = new File(output + "/hunk1");
            if (!outputFile.exists()) {
                outputFile.mkdir();
            }
            // v1
            Slice slice1 = CodeDiffSlicer.slice(pdgInfo1, 27, null, "startNode");
            CFGPathSlicer pathSlicer1 = new CFGPathSlicer(slice1);
            pathSlicer1.slice();
            Set<Slice> paths1 = pathSlicer1.getCfgPaths();
            assert slice1 != null;
            DotExporter.exportDot(slice1, outputFile.getAbsolutePath() + "/buggy.dot");
            AtomicInteger i = new AtomicInteger();
            paths1.forEach(p -> {
                p.setPaletteResult(slice1.paletteResult);
                DotExporter.exportDot(p, outputFile.getAbsolutePath() + "/buggy_path_" + i.getAndIncrement() + ".dot");
            });
            // v2
            Slice slice2 = CodeDiffSlicer.slice(pdgInfo2, 27, null, "brNode");
            CFGPathSlicer pathSlicer2 = new CFGPathSlicer(slice2);
            pathSlicer2.slice();
            Set<Slice> paths2 = pathSlicer2.getCfgPaths();
            assert slice2 != null;
            DotExporter.exportDot(slice2, outputFile.getAbsolutePath() + "/fixed.dot");
            AtomicInteger j = new AtomicInteger();
            paths2.forEach(p -> {
                p.setPaletteResult(slice2.paletteResult);
                DotExporter.exportDot(p, outputFile.getAbsolutePath() + "/fixed_path_" + j.getAndIncrement() + ".dot");
            });
        }

        // hunk2
        {
            File outputFile = new File(output + "/hunk2");
            if (!outputFile.exists()) {
                outputFile.mkdir();
            }
            // v1
            Slice slice1 = CodeDiffSlicer.slice(pdgInfo1, 30, null, "startNode");
            CFGPathSlicer pathSlicer1 = new CFGPathSlicer(slice1);
            pathSlicer1.slice();
            Set<Slice> paths1 = pathSlicer1.getCfgPaths();
            assert slice1 != null;
            DotExporter.exportDot(slice1, outputFile.getAbsolutePath() + "/buggy.dot");
            AtomicInteger i = new AtomicInteger();
            paths1.forEach(p -> {
                p.setPaletteResult(slice1.paletteResult);
                DotExporter.exportDot(p, outputFile.getAbsolutePath() + "/buggy_path_" + i.getAndIncrement() + ".dot");
            });
            // v2
            List<Integer> chLines = Arrays.asList(30, 31, 32, 33);
            Slice slice2 = CodeDiffSlicer.slice(pdgInfo2, chLines, null).get(0);
            CFGPathSlicer pathSlicer2 = new CFGPathSlicer(slice2);
            pathSlicer2.slice();
            Set<Slice> paths2 = pathSlicer2.getCfgPaths();
            assert slice2 != null;
            DotExporter.exportDot(slice2, outputFile.getAbsolutePath() + "/fixed.dot");
            AtomicInteger j = new AtomicInteger();
            paths2.forEach(p -> {
                p.setPaletteResult(slice2.paletteResult);
                DotExporter.exportDot(p, outputFile.getAbsolutePath() + "/fixed_path_" + j.getAndIncrement() + ".dot");
            });
        }

        // hunk3
        {
            File outputFile = new File(output + "/hunk3");
            if (!outputFile.exists()) {
                outputFile.mkdir();
            }
            // v1
            Slice slice1 = CodeDiffSlicer.slice(pdgInfo1, 36, null, "startNode");
            CFGPathSlicer pathSlicer1 = new CFGPathSlicer(slice1);
            pathSlicer1.slice();
            Set<Slice> paths1 = pathSlicer1.getCfgPaths();
            assert slice1 != null;
            DotExporter.exportDot(slice1, outputFile.getAbsolutePath() + "/buggy.dot");
            AtomicInteger i = new AtomicInteger();
            paths1.forEach(p -> {
                p.setPaletteResult(slice1.paletteResult);
                DotExporter.exportDot(p, outputFile.getAbsolutePath() + "/buggy_path_" + i.getAndIncrement() + ".dot");
            });
            // v2
            Slice slice2 = CodeDiffSlicer.slice(pdgInfo2, 39, null, "node");
            CFGPathSlicer pathSlicer2 = new CFGPathSlicer(slice2);
            pathSlicer2.slice();
            Set<Slice> paths2 = pathSlicer2.getCfgPaths();
            assert slice2 != null;
            DotExporter.exportDot(slice2, outputFile.getAbsolutePath() + "/fixed.dot");
            AtomicInteger j = new AtomicInteger();
            paths2.forEach(p -> {
                p.setPaletteResult(slice2.paletteResult);
                DotExporter.exportDot(p, outputFile.getAbsolutePath() + "/fixed_path_" + j.getAndIncrement() + ".dot");
            });
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
