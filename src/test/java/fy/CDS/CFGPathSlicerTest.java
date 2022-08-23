package fy.CDS;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import fy.CDS.data.Slice;
import fy.CDS.export.DotExporter;
import fy.PROGEX.build.MyPDGBuilder;
import fy.PROGEX.parse.PDGInfo;
import fy.PROGEX.parse.PDGInfoParser;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class CFGPathSlicerTest {
    String javaFile1 = "/Users/fy/Documents/MyProjects/CCS_cases/handmade/cases/case5/v1.java";
    String javaFile2 = "/Users/fy/Documents/MyProjects/CCS_cases/handmade/cases/case5/v2.java";
    String outDir = "/Users/fy/Documents/MyProjects/CCS_cases/handmade/cases/case5";

    @Test
    void test () throws GitAPIException, IOException {
        // v1
        ProgramDependeceGraph graph1 = MyPDGBuilder.build(new File(javaFile1));
        PDGInfo pdgInfo1 = new PDGInfo(graph1);
        PDGInfoParser.parse(pdgInfo1);
        List<Integer> chlines1 = Arrays.asList(24, 25, 26, 27);
        CompilationUnit cu1 = StaticJavaParser.parse(new File(javaFile1));
        List<Slice> slices1 = CodeDiffSlicer.slice(pdgInfo1, chlines1, null);
//        // v2
//        ProgramDependeceGraph graph2 = MyPDGBuilder.build(new File(javaFile2));
//        PDGInfo pdgInfo2 = new PDGInfo(graph2);
//        PDGInfoParser.parse(pdgInfo2);
//        List<Integer> chLines2 = Arrays.asList(28, 29, 30);
//        CompilationUnit cu2 = StaticJavaParser.parse(new File(javaFile2));
//        List<Slice> slices2 = CodeDiffSlicer.slice(pdgInfo2, chLines2, null, cu2);
        Slice slice = slices1.get(0);
        CFGPathSlicer pathSlicer = new CFGPathSlicer(slice);
        pathSlicer.slice();
        AtomicInteger i = new AtomicInteger();
        pathSlicer.cfgPaths.forEach(s -> {
            DotExporter.exportDot(s, outDir + "/path_" + i.getAndIncrement() + ".dot");
        });
    }
}