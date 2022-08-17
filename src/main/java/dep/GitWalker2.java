//package dep;
//
//import com.github.javaparser.symbolsolver.JavaSymbolSolver;
//import fy.GW.data.CommitDiff;
//import fy.GW.data.FileDiff;
//import fy.GW.utils.HeapSizeChecker;
//import fy.GW.utils.JGitUtils;
//import fy.GW.utils.PathUtils;
//import fy.PROGEX.build.MyPDGBuilder;
//import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
//import org.eclipse.jgit.api.errors.GitAPIException;
//import org.eclipse.jgit.diff.DiffEntry;
//import org.eclipse.jgit.diff.Edit;
//import org.eclipse.jgit.lib.ObjectId;
//import org.eclipse.jgit.lib.Repository;
//import org.eclipse.jgit.revwalk.RevCommit;
//import org.eclipse.jgit.revwalk.RevWalk;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//public class GitWalker2 {
//
//    String projectPath;
//    Repository repository;
//    JGitUtils jgit;
//    Map<String, JavaSymbolSolver> javaSymbolSolverMap = new HashMap<>();
//    List<RevCommit> allCommits = new ArrayList<>();
//    List<CommitDiff> commitDiffs = new ArrayList<>();
//
//    public GitWalker2(String projectPath) throws GitAPIException, IOException {
//        this.projectPath = projectPath;
//        this.repository = JGitUtils.buildJGitRepository(projectPath);
//        this.jgit = new JGitUtils(projectPath);
//        preWalk();
//        System.out.println("prewalk done");
//    }
//
//    public void preWalk() throws GitAPIException, IOException {
//        jgit.delete_lock_file();
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
//        for (RevCommit commit : revWalk) {
//            allCommits.add(commit);
//            RevCommit par = JGitUtils.findFirstParent(repository, commit);
//            if (par == null) {
//                continue;
//            }
//            List<DiffEntry> diffEntries = JGitUtils.listDiffEntries(repository, commit, par, ".java");
//            if (diffEntries.size() > 20) {
//                continue;
//            }
//            if (diffEntries.size() < 1) {
//                continue;
//            }
//            commitDiffs.add(new CommitDiff(repository, projectPath, commit.getId().name(), par.getId().name(), diffEntries));
//        }
//    }
//
//    public void walk() throws IOException, GitAPIException {
//        for (RevCommit commit : this.allCommits) {
//            System.out.println(allCommits.indexOf(commit));
//            solve(commit);
//            long total = Runtime.getRuntime().totalMemory();
//            long max = Runtime.getRuntime().maxMemory();
//            long free = Runtime.getRuntime().freeMemory();
//            System.out.println(HeapSizeChecker.formatSize(total));
//            System.out.println(HeapSizeChecker.formatSize(max));
//            System.out.println(HeapSizeChecker.formatSize(free));
//        }
//    }
//
//    public void walk(int num) throws IOException, GitAPIException {
//        for (RevCommit commit : this.allCommits.subList(0, num)) {
//            System.out.println(allCommits.indexOf(commit));
//            System.out.println(commit);
//            solve(commit);
//        }
//    }
//
//    public void solve(RevCommit commit) throws IOException, GitAPIException {
//        String v = commit.getId().name();
//        jgit.safe_checkout(v);
//        // as v1
//        List<CommitDiff> commitDiffs1 = commitDiffs.stream()
//                .filter(commitDiff -> commitDiff.getV1().equals(v))
//                .collect(Collectors.toList());
//        commitDiffs1.forEach(commitDiff -> {
//            List<FileDiff> fileDiffs = commitDiff.getDiffEntries().stream()
//                    .map(FileDiff::new)
//                    .collect(Collectors.toList());
//            fileDiffs.forEach(fileDiff -> {
//                DiffEntry diffEntry = fileDiff.getDiffEntry();
//                String path1 = PathUtils.getOldPath(diffEntry, repository);
//                if (path1 == null) return;
//                File javaFile1 = new File(path1);
//                ProgramDependeceGraph graph1 = null;
//                try {
//                    graph1 = MyPDGBuilder.build(javaFile1);
//                }
//                catch (Exception e) {
//
//                }
//                try {
//                    List<Edit> edits1 = JGitUtils.getEditList(repository, diffEntry);
//                    List<List<Integer>> chLinesList1 = new ArrayList<>();
//                    edits1.forEach(edit -> {
//                        chLinesList1.add(IntStream.range(edit.getBeginA()+1, edit.getEndA()+1)
//                        .boxed()
//                        .collect(Collectors.toList()));
//                    });
//                    if (graph1 != null) {
//                        fileDiff.setFromV1(v, graph1, chLinesList1);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            commitDiff.setFileDiffs(fileDiffs);
//        });
//        // as v2
//        List<CommitDiff> commitDiffs2 = commitDiffs.stream()
//                .filter(commitDiff -> commitDiff.getV2().equals(v))
//                .collect(Collectors.toList());
//        commitDiffs2.forEach(commitDiff -> {
//            List<FileDiff> fileDiffs = commitDiff.getDiffEntries().stream()
//                    .map(FileDiff::new)
//                    .collect(Collectors.toList());
//            fileDiffs.forEach(fileDiff -> {
//                DiffEntry diffEntry = fileDiff.getDiffEntry();
//                String path2 = PathUtils.getNewPath(diffEntry, repository);
//                if (path2 == null) return;
//                File javaFile2 = new File(path2);
//                ProgramDependeceGraph graph2 = null;
//                try {
//                    graph2 = MyPDGBuilder.build(javaFile2);
//                }
//                catch (Exception e) {
//
//                }
//                try {
//                    List<Edit> edits2 = JGitUtils.getEditList(repository, diffEntry);
//                    List<List<Integer>> chLinesList2 = new ArrayList<>();
//                    edits2.forEach(edit -> {
//                        chLinesList2.add(IntStream.range(edit.getBeginB()+1, edit.getEndB()+1)
//                        .boxed()
//                        .collect(Collectors.toList()));
//                    });
//                    if (graph2 != null) {
//                        fileDiff.setFromV2(v, graph2, chLinesList2);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            commitDiff.setFileDiffs(fileDiffs);
//        });
//    }
//
//    public void check () {
//        commitDiffs.forEach(commitDiff -> {
//            commitDiff.getFileDiffs().removeIf(fileDiff -> !fileDiff.isValid());
//        });
//        commitDiffs.removeIf(commitDiff -> !commitDiff.isValid());
//    }
//
//    public List<RevCommit> getAllCommits() {
//        return allCommits;
//    }
//
//    public List<CommitDiff> getCommitDiffs() {
//        return commitDiffs;
//    }
//
//    public void walk2() throws IOException, GitAPIException {
//        for (RevCommit commit : allCommits) {
//            RevCommit par = JGitUtils.findFirstParent(repository, commit);
//            List<DiffEntry> diffEntries = JGitUtils.listDiffEntries(repository, commit, par, ".java");
//            // v1
//            jgit.safe_checkout(par.getId().name());
//            diffEntries.forEach(diffEntry -> {
//                String path1 = PathUtils.getOldPath(diffEntry, repository);
//                if (path1 == null) return;
//                File javaFile1 = new File(path1);
//                ProgramDependeceGraph graph1 = null;
//                try {
//                    graph1 = MyPDGBuilder.build(javaFile1);
//                }
//                catch (Exception e) {
//
//                }
//                List<List<Integer>> chLinesList1 = new ArrayList<>();
//                try {
//                    List<Edit> edits = JGitUtils.getEditList(repository, diffEntry);
//                    edits.forEach(edit -> {
//                        chLinesList1.add(IntStream.range(edit.getBeginA()+1, edit.getEndA()+1)
//                                .boxed()
//                                .collect(Collectors.toList()));
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            // v2
//            jgit.safe_checkout(commit.getId().name());
//            diffEntries.forEach(diffEntry -> {
//                String path2 = PathUtils.getNewPath(diffEntry, repository);
//                if (path2 == null) return;
//                File javaFile2 = new File(path2);
//                ProgramDependeceGraph graph2 = null;
//                try {
//                    graph2 = MyPDGBuilder.build(javaFile2);
//                }
//                catch (Exception e) {
//
//                }
//                List<List<Integer>> chLinesList2 = new ArrayList<>();
//                try {
//                    List<Edit> edits = JGitUtils.getEditList(repository, diffEntry);
//                    edits.forEach(edit -> {
//                        chLinesList2.add(IntStream.range(edit.getBeginB()+1, edit.getEndB()+1)
//                                .boxed()
//                                .collect(Collectors.toList()));
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            // collect
//        }
//    }
//}
