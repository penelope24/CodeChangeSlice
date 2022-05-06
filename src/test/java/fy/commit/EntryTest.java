package fy.commit;

import fy.commit.entry.Entry;
import fy.commit.repr.CommitDiff;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class EntryTest {
    String project_path = "/Users/fy/Documents/cc2vec/test/spring-data-cassandra";

    @Test
    void test() throws GitAPIException, IOException {
        GitHistoryWalker walker = new GitHistoryWalker(project_path);
        walker.walk();
        CommitParser parser = new CommitParser(walker.repository, walker.jgit);
        parser.parse(walker.allCommits);
        List<CommitDiff> commitDiffs = parser.commitDiffs;
        System.out.println(commitDiffs.size());
    }

    @Test
    void process_single() throws IOException, GitAPIException {
        String v = "1f1cdcee273c59578844944ca1f0748f8c237212";
        CommitDiff commitDiff = Entry.process_one(project_path, v);
        System.out.println(commitDiff);
    }
}