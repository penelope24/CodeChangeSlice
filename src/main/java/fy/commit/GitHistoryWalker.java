package fy.commit;

import fy.commit.repr.CommitDiff;
import fy.utils.jgit.JGitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class GitHistoryWalker {
    public String project_path;
    public Repository repository;
    public JGitUtils jgit;
    // res
    public List<RevCommit> allCommits = new LinkedList<>();
    public List<RevCommit> validCommits = new LinkedList<>();
    public List<CommitDiff> commitDiffs = new LinkedList<>();

    public GitHistoryWalker(String project_path) {
        this.project_path = project_path;
        this.repository = JGitUtils.buildJGitRepository(project_path);
        this.jgit = new JGitUtils(project_path);
    }

    public void walk_and_parse(String output) throws GitAPIException {
        jgit.delete_lock_file();
        jgit.reset();
        try {
            ObjectId master = jgit.getMaster();
            if (master == null) {
                throw new IllegalStateException("cannot find master head to start traverse");
            }
            RevCommit head = repository.parseCommit(master);
            jgit.safe_checkout(head.getId().name());
            try (RevWalk revWalk = new RevWalk(repository)) {
                try {
                    revWalk.markStart(this.getHeadCommit());
                    int index = 0;
                    int parse_error_counter = 0;
                    for (RevCommit commit : revWalk) {
                        System.out.println("at index: " + index + "    " + commit);
                        // parse
                        RevCommit par = getMainParent(repository, commit);
                        if (par == null) {
                            continue;
                        }
                        CommitParser parser = new CommitParser(repository, jgit, commit, par, index++);
                        if (parser.is_valid()) {
                            validCommits.add(commit);
                            CommitDiff commitDiff = parser.parse();
                            if (commitDiff != null) {
                                commitDiffs.add(commitDiff);
                                commitDiff.ipdg1.exportDOT("/Users/fy/Documents/data/slicing_cases/output");
                                commitDiff.ipdg2.exportDOT("/Users/fy/Documents/data/slicing_cases/output");
                            }
                        }
                        // record parsed commit
                        allCommits.add(commit);
                    }
                    System.out.println("parse finished, error: " + parse_error_counter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    public void walk() throws GitAPIException {
        jgit.delete_lock_file();
        jgit.reset();
        try {
            ObjectId master = jgit.getMaster();
            if (master == null) {
                throw new IllegalStateException("cannot find master head to start traverse");
            }
            RevCommit head = repository.parseCommit(master);
            jgit.safe_checkout(head.getId().name());
            try (RevWalk revWalk = new RevWalk(repository)) {
                try {
                    revWalk.markStart(this.getHeadCommit());
                    int index = 0;
                    int parse_error_counter = 0;
                    for (RevCommit commit : revWalk) {
                        // parse
                        RevCommit par = getMainParent(repository, commit);
                        if (par == null) {
                            continue;
                        }
                        // record parsed commit
                        allCommits.add(commit);
                        CommitParser parser = new CommitParser(repository, jgit, commit, par, index++);
                        if (parser.is_valid()) {
                            validCommits.add(commit);
                        }
                    }
                    System.out.println("parse finished, error: " + parse_error_counter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    private RevCommit getHeadCommit() throws IOException {
        Ref head = repository.findRef("HEAD");
        return repository.parseCommit(head.getObjectId());
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

}
