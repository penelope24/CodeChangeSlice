package fy.PROGEX.type.solver;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import fy.PROGEX.type.collect.TypeCollector;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public class MyTypeSolver {
    Map<String, List<ClassOrInterfaceDeclaration>> pkg2types;
    Map<String, List<MethodDeclaration>> typeName2Methods;

    public MyTypeSolver(TypeCollector collector) throws FileNotFoundException {
        this.pkg2types = collector.package2types;
        this.typeName2Methods = collector.typeName2Methods;
    }

}
