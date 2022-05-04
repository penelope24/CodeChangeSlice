package fy.slicing.entry;

import fy.commit.repr.CommitDiff;
import fy.slicing.repr.SliceGraph;
import fy.slicing.track.IPDGTracker;

import java.io.IOException;

public class CommitEntry {

    public static void track(CommitDiff commitDiff, String output) throws IOException {
        SliceGraph slice1 = IPDGTracker.track(commitDiff.ipdg1, commitDiff.commit, 1);
        SliceGraph slice2 = IPDGTracker.track(commitDiff.ipdg2, commitDiff.commit, 2);
        slice1.exportDOT(output);
        slice2.exportDOT(output);
    }
}
