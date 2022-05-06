package fy.commit.repr;

import fy.utils.jgit.JGitUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AtomEditTest {
    String project_path = "/Users/fy/Documents/cc2vec/slicing_change_cases";
    String output_path = project_path + "/output";
    Repository repository;
    JGitUtils jgit;

    @BeforeEach
    void init () {
        repository = JGitUtils.buildJGitRepository(project_path);
        jgit = new JGitUtils(project_path);
    }

    @Test
    void test_edit() throws IOException, GitAPIException {
        RevCommit commit = JGitUtils.getRevCommitFromId(repository, "10fc7ee2301b6846451cffbbd9c41460d7ebe802");
        RevCommit par = getMainParent(repository, commit);
        List<DiffEntry> diffEntries = listDiffEntries(commit, par, ".java");
        DiffEntry entry = diffEntries.get(0);
        EditList edits = getEditList(entry);
        edits.forEach(edit -> {
            System.out.println(edit);
            System.out.println(edit.getBeginB());
            System.out.println(edit.getEndB());
        });
    }

    private List<DiffEntry> listDiffEntries(RevCommit curr, RevCommit par, String filter) throws IOException, GitAPIException {
        List<DiffEntry> diffEntries = new ArrayList<>();
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(reader, par.getTree());
            CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, curr.getTree());
            try (Git git = new Git(this.repository)) {
                diffEntries = git.diff()
                        .setOldTree(oldTree)
                        .setNewTree(newTree)
                        .setPathFilter(PathSuffixFilter.create(filter))
                        .call();
                return diffEntries;
            }
        }
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

    private EditList getEditList(DiffEntry diffEntry) throws IOException {
        DiffFormatter diffFormatter = new DiffFormatter(null);
        diffFormatter.setContext(0);
        diffFormatter.setRepository(repository);
        return diffFormatter.toFileHeader(diffEntry).toEditList();
    }
}