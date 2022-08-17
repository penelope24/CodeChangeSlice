package dep.GW_dep;

import fy.GW.utils.JGitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitWalkBasic {


    public static List<RevCommit> walk(String projectPath) throws GitAPIException, IOException {
        // init
        JGitUtils jgit = new JGitUtils(projectPath);
        Repository repository = JGitUtils.buildJGitRepository(projectPath);
        List<RevCommit> allCommits = new ArrayList<>();
        jgit.reset();
        ObjectId master = JGitUtils.getMaster(repository);
        if (master == null) {
            throw new IllegalStateException("cannot find master head to start traverse");
        }
        RevCommit head = repository.parseCommit(master);
        jgit.safe_checkout(head.getId().name());
        RevWalk revWalk = new RevWalk(repository);
        revWalk.markStart(head);
        for (RevCommit commit : revWalk) {
            RevCommit par = JGitUtils.findFirstParent(repository, commit);
            if (par == null) {
                allCommits.add(commit);
                continue;
            }
            allCommits.add(commit);
        }
        return allCommits;
    }
}
