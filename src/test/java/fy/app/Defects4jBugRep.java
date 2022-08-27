package fy.app;

import fy.GW.GitWalker;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Defects4jBugRep {

    static String projectPath = "/Users/fy/Documents/opensource/defects4j-presentation-urls";
    static String base = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/defects4j";
    static GitWalker walker;

    @BeforeAll
    static void init () throws GitAPIException, IOException {
        walker = new GitWalker(projectPath);
    }

    private void rep(String v, int index) {
        BugRep.rep1(walker, v, index, base);
    }

    @Test
    void bug1() {
        System.out.println(walker.getAllCommits().size());
        String v = "b77fa155b26e1defad407ab75aaf3aa70aa73b14";
        rep(v, 1);
    }

    @Test
    void bug2 () {
        String v = "4dfdbc80adf431848867db2b644c8b1627b19aff";
        rep(v, 2);
    }

    @Test
    void bug3 () {
        String v = "af970e140be03ac4074ef5b822dd4f19ac3934fb";
        rep(v, 3);
    }

    @Test
    void bug4() {
        String v = "11dbd2fe88929f54998c81dbbd8d2604b08b6ff6";
        rep(v, 4);
    }

    @Test
    void bug5 () {
        String v = "195d7d5af81315e79af52ee52ba904d04ce4587e";
        rep(v, 5);
    }
}
