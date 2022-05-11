package fy.progex.parse.type.solver;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import fy.progex.parse.type.collect.TypeCollector;
import fy.utils.file.DirTraveler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

class MySimpleTypeSolverTest {
    String project = "/Users/fy/Documents/fyJavaProjects/ProgramGraphs";
    TypeCollector collector;
    String path = "/Users/fy/Documents/fyJavaProjects/ProgramGraphs/src/main/java/fy/slicing/track/CFGTracker.java";
    MySimpleTypeSolver solver;

    @BeforeEach
    void init () throws FileNotFoundException {
        List<String> javaFiles = DirTraveler.findAllJavaFiles(project);
        collector = new TypeCollector(javaFiles.toArray(new String[0]));
        collector.collect();
    }

    @Test
    void test() {
        System.out.println(solver);
    }

    @Test
    void testSolveSimpleTypeName() throws FileNotFoundException {
        String s = solver.solveSimpleTypeName("String");
        System.out.println(s);
    }

    @Test
    void testSolveVarUse() {
        String s = solver.solveVarUse("mce");
        String ss = solver.solveSimpleTypeName(s);
        System.out.println(s);
        System.out.println(ss);
    }

    @Test
    void testSolveMethodCall() throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(new File(path));
        cu.findAll(MethodCallExpr.class).forEach(mce -> {
            System.out.println(mce);
            Expression scope = mce.getScope().get();
            System.out.println(solver.analyzeScope(scope));
            System.out.println("--------");
        });
    }

}