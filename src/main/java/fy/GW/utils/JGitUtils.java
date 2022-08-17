package fy.GW.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JGitUtils {
    public String project_path;
    public Repository repository;
    public Git git;

    public JGitUtils(String path) {
        this.project_path = path;
        this.repository = JGitUtils.buildJGitRepository(path);
        this.git = new Git(this.repository);
    }

    public JGitUtils(Git git) {
        this.project_path = git.getRepository().getDirectory().getAbsolutePath();
        this.repository = git.getRepository();
        this.git = git;
    }

    /**
     * static method
     * build up a JGit Repository object by local path
     */
    public static Repository buildJGitRepository (String path) {
        File file = new File(path, ".git");
        Repository repository = null;
        try {
            repository = FileRepositoryBuilder.create(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (repository == null) {
            throw new IllegalStateException("returned repository cannot be null");
        }
        return repository;
    }

    /**
     * static method
     * get RevCommit object by commitId
     */
    public static RevCommit getRevCommitFromId(Repository repository, String commitId) throws IOException {
        return repository.parseCommit(repository.resolve(commitId));
    }

    public static List<DiffEntry> listDiffEntries(Repository repository, RevCommit curr, RevCommit par, String filter) throws IOException, GitAPIException {
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

    public static RevCommit findFirstParent(Repository repository, RevCommit curr) {
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

    // TODO: 2022/5/21
    public static ObjectId getMaster(Repository repository) throws IOException {
        if (repository.findRef("master") != null) {
            return repository.findRef("master").getObjectId();
        }
        else if (repository.findRef("main") != null) {
            return repository.findRef("main").getObjectId();
        }
        else {
            return null;
        }
//        return repository.getBranch();
    }

    public static EditList getEditList(Repository repository, DiffEntry diffEntry) throws IOException {
        DiffFormatter diffFormatter = new DiffFormatter(null);
        diffFormatter.setContext(0);
        diffFormatter.setRepository(repository);
        return diffFormatter.toFileHeader(diffEntry).toEditList();
    }

    public void delete_lock_file() {
        File lock = new File(project_path + "/.git/index.lock");
        if (lock.exists()) {
            boolean r = lock.delete();
            assert r == true;
        }
    }


    /**
     *  git checkout versionID
     * */
    public void checkout(String versionId) throws GitAPIException {
        git.checkout().setName(versionId).call();
    }

    /**
     *  git clean -f
     * */
    public void clean() throws GitAPIException {
        git.clean().setForce(true).call();
    }

    /**
     *  git reset --HARD
     * */
    public void reset() throws GitAPIException {
        git.reset()
                .setMode(ResetCommand.ResetType.HARD)
                .setRef("HEAD")
                .call();
    }

    /**
     * 为了避免在windows系统上的“could not rename file”问题
     * 目前尚未解决
     * */
    public void safe_checkout(String versionId) throws GitAPIException {
        clean();
        reset();
        checkout(versionId);
    }
}
