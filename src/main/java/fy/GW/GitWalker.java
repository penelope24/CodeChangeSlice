package fy.GW;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import fy.ACE.JavaSymbolSolverBuilder;
import fy.ACE.MyPatchSolver;
import fy.GW.data.CommitDiff;
import fy.GW.data.FileDiff;
import fy.GW.utils.JGitUtils;
import fy.GW.utils.PathUtils;
import fy.PROGEX.build.MyASTBuilder;
import fy.PROGEX.build.MyPDGBuilder;
import fy.utils.file.JavaFileUtils;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GitWalker {
    String projectPath;
    Repository repository;
    JGitUtils jgit;
    List<RevCommit> allCommits = new ArrayList<>();
    List<CommitDiff> commitDiffs = new ArrayList<>();
    // for test
    int all_build_trial = 0;
    int build_succ = 0;
    int build_fail = 0;
    // hyper params
    int DIFF_ENTRY_SIZE_LIMIT = 20;
    int JAVA_FILE_SIZE_LIMIT = 10000;

    public GitWalker(String projectPath) throws GitAPIException, IOException {
        this.projectPath = projectPath;
        this.repository = JGitUtils.buildJGitRepository(projectPath);
        this.jgit = new JGitUtils(projectPath);
        preWalk();
        System.out.println("prewalk done");
    }

    public GitWalker(String projectPath, JavaSymbolSolver javaSymbolSolver) throws GitAPIException, IOException {
        this.projectPath = projectPath;
        this.repository = JGitUtils.buildJGitRepository(projectPath);
        this.jgit = new JGitUtils(projectPath);
        preWalk();
        System.out.println("prewalk done");
        StaticJavaParser.getConfiguration().setSymbolResolver(javaSymbolSolver);
    }

    public void preWalk() throws GitAPIException, IOException {
        jgit.delete_lock_file();
        jgit.reset();
        ObjectId master = JGitUtils.getMaster(repository);
        if (master == null) {
            throw new IllegalStateException("cannot find master head to start traverse");
        }
        RevCommit head = repository.parseCommit(master);
        String headId = head.getId().name();
        jgit.safe_checkout(headId);
        RevWalk revWalk = new RevWalk(repository);
        revWalk.markStart(head);
        for (RevCommit commit : revWalk) {
            allCommits.add(commit);
        }
    }

    public void walk() {
        allCommits.forEach(commit -> {
            System.out.println("at: " + allCommits.indexOf(commit));
            CommitDiff commitDiff = solve(commit);
            commitDiffs.add(commitDiff);
        });
    }

    public void walk(int num) {
        allCommits.subList(0, num).forEach(commit -> {
            System.out.println(allCommits.indexOf(commit));
            CommitDiff commitDiff = solve(commit);
            commitDiffs.add(commitDiff);
        });
    }

    public void walk(int start, int end) {
        allCommits.subList(start, end).forEach(commit -> {
            System.out.println(allCommits.indexOf(commit));
            CommitDiff commitDiff = solve(commit);
            commitDiffs.add(commitDiff);
        });
    }


    public CommitDiff solve(RevCommit commit) {
        RevCommit par = JGitUtils.findFirstParent(repository, commit);
        if (par == null) return null;
        String v1 = par.getId().name();
        String v2 = commit.getId().name();
        List<DiffEntry> diffEntries = null;
        try {
            diffEntries = JGitUtils.listDiffEntries(repository, commit, par, ".java");
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        if (diffEntries == null) return null;
        if (diffEntries.size() > 20 || diffEntries.size() < 1) return null;
        diffEntries.removeIf(Objects::isNull);
        CommitDiff commitDiff = new CommitDiff(repository, projectPath, v1, v2, diffEntries);
        List<FileDiff> fileDiffs = diffEntries.stream()
                .map(FileDiff::new)
                .collect(Collectors.toList());
        fileDiffs.forEach(fileDiff -> {
            try {
                List<Edit> edits = JGitUtils.getEditList(repository, fileDiff.diffEntry);
                fileDiff.setEdits(edits);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        // v1
        try {
            jgit.checkout(v1);
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        fileDiffs.forEach(fileDiff -> {
            String path = PathUtils.getOldPath(fileDiff.diffEntry, repository);
            if (path == null) return;
            int totalLineNUm = JavaFileUtils.countSourceLineNum(path);
            if (totalLineNUm > this.JAVA_FILE_SIZE_LIMIT) return;
            File javaFile = new File(path);
            CompilationUnit cu = null;
            ProgramDependeceGraph graph = null;
            AbstractSyntaxTree ast = null;
            all_build_trial++;
            try {
                graph = MyPDGBuilder.build(javaFile);
                ast = MyASTBuilder.build(path);
                cu = StaticJavaParser.parse(javaFile);
                build_succ++;
            }
            catch (Exception e) {
                build_fail++;
            }
            if (graph != null ) {
                fileDiff.setV1(v1);
                fileDiff.setGraph1(graph);
                fileDiff.setAst1(ast);
                fileDiff.setCu1(cu);
            }
        });
//        JavaSymbolSolver symbolSolver1 = JavaSymbolSolverBuilder.build(projectPath);
//        List<File> diffJavaFiles1 = fileDiffs.stream()
//                .filter(fileDiff -> fileDiff.graph1 != null)
//                .map(fileDiff -> fileDiff.graph1.FILE_NAME)
//                .collect(Collectors.toList());
//        MyPatchSolver patchSolver1 = new MyPatchSolver(projectPath, symbolSolver1, diffJavaFiles1);
//        commitDiff.setSolver1(patchSolver1);
        // v2
        try {
            jgit.checkout(v2);
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        fileDiffs.forEach(fileDiff -> {
            String path = PathUtils.getNewPath(fileDiff.diffEntry, repository);
            if (path == null) return;
            int totalLineNUm = JavaFileUtils.countSourceLineNum(path);
            if (totalLineNUm > this.JAVA_FILE_SIZE_LIMIT) return;
            File javaFile = new File(path);
            CompilationUnit cu = null;
            ProgramDependeceGraph graph = null;
            AbstractSyntaxTree ast = null;
            all_build_trial++;
            try {
                graph = MyPDGBuilder.build(javaFile);
                ast = MyASTBuilder.build(path);
                cu = StaticJavaParser.parse(javaFile);
                build_succ++;
            }
            catch (Exception e) {
                build_fail++;
            }
            if (graph != null) {
                fileDiff.setV2(v2);
                fileDiff.setGraph2(graph);
                fileDiff.setAst2(ast);
                fileDiff.setCu2(cu);
            }
        });
//        JavaSymbolSolver symbolSolver2 = JavaSymbolSolverBuilder.build(projectPath);
//        List<File> diffJavaFiles2 = fileDiffs.stream()
//                .filter(fileDiff -> fileDiff.graph2 != null)
//                .map(fileDiff -> fileDiff.graph2.FILE_NAME)
//                .collect(Collectors.toList());
//        MyPatchSolver patchSolver2 = new MyPatchSolver(projectPath, symbolSolver2, diffJavaFiles2);
//        commitDiff.setSolver2(patchSolver2);
        // check
        fileDiffs.removeIf(fileDiff -> !fileDiff.isValid());
        // return
        commitDiff.setFileDiffs(fileDiffs);
        return commitDiff;
    }

    public void check () {
        commitDiffs.removeIf(Objects::isNull);
        commitDiffs.removeIf(commitDiff -> !commitDiff.isValid());
    }

    public String getProjectPath() {
        return projectPath;
    }

    public Repository getRepository() {
        return repository;
    }

    public JGitUtils getJgit() {
        return jgit;
    }

    public List<RevCommit> getAllCommits() {
        return allCommits;
    }

    public List<CommitDiff> getCommitDiffs() {
        return commitDiffs;
    }

    public int getAll_build_trial() {
        return all_build_trial;
    }

    public int getBuild_succ() {
        return build_succ;
    }

    public int getBuild_fail() {
        return build_fail;
    }

    public int getDIFF_ENTRY_SIZE_LIMIT() {
        return DIFF_ENTRY_SIZE_LIMIT;
    }

    public int getJAVA_FILE_SIZE_LIMIT() {
        return JAVA_FILE_SIZE_LIMIT;
    }
}
