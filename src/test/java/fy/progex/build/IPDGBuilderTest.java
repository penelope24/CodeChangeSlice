package fy.progex.build;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import fy.TestUtils;
import fy.progex.graphs.IPDG;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

class IPDGBuilderTest {
    String base = "/Users/fy/Documents/data/slicing_cases/cases/icfg2";
    String output = "/Users/fy/Documents/data/slicing_cases/output";

    @Test
    void test() throws IOException {
        List<String> paths = TestUtils.getPaths(base);
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(new File(base)));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);

    }
}