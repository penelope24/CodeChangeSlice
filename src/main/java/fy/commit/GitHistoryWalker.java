package fy.commit;

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

    public GitHistoryWalker(String project_path) {
        this.project_path = project_path;
        this.repository = JGitUtils.buildJGitRepository(project_path);
        this.jgit = new JGitUtils(project_path);
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
                    for (RevCommit commit : revWalk) {
                        // record parsed commit
                        allCommits.add(commit);
                    }
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

}
