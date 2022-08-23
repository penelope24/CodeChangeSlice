package fy.CDS;

import fy.CDS.data.Slice;
import fy.CDS.export.DotExporter;
import fy.GW.GitWalker;
import fy.GW.data.CommitDiff;
import fy.GW.data.FileDiff;
import fy.PROGEX.build.MyPDGBuilder;
import fy.PROGEX.parse.PDGInfo;
import fy.PROGEX.parse.PDGInfoParser;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Defects4JTest {

//    @Test
//    void bug1() {
//        String base = "/Users/fy/Documents/MyProjects/CCS_cases/benchmarks/defects4j/bug1";
//        String output = base;
//        // v1
//        {
//            String fileName = base + "/buggy.java";
//            ProgramDependeceGraph graph = MyPDGBuilder.build(new File(fileName));
//            PDGInfo pdgInfo = new PDGInfo(graph);
//            PDGInfoParser.parse(pdgInfo);
//            List<Integer> chLines = Collections.singletonList(1797);
//            List<Slice> slices = CodeDiffSlicer.slice(pdgInfo, chLines, null);
//            Slice slice = slices.get(0);
//            DotExporter.exportDot(slice, output + "/buggy.dot");
//        }
//        // v2
//        {
//            String fileName = base + "/fixed.java";
//            ProgramDependeceGraph graph = MyPDGBuilder.build(new File(fileName));
//            PDGInfo pdgInfo = new PDGInfo(graph);
//            PDGInfoParser.parse(pdgInfo);
//            List<Integer> chLines = Collections.singletonList(1797);
//            List<Slice> slices = CodeDiffSlicer.slice(pdgInfo, chLines, null);
//            Slice slice = slices.get(0);
//            DotExporter.exportDot(slice, output + "/fixed.dot");
//        }
//    }
    private void rep(String v, int index) throws IOException, GitAPIException {
        String projectPath = "/Users/fy/Documents/opensource/defects4j-presentation-urls";
        String output = "/Users/fy/Documents/MyProjects/CodeChangeCases/benchmark/defects4j/" + "bug" + index;
        File outputFile = new File(output);
        if (!outputFile.exists()) {
            outputFile.mkdir();
        }
        GitWalker walker = new GitWalker(projectPath);
        RevCommit commit = walker.getAllCommits().stream()
                .filter(revCommit -> revCommit.getId().name().equals(v))
                .findFirst().get();
        CommitDiff commitDiff = walker.solve(commit);
        CommitDiffProcessor processor = new CommitDiffProcessor(commitDiff, output);
        processor.process();
    }

    @Test
    void bug1() throws IOException, GitAPIException {
        String v = "b77fa155b26e1defad407ab75aaf3aa70aa73b14";
        rep(v, 1);
    }

    @Test
    void bug2() throws Exception {
        String projectPath = "/Users/fy/Documents/opensource/defects4j-presentation-urls";
        String output = "/Users/fy/Documents/MyProjects/CodeChangeCases/benchmark/defects4j/bug2";
        String v = "4dfdbc80adf431848867db2b644c8b1627b19aff";
        GitWalker walker = new GitWalker(projectPath);
        RevCommit commit = walker.getAllCommits().stream()
                .filter(revCommit -> revCommit.getId().name().equals(v))
                .findFirst().get();
        CommitDiff commitDiff = walker.solve(commit);
        CommitDiffProcessor processor = new CommitDiffProcessor(commitDiff, output);
        processor.process();
//        System.out.println(commitDiff.fileDiffs.get(0).edits.size());
    }

    @Test
    void bug3 () throws IOException, GitAPIException {
        String v = "af970e140be03ac4074ef5b822dd4f19ac3934fb";
        rep(v, 3);
    }

    @Test
    void bug4() throws IOException, GitAPIException {
        String v = "11dbd2fe88929f54998c81dbbd8d2604b08b6ff6";
        rep(v, 4);
    }

    @Test
    void bug5 () throws IOException, GitAPIException {
        String v = "195d7d5af81315e79af52ee52ba904d04ce4587e";
        rep(v, 5);
    }
}
