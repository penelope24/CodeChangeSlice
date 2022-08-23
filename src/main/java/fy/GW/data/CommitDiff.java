package fy.GW.data;

import fy.ACE.MethodCall;
import fy.ACE.MyPatchSolver;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;

import java.util.List;

public class CommitDiff {
    public Repository repository;
    public String projectPath;
    public String v1;
    public String v2;
    public List<DiffEntry> diffEntries;
    public List<FileDiff> fileDiffs;
    public MyPatchSolver solver1;
    public MyPatchSolver solver2;

    public CommitDiff(Repository repository, String projectPath, String v1, String v2, List<DiffEntry> diffEntries) {
        this.repository = repository;
        this.projectPath = projectPath;
        this.v1 = v1;
        this.v2 = v2;
        this.diffEntries = diffEntries;
    }

    public boolean isValid() {
        if (fileDiffs == null) {
            return false;
        }
        return !fileDiffs.isEmpty();
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public void setV1(String v1) {
        this.v1 = v1;
    }

    public void setV2(String v2) {
        this.v2 = v2;
    }

    public void setDiffEntries(List<DiffEntry> diffEntries) {
        this.diffEntries = diffEntries;
    }

    public void setFileDiffs(List<FileDiff> fileDiffs) {
        this.fileDiffs = fileDiffs;
    }

    public void setSolver1(MyPatchSolver solver1) {
        this.solver1 = solver1;
    }

    public void setSolver2(MyPatchSolver solver2) {
        this.solver2 = solver2;
    }

    @Override
    public String toString() {
        return "CommitDiff{" +
                "repository=" + repository +
                ", projectPath='" + projectPath + '\'' +
                ", v1='" + v1 + '\'' +
                ", v2='" + v2 + '\'' +
                ", diffEntries=" + diffEntries +
                ", fileDiffs=" + fileDiffs +
                ", solver1=" + solver1 +
                ", solver2=" + solver2 +
                '}';
    }
}
