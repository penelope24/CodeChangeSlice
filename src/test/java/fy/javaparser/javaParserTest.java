package fy.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class javaParserTest {
    String path = "/Users/fy/Documents/fyJavaProjects/ProgramGraphs/src/main/java/fy/progex/parse/type/solver/MyTypeSolver.java";

    @Test
    void test() throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(new File(path));
//        cu.findAll(MethodCallExpr.class).forEach(t -> {
//            System.out.println(t);
//            System.out.println("-------");
//        });
        List<MethodCallExpr> mces = cu.findAll(MethodCallExpr.class);
        MethodCallExpr mce = mces.get(1);

    }
}
