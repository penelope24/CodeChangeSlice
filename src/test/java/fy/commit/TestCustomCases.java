package fy.commit;

import fy.utils.jgit.JGitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class TestCustomCases {

    String project_path = "/Users/fy/Documents/cc2vec/slicing_change_cases";
    String output_path = project_path + "/output";
    Repository repository;
    JGitUtils jgit;

    @BeforeEach
    void init () {
        repository = JGitUtils.buildJGitRepository(project_path);
        jgit = new JGitUtils(project_path);
    }

    @Test
    void run () throws GitAPIException, IOException {

    }

    @Test
    void run_single() throws GitAPIException {

    }
}
