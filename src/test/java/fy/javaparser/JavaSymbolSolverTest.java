package fy.javaparser;

import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import fy.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class JavaSymbolSolverTest {

    String base = "/Users/fy/Documents/cc2vec/slicing_change_cases/src/ch1";

    @Test
    void type_solver_test() {
        List<String> paths = TestUtils.getPaths(base);
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new MemoryTypeSolver());
        paths.forEach(path -> {
            System.out.println(path);
            typeSolver.add(new JavaParserTypeSolver(new File(base)));
        });
    }

}
