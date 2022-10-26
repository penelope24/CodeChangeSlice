package fy.GW;

import fy.GW.data.CommitDiff;
import fy.GW.data.FileDiffNew;
import fy.GW.data.Hunk;
import fy.GW.utils.JGitUtils;
import fy.GW.utils.PathUtils;
import fy.PROGEX.build.MyPDGBuilder;
import fy.utils.file.JavaFileUtils;
import ghaffarian.progex.graphs.pdg.PDGBuilder;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GitWalkNew {
    // input
    String projectPath;
    Repository repository;
    JGitUtils jgit;
    // configure
    int MAX_JAVA_FILES_PER_COMMIT = 100;
    int MAX_HUNKS_PER_FILE = 20;
    // output
    List<RevCommit> allCommits = new ArrayList<>();
    List<CommitDiff> commitDiffs = new LinkedList<>();
    List<FileDiffNew> allFileDiffNews = new LinkedList<>();
    List<Hunk> allHunks = new LinkedList<>();
    // running stats
    int PDG_BUILD_TRAIL = 0;
    int PDG_BUILD_ERR = 0;

    public GitWalkNew(String projectPath) {
        this.projectPath = projectPath;
        this.repository = JGitUtils.buildJGitRepository(projectPath);
        this.jgit = new JGitUtils(projectPath);
    }

    public void pre_walk() throws IOException, GitAPIException {
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
            RevCommit par = JGitUtils.findFirstParent(repository, commit);
            if (par == null) return;
            List<DiffEntry> diffEntries = JGitUtils.listDiffEntries(repository, commit, par, ".java");
            if (diffEntries == null || diffEntries.isEmpty()) continue;
            CommitDiff commitDiff = new CommitDiff(commit, repository, par.getId().name(), commit.getId().name(), diffEntries);
            diffEntries.forEach(entry -> {
                FileDiffNew fileDiffNew = new FileDiffNew(entry);
                fileDiffNew.v1 = par.getId().name();
                fileDiffNew.v2 = commit.getId().name();
                fileDiffNew.path1 = PathUtils.getOldPath(entry, repository);
                fileDiffNew.path2 = PathUtils.getNewPath(entry, repository);
                try {
                    EditList edits = JGitUtils.getEditList(repository, entry);
                    edits.forEach(edit -> {
                        Hunk hunk = new Hunk(edit);
                        fileDiffNew.hunks.add(hunk);
                        allHunks.add(hunk);
                    });
                    commitDiff.fileDiffNews.add(fileDiffNew);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            commitDiffs.add(commitDiff);
        }
    }

    public void walk() {
        commitDiffs.forEach(commitDiff -> {
            solve(commitDiff.commit);
        });
    }

    public void walk(int start, int end) {
        commitDiffs.subList(start, end).forEach(commitDiff -> {
            solve(commitDiff.commit);
        });
    }

    public void walk(int num) {
        commitDiffs.subList(0, num).forEach(commitDiff -> {
            solve(commitDiff.commit);
        });
    }

    public void solve(RevCommit commit) {
        RevCommit par = JGitUtils.findFirstParent(repository, commit);
        if (par == null) return;
        String v1 = par.getId().name();
        String v2 = commit.getId().name();
        try {
            CommitDiff commitDiff = commitDiffs.stream()
                    .filter(t -> t.v2.equals(v2))
                    .findFirst().orElse(null);
            assert commitDiff != null;
            // v1
            jgit.checkout(v1);
            commitDiff.fileDiffNews.forEach(fileDiffNew -> {
                String path1 = fileDiffNew.path1;
                if (path1 == null) return;
                // pdg
                ProgramDependeceGraph pdg = null;
                try {
                    PDG_BUILD_TRAIL++;
                    pdg = MyPDGBuilder.build(new File(path1));
                }
                catch (Exception e) {
                    PDG_BUILD_ERR++;
                    System.out.println(e);
                    return;
                }
                if (pdg == null) return;
                fileDiffNew.graph1 = pdg;
                // hunks
                fileDiffNew.hunks.forEach(hunk -> {
                    IntStream.range(hunk.edit.getBeginA() + 1, hunk.edit.getEndA() + 1)
                            .boxed()
                            .forEach(i -> {
                                hunk.linesREM.add(i);
                                try {
                                    String line = JavaFileUtils.getLineByLineNum(fileDiffNew.path1, i);
                                    hunk.stmtsREM.add(line);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                });
            });
            //v2
            jgit.checkout(v2);
            commitDiff.fileDiffNews.forEach(fileDiffNew -> {
                String path2 = fileDiffNew.path2;
                if (path2 == null) return;
                // pdg
                ProgramDependeceGraph pdg = null;
                try {
                    PDG_BUILD_TRAIL++;
                    pdg = MyPDGBuilder.build(new File(path2));
                }
                catch (Exception e) {
                    PDG_BUILD_ERR++;
                    System.out.println(e);
                    return;
                }
                if (pdg == null) return;
                fileDiffNew.graph2 = pdg;
                fileDiffNew.hunks.forEach(hunk -> {
                    IntStream.range(hunk.edit.getBeginB() + 1, hunk.edit.getEndB() + 1)
                            .boxed()
                            .forEach(i -> {
                                hunk.linesADD.add(i);
                                try {
                                    String line = JavaFileUtils.getLineByLineNum(fileDiffNew.path2, i);
                                    hunk.stmtsADD.add(line);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                });
            });
            commitDiff.is_solved = true;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public void check() {
        System.out.println("total PDG trial: " + PDG_BUILD_TRAIL);
        System.out.println("total PDG err: " + PDG_BUILD_ERR);
        List<CommitDiff> solvedCommitDiffs = commitDiffs.stream()
                .filter(commitDiff -> commitDiff.is_solved)
                .collect(Collectors.toList());
        System.out.println(solvedCommitDiffs.size());
        List<CommitDiff> validCommitDiffs = solvedCommitDiffs.stream()
                .filter(CommitDiff::is_valid)
                .collect(Collectors.toList());
        System.out.println(validCommitDiffs.size());
    }
}
