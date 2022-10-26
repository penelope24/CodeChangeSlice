package fy.GW;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class GitWalkNewTest {
    String path = "/Users/fy/Documents/MyProjects/CodeChangeDataSet/gerrit";

    // 1m53s  2s
    // 3m49s(100 - 109s)    7m2s (300 - 302)
    @Test
    void test () throws GitAPIException, IOException {
        GitWalkNew gitWalkNew = new GitWalkNew(path);
        gitWalkNew.pre_walk();
        gitWalkNew.walk(100);
        gitWalkNew.check();
    }

}
