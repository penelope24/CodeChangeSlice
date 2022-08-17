//package dep.GW_dep;
//
//import dep.GW_dep.repr.FileSnapShot;
//import fy.GW.utils.JGitUtils;
//import fy.GW.utils.PathUtils;
//import org.eclipse.jgit.api.errors.GitAPIException;
//import org.eclipse.jgit.diff.DiffEntry;
//import org.eclipse.jgit.diff.EditList;
//import org.eclipse.jgit.lib.ObjectId;
//import org.eclipse.jgit.lib.Repository;
//import org.eclipse.jgit.revwalk.RevCommit;
//import org.eclipse.jgit.revwalk.RevWalk;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class GitWalker {
//
//    String projectPath;
//    Repository repository;
//    JGitUtils jgit;
//    List<RevCommit> allCommits = new ArrayList<>();
//    Map<String, FileSnapShot> fileSnapShotMap = new HashMap<>();
//    Map<String, MyTypeSolver> typeSolverMap = new HashMap<>();
//
//    public GitWalker(String projectPath) {
//        this.projectPath = projectPath;
//        this.repository = JGitUtils.buildJGitRepository(projectPath);
//        this.jgit = new JGitUtils(projectPath);
//    }
//
//    public void walk() throws GitAPIException, IOException {
//        // init
//        jgit.reset();
//        ObjectId master = JGitUtils.getMaster(repository);
//        if (master == null) {
//            throw new IllegalStateException("cannot find master head to start traverse");
//        }
//        RevCommit head = repository.parseCommit(master);
//        String headId = head.getId().name();
//        jgit.safe_checkout(headId);
//        RevWalk revWalk = new RevWalk(repository);
//        revWalk.markStart(head);
//        int count = 0;
//        for (RevCommit commit : revWalk) {
//            RevCommit par = JGitUtils.findFirstParent(repository, commit);
//            if (par == null) {
//                allCommits.add(commit);
//                continue;
//            }
//            allCommits.add(commit);
//            List<DiffEntry> diffEntries = JGitUtils.listDiffEntries(repository, commit, par, ".java");
//            if (count == 0) {
//                String id_new = par.getId().name();
//                String id_old = commit.getId().name();
//                jgit.safe_checkout(id_new);
//                diffEntries.forEach(diffEntry -> {
//                    try {
//                        String path = PathUtils.getNewPath(diffEntry, repository);
//                        EditList edits = JGitUtils.getEditList(repository, diffEntry);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                });
//            }
//        }
//
//    }
//}
