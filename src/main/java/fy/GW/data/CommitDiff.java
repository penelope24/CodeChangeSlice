package fy.GW.data;

import fy.ACE.MethodCall;
import fy.ACE.MyPatchSolver;
import fy.CDS.result.CFGTrackResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.LinkedList;
import java.util.List;

public class CommitDiff {
    public RevCommit commit;
    public Repository repository;
    public String v1;
    public String v2;
    public List<DiffEntry> diffEntries;
    public List<FileDiff> fileDiffs;
    public List<FileDiffNew> fileDiffNews = new LinkedList<>();
    public MyPatchSolver solver1;
    public MyPatchSolver solver2;
    // flag
    public boolean is_solved;

    public CommitDiff(RevCommit commit, Repository repository, String v1, String v2, List<DiffEntry> diffEntries) {
        this.commit = commit;
        this.repository = repository;
        this.v1 = v1;
        this.v2 = v2;
        this.diffEntries = diffEntries;
    }

    public boolean is_valid() {
        if (v1 == null || v2 == null) {
            return false;
        }
        if (fileDiffNews.stream().noneMatch(FileDiffNew::is_valid)) {
            return false;
        }
        return true;
    }

}
