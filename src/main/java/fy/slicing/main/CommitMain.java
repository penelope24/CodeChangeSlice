package fy.slicing.main;

import fy.commit.repr.CommitDiff;
import fy.progex.graphs.IPDG;
import fy.slicing.repr.SliceGraph;
import fy.slicing.track.IPDGTracker;

public class CommitMain {

    public void track(CommitDiff commitDiff) {
        SliceGraph slice1 = IPDGTracker.track(commitDiff.ipdg1);
        SliceGraph slice2 = IPDGTracker.track(commitDiff.ipdg2);
    }
}
