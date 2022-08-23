package fy.GW;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import fy.ACE.JavaSymbolSolverBuilder;
import org.junit.jupiter.api.Test;

class MyPatchSolverTest {
    String projectPath = "/Users/fy/Downloads/spring-security-oauth";

    @Test
    void run () {
        JavaSymbolSolver javaSymbolSolver = JavaSymbolSolverBuilder.build(projectPath);
    }
}