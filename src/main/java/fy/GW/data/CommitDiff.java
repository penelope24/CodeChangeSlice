package fy.GW.data;

import fy.ACE.MethodCall;
import fy.ACE.MyPatchSolver;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;

import java.util.List;

public class CommitDiff {
    Repository repository;
    String projectPath;
    String v1;
    String v2;
    List<DiffEntry> diffEntries;
    List<FileDiff> fileDiffs;
    MyPatchSolver solver1;
    List<MethodCall> methodCalls1;
    MyPatchSolver solver2;
    List<MethodCall> methodCalls2;

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

    public void setSolver1(MyPatchSolver solver1) {
        this.solver1 = solver1;
    }

    public void setSolver2(MyPatchSolver solver2) {
        this.solver2 = solver2;
    }

    public void setFileDiffs(List<FileDiff> fileDiffs) {
        this.fileDiffs = fileDiffs;
    }

    public Repository getRepository() {
        return repository;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getV1() {
        return v1;
    }

    public String getV2() {
        return v2;
    }

    public List<DiffEntry> getDiffEntries() {
        return diffEntries;
    }

    public MyPatchSolver getSolver1() {
        return solver1;
    }

    public MyPatchSolver getSolver2() {
        return solver2;
    }

    public List<FileDiff> getFileDiffs() {
        return fileDiffs;
    }

    public List<MethodCall> getMethodCalls1() {
        return methodCalls1;
    }

    public void setMethodCalls1(List<MethodCall> methodCalls1) {
        this.methodCalls1 = methodCalls1;
    }

    public List<MethodCall> getMethodCalls2() {
        return methodCalls2;
    }

    public void setMethodCalls2(List<MethodCall> methodCalls2) {
        this.methodCalls2 = methodCalls2;
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
