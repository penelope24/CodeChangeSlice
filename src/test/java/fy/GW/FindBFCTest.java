package fy.GW;

import com.github.javaparser.ast.CompilationUnit;
import fy.CDS.CodeDiffSlicer;
import fy.CDS.CommitDiffProcessor;
import fy.CDS.FileDiffProcessor;
import fy.CDS.data.Slice;
import fy.CDS.export.DotExporter;
import fy.GW.data.CommitDiff;
import fy.GW.data.FileDiff;
import fy.PROGEX.parse.PDGInfo;
import fy.PROGEX.parse.PDGInfoParser;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FindBFCTest {
    String output = "/Users/fy/Documents/MyProjects/CCS_cases/real_project/spring_oauth/bfc";

    @Test
    void find_bfc() throws GitAPIException, IOException {
        String projectPath = "/Users/fy/Downloads/spring-security-oauth";
        GitWalker walker = new GitWalker(projectPath);
        List<RevCommit> allBFCList = walker.allCommits.stream()
                .filter(CommitParse::is_positive)
                .collect(Collectors.toList());
        System.out.println(allBFCList.size());
        allBFCList.forEach(revCommit -> {
            System.out.println(revCommit);
            CommitDiff commitDiff = walker.solve(revCommit);
            if (commitDiff != null) {
                CommitDiffProcessor processor = new CommitDiffProcessor(commitDiff, output);
                try {
                    processor.process();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Test
    void reproduce() throws GitAPIException, IOException {
        String projectPath = "/Users/fy/Downloads/spring-security-oauth";
        String v = "2dc744bff4a2ebdfbf9eebdfc9d44fd84b4b4ccd";
        GitWalker walker = new GitWalker(projectPath);
        RevCommit commit = walker.allCommits.stream()
                .filter(revCommit -> revCommit.getId().name().equals(v))
                .findFirst().get();
        CommitDiff commitDiff = walker.solve(commit);
        if (commitDiff != null) {
            CommitDiffProcessor processor = new CommitDiffProcessor(commitDiff, output);
            try {
                processor.process();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void reproduce_atom() throws GitAPIException, IOException {
        String projectPath = "/Users/fy/Downloads/spring-security-oauth";
        String v = "2dc744bff4a2ebdfbf9eebdfc9d44fd84b4b4ccd";
        String javaFileName = "AbstractIntegrationTests";
        String type = "v2";
        GitWalker walker = new GitWalker(projectPath);
        RevCommit commit = walker.allCommits.stream()
                .filter(revCommit -> revCommit.getId().name().equals(v))
                .findFirst().get();
        CommitDiff commitDiff = walker.solve(commit);
        FileDiff aimFileDiff = commitDiff.fileDiffs.stream()
                .filter(fileDiff -> fileDiff.graph2.FILE_NAME.getName().contains(javaFileName))
                .findFirst().orElse(null);
        assert aimFileDiff != null;
        FileDiffProcessor processor = new FileDiffProcessor(aimFileDiff);
        Edit aimEdit = aimFileDiff.edits.get(3);
        List<Integer> chLines2 = IntStream.range(aimEdit.getBeginB() + 1, aimEdit.getEndB() + 1)
                .boxed()
                .collect(Collectors.toList());
        ProgramDependeceGraph graph2 = aimFileDiff.graph2;
        AbstractSyntaxTree ast2 = null;
        CompilationUnit cu2 = aimFileDiff.cu2;
        if (graph2 != null && !chLines2.isEmpty()) {
            PDGInfo pdgInfo = new PDGInfo(graph2);
            PDGInfoParser.parse(pdgInfo);
            List<Slice> slices = CodeDiffSlicer.slice(pdgInfo, chLines2, aimEdit.getType());
        }
    }
}
