package fy.app;

import fy.CDS.CodeDiffSlicer;
import fy.CDS.CommitDiffProcessor;
import fy.CDS.data.Slice;
import fy.CDS.export.DotExporter;
import fy.GW.GitWalker;
import fy.GW.data.CommitDiff;
import fy.PROGEX.build.MyPDGBuilder;
import fy.PROGEX.parse.PDGInfo;
import fy.PROGEX.parse.PDGInfoParser;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class BugRep {

    public static void rep1 (GitWalker walker, String v, int index, String base) {
        String output = base + "/" + "bug" + index;
        File outputFile = new File(output);
        if (!outputFile.exists()) {
            outputFile.mkdir();
        }
        RevCommit commit = walker.getAllCommits().stream()
                .filter(revCommit -> revCommit.getId().name().equals(v))
                .findFirst().get();
        CommitDiff commitDiff = walker.solve(commit);
        CommitDiffProcessor processor = new CommitDiffProcessor(commitDiff, output);
        try {
            processor.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rep2(String f1, String f2, List<Integer> ll1, List<Integer> ll2, String base) {
        // v1
        {
            ProgramDependeceGraph graph = MyPDGBuilder.build(new File(f1));
            PDGInfo pdgInfo = new PDGInfo(graph);
            PDGInfoParser.parse(pdgInfo);
            List<Slice> slices = CodeDiffSlicer.slice(pdgInfo, ll1, null);
            Slice slice = slices.get(0);
            DotExporter.exportDot(slice, base + "/buggy.dot");
        }
//        // v2
//        {
//            ProgramDependeceGraph graph = MyPDGBuilder.build(new File(f2));
//            PDGInfo pdgInfo = new PDGInfo(graph);
//            PDGInfoParser.parse(pdgInfo);
//            List<Slice> slices = CodeDiffSlicer.slice(pdgInfo, ll2, null);
//            Slice slice = slices.get(0);
//            DotExporter.exportDot(slice, base + "/fixed.dot");
//        }
    }
}
