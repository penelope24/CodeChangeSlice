package fy.GW;

import fy.CDS.CommitDiffProcessor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class GitWalkerTest {
    String projectPath = "/Users/fy/Downloads/spring-security-oauth";
    String outputPth = "/Users/fy/Documents/slicing_cases/real_project/security_ouath_bfc";


    @Test
    // 43m 44s
    // 1m 2s -> 10 commits --> 36s
    void run() throws GitAPIException, IOException {
        GitWalker walker = new GitWalker(projectPath);
        walker.walk();
        walker.check();
        System.out.println("valid commit diffs : " + walker.commitDiffs.size());
        walker.commitDiffs.forEach(commitDiff -> {
            System.out.println("now at : " + commitDiff.v2);
            CommitDiffProcessor processor = new CommitDiffProcessor(commitDiff, "/Users/fy/Documents/MyProjects/java/ProgramGraphs/src/main/resources/test_output");
            try {
                processor.process();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void check_file_diff_with_version() throws GitAPIException, IOException {
        GitWalker walker = new GitWalker(projectPath);
        walker.walk(100);
        walker.check();
        System.out.println("valid commit diffs : " + walker.commitDiffs.size());
        walker.commitDiffs.forEach(commitDiff -> {
            System.out.println("now at : " + commitDiff.v2);
            CommitDiffProcessor processor = new CommitDiffProcessor(commitDiff, "/Users/fy/Documents/MyProjects/java/ProgramGraphs/src/main/resources/test_output");
            try {
                processor.custom_check();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}