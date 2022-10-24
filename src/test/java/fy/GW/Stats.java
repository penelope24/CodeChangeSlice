package fy.GW;

import fy.GW.utils.JGitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Stats {
    String projectPath = "/Users/fy/Documents/opensource/gerrit";
    Repository repository = JGitUtils.buildJGitRepository(projectPath);

    @Test
    void count_hunks() throws GitAPIException, IOException {
        GitWalker walker = new GitWalker(projectPath);
        AtomicInteger total = new AtomicInteger();
        List<RevCommit> commits = walker.getAllCommits();
        commits.removeIf(Objects::isNull);
        for (RevCommit commit : commits) {
            List<DiffEntry> diffEntries = null;
            RevCommit par = JGitUtils.findFirstParent(repository, commit);
            if (par == null) continue;
            try {
                diffEntries = JGitUtils.listDiffEntries(repository, commit, par, ".java");
                diffEntries.removeIf(Objects::isNull);
                diffEntries.forEach(entry -> {
                    try {
                        EditList edits = JGitUtils.getEditList(repository, entry);
                        total.addAndGet(edits.size());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
        }
        System.out.println("number of commits: " + commits.size());
        System.out.println("number of hunks: " +  total.get());
    }
}

// 1416 21510 : 15.19
// 56335 549636: 9.75
