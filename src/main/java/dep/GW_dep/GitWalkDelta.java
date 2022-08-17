//package dep.GW_dep;
//
//import com.github.javaparser.symbolsolver.JavaSymbolSolver;
//import dep.GW_dep.repr.Delta;
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
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//public class GitWalkDelta {
//    public String project_path;
//    public Repository repository;
//    public JGitUtils jgit;
//    public JavaSymbolSolver javaSymbolSolver;
//    public List<RevCommit> allCommits = new ArrayList<>();
//    public List<Delta> worklist = new ArrayList<>();
//    public int LIMIT_SIZE = 20;
//
//    public GitWalkDelta(String project_path) throws GitAPIException, IOException {
//        this.project_path = project_path;
//        this.repository = JGitUtils.buildJGitRepository(project_path);
//        this.jgit = new JGitUtils(project_path);
////        this.javaSymbolSolver = JavaParserUtils.getJavaSymbolSolver(project_path);
//        jgit.delete_lock_file();
//        jgit.reset();
////        myTypeSolver = getMyTypeSolver();
//    }
//
//    public void walk() throws IOException, GitAPIException {
//        walk1();
//        walk2();
//    }
//
//
//    public void walk1() throws IOException, GitAPIException {
//        jgit.reset();
//        ObjectId master = JGitUtils.getMaster(repository);
//        if (master == null) {
//            throw new IllegalStateException("cannot find master head to start traverse");
//        }
//        RevCommit head = repository.parseCommit(master);
//        jgit.safe_checkout(head.getId().name());
//        RevWalk revWalk = new RevWalk(repository);
//        revWalk.markStart(head);
//        for (RevCommit commit : revWalk) {
//            RevCommit par = JGitUtils.findFirstParent(repository, commit);
//            if (par == null) {
//                allCommits.add(commit);
//                continue;
//            }
//            allCommits.add(commit);
//            List<DiffEntry> diffEntries = JGitUtils.listDiffEntries(repository, commit, par, ".java");
//            if (diffEntries.isEmpty() || diffEntries.size() > LIMIT_SIZE) {
//                continue;
//            }
//            worklist.add(new Delta(project_path, repository, par.getId().name(), commit.getId().name(), diffEntries));
//        }
//    }
//
//    public void walk2() throws IOException, GitAPIException {
//        List<RevCommit> commits = allCommits.subList(0, 30);
//        for (RevCommit commit : commits) {
//            String v = commit.getId().name();
//            jgit.safe_checkout(v);
//            // v1
//            Set<Delta> v1_list = worklist.stream()
//                    .filter(delta -> delta.v1.equals(v))
//                    .collect(Collectors.toSet());
//            if (!v1_list.isEmpty()) {
//                for (Delta delta : v1_list) {
//                    Set<FileSnapShot> snapShots = new HashSet<>();
//                    for (DiffEntry diffEntry : delta.validDiffEntries) {
//                        String path = PathUtils.getOldPath(diffEntry, repository);
//                        if (path == null) {
//                            continue;
//                        }
//                        File javaFile = new File(path);
////                        StaticJavaParser.getConfiguration().setSymbolResolver(javaSymbolSolver);
////                        CompilationUnit cu = StaticJavaParser.parse(javaFile);
//                        EditList edits = JGitUtils.getEditList(repository, diffEntry);
//                        snapShots.add(new FileSnapShot(diffEntry, javaFile, edits, "v1"));
//                    }
//                    delta.setSnapshotV1(snapShots);
//                }
//            }
//            // v2
//            Set<Delta> v2_list = worklist.stream()
//                    .filter(delta -> delta.v2.equals(v))
//                    .collect(Collectors.toSet());
//            if (!v2_list.isEmpty()) {
//                for (Delta delta : v2_list) {
//                    Set<FileSnapShot> snapShots = new HashSet<>();
//                    for (DiffEntry diffEntry : delta.validDiffEntries) {
//                        String path = PathUtils.getNewPath(diffEntry, repository);
//                        if (path == null) {
//                            continue;
//                        }
//                        File javaFile = new File(path);
////                        StaticJavaParser.getConfiguration().setSymbolResolver(javaSymbolSolver);
////                        CompilationUnit cu = StaticJavaParser.parse(javaFile);
//                        EditList edits = JGitUtils.getEditList(repository, diffEntry);
//                        snapShots.add(new FileSnapShot(diffEntry, javaFile, edits, "v2"));
//                    }
//                    delta.setSnapShotV2(snapShots);
//                }
//            }
//        }
//        worklist.removeIf(delta -> delta.snapShots1.isEmpty() || delta.snapShots2.isEmpty());
//    }
//}
