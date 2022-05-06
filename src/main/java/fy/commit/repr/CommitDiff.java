package fy.commit.repr;

import fy.progex.graphs.IPDG;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Map;
import java.util.Objects;


public class CommitDiff {
    public RevCommit commit;
    public IPDG ipdg1;
    public IPDG ipdg2;
    public Map<Edit, AtomEdit> atomEditMap;

    public CommitDiff(RevCommit commit, IPDG ipdg1, IPDG ipdg2, Map<Edit, AtomEdit> atomEditMap) {
        this.commit = commit;
        this.ipdg1 = ipdg1;
        this.ipdg2 = ipdg2;
        this.atomEditMap = atomEditMap;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.commit);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CommitDiff other = (CommitDiff) obj;
        return Objects.equals(this.commit, other.commit);
    }
}
