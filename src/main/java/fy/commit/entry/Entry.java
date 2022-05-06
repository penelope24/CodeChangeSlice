package fy.commit.entry;

import fy.commit.CommitParser;
import fy.commit.GitHistoryWalker;
import fy.commit.repr.CommitDiff;
import fy.utils.jgit.JGitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Entry {

    public static List<CommitDiff> process(String project) throws GitAPIException, IOException {
        GitHistoryWalker walker = new GitHistoryWalker(project);
        walker.walk();
        CommitParser parser = new CommitParser(walker.repository, walker.jgit);
        parser.parse(walker.allCommits);
        return parser.commitDiffs;
    }

    public static CommitDiff process_one(String project, String versionID) throws IOException, GitAPIException {
        Repository repository = JGitUtils.buildJGitRepository(project);
        JGitUtils jgit = new JGitUtils(project);
        RevCommit commit = JGitUtils.getRevCommitFromId(repository, versionID);
        List<RevCommit> worklist = new ArrayList<>();
        worklist.add(commit);
        CommitParser parser = new CommitParser(repository, jgit);
        parser.parse(worklist);
        List<CommitDiff> commitDiffs = parser.commitDiffs;
        return commitDiffs.isEmpty() ? null : commitDiffs.get(0);
    }
}

