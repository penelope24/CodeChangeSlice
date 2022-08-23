package fy.CDS;

import fy.GW.CommitParse;
import fy.GW.GitWalker;
import fy.GW.data.CommitDiff;
import fy.PROGEX.build.MyPDGBuilder;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VersionedGraphMather {

    String javaFile1 = "/Users/fy/Documents/MyProjects/CCS_cases/handmade/cases/case5/v1.java";
    String javaFile2 = "/Users/fy/Documents/MyProjects/CCS_cases/handmade/cases/case5/v2.java";
    List<List<Integer>> chLinesList1 = new ArrayList<>();
    List<List<Integer>> chLinesList2 = new ArrayList<>();
    String outDir = "/Users/fy/Documents/MyProjects/CCS_cases/handmade/cases/case5";

    @Test
    void test () throws GitAPIException, IOException {
        ProgramDependeceGraph graph1 = MyPDGBuilder.build(new File(javaFile1));
        ProgramDependeceGraph graph2 = MyPDGBuilder.build(new File(javaFile2));
        System.out.println(graph1.DDS.vertexCount());
        System.out.println(graph2.DDS.vertexCount());
        for (PDNode node : graph1.DDS.copyVertexSet()) {
            System.out.println(graph2.DDS.copyVertexSet().contains(node));
        }
    }
}
