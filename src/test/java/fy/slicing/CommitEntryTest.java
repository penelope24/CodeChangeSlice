package fy.slicing;

import fy.commit.CommitParser;
import fy.commit.GitHistoryWalker;
import fy.commit.repr.CommitDiff;
import fy.progex.graphs.IPDG;
import fy.slicing.repr.SliceGraph;
import fy.slicing.track.IPDGTracker;
import fy.utils.jgit.JGitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class CommitEntryTest {
    String project_path = "/Users/fy/Documents/cc2vec/slicing_change_cases";
    String output_path = project_path + "/output";
    Repository repository;
    JGitUtils jgit;

}
