package fy.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import fy.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class JavaSymbolSolverTest {

    String base = "/Users/fy/Documents/cc2vec/slicing_change_cases/src/ch1";

    @Test
    void test1() throws FileNotFoundException {
        TypeSolver solver = new ReflectionTypeSolver();
        solver.solveType("java.lang.TypeDeclaration");
    }

    @Test
    void test2() throws FileNotFoundException {
//        String base = "/Users/fy/Documents/fyJavaProjects/ProgramGraphs/src/main/java/fy/progex/build";
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new JavaParserTypeSolver(new File(base)));
        typeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(javaSymbolSolver);
        CompilationUnit cu = StaticJavaParser.parse(new File(
                "/Users/fy/Documents/fyJavaProjects/ProgramGraphs/src/main/java/fy/progex/build/IPDGBuilder.java"));
        MethodCallExpr mc = cu.findAll(MethodCallExpr.class).stream()
                .filter(mce -> mce.getRange().get().begin.line == 101)
                .findFirst().get();
//        cu.findAll(MethodCallExpr.class).forEach(mce -> {
//            System.out.println(mce);
//            System.out.println(mce.resolve().getQualifiedSignature());
//            System.out.println("---------");
//        });
        System.out.println(mc);
        System.out.println(mc.resolve().getQualifiedName());
    }

}
