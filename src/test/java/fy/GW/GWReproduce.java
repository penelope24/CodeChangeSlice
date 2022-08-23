package fy.GW;

import fy.CDS.CommitDiffProcessor;
import fy.GW.data.CommitDiff;
import fy.GW.utils.JGitUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

public class GWReproduce {
    String projectPath = "/Users/fy/Documents/MyProjects/CCS_cases";
    String output = "/Users/fy/Documents/MyProjects/CCS_cases/output";

    @Test
    void reproduce_version () throws Exception {
        String v = "6b14e851bd96535450a71a74e04fe270cfef67f2";
        GitWalker walker = new GitWalker(projectPath);
        RevCommit commit = walker.allCommits.stream()
                .filter(revCommit -> revCommit.getId().name().equals(v))
                .findFirst().get();
        System.out.println(walker.allCommits.size());
        System.out.println("at : " + walker.allCommits.indexOf(commit));
        CommitDiff commitDiff = walker.solve(commit);
        CommitDiffProcessor processor = new CommitDiffProcessor(commitDiff, output);
        processor.process();
        System.out.println("over");
        JGitUtils jgit = new JGitUtils(projectPath);
        jgit.reset();
    }


}
