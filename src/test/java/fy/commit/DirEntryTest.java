package fy.commit;

import fy.utils.jgit.JGitUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DirEntryTest {

    String project = "/Users/fy/Documents/cc2vec/test/spring-data-cassandra";

    @Test
    void test() throws IOException, GitAPIException {
        Repository repository = JGitUtils.buildJGitRepository(project);
        String v = "bd9d48e173f4e46b0c6cfd8c02d7536f44eba8d4";
        RevCommit commit = JGitUtils.getRevCommitFromId(repository, v);
        RevCommit par = getMainParent(repository, commit);
        List<DiffEntry> diffEntries = listDiffEntries(repository, commit, par, ".java");
        List<String> javaFiles = diffEntries.stream()
                .map(diffEntry -> PathUtils.getOldPath(diffEntry, repository))
                .collect(Collectors.toList());
        DirParser.parse(javaFiles, project);
    }

    private static RevCommit getMainParent(Repository repository, RevCommit curr) {
        if (curr.getParentCount() < 1) {
            return null;
        }
        ObjectId parId = curr.getParent(0).getId();
        try {
            RevCommit par = repository.parseCommit(parId);
            assert par.getTree() != null;
            return par;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<DiffEntry> listDiffEntries(Repository repository, RevCommit curr, RevCommit par, String filter) throws IOException, GitAPIException {
        List<DiffEntry> diffEntries = new ArrayList<>();
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(reader, par.getTree());
            CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, curr.getTree());
            try (Git git = new Git(repository)) {
                diffEntries = git.diff()
                        .setOldTree(oldTree)
                        .setNewTree(newTree)
                        .setPathFilter(PathSuffixFilter.create(filter))
                        .call();
                return diffEntries;
            }
        }
    }
}
