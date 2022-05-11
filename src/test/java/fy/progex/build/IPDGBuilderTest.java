package fy.progex.build;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import fy.TestUtils;
import fy.commit.GitHistoryWalker;
import fy.commit.entry.CommitEntry;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

class IPDGBuilderTest {
    String base = "/Users/fy/Documents/data/slicing_cases/cases/icfg2";
    String output = "/Users/fy/Documents/data/slicing_cases/output";

    @Test
    void test() throws IOException, GitAPIException {
        GitHistoryWalker walker = new GitHistoryWalker(base);
        walker.walk_and_parse(output);
    }
}