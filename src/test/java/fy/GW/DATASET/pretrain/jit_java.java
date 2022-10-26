package fy.GW.DATASET.pretrain;

import fy.GW.GitWalker;
import fy.GW.utils.JGitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class jit_java {

    String path_file = "/Users/fy/Documents/MyProjects/CCDS/datasets/jit_java/paths";
    ArrayList<String> paths = new ArrayList<>();

    @BeforeEach
    void init () throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(path_file)));
        String str;
        while ((str = br.readLine()) != null) {
            paths.add(str);
        }
    }

    @Test
    void test () {
        System.out.println(paths);
    }

    @Test
    void count_hunks () {
        paths.forEach(p -> {
            Repository repository = JGitUtils.buildJGitRepository(p);
            try {
                GitWalker gitWalker = new GitWalker(p);
                List<RevCommit> commits = gitWalker.getAllCommits();
                commits.forEach(commit -> {
                    RevCommit par = JGitUtils.findFirstParent(repository, commit);
                    if (par == null) return;
                    List<DiffEntry> diffEntries = null;
                    try {
                        diffEntries = JGitUtils.listDiffEntries(repository, commit, par, ".java");

                    } catch (IOException | GitAPIException e) {
                        e.printStackTrace();
                    }
                });
            } catch (GitAPIException | IOException e) {
                e.printStackTrace();
            }
        });
    }
}
