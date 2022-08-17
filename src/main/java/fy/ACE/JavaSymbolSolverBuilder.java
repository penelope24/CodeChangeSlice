package fy.ACE;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JavaSymbolSolverBuilder {


    public static JavaSymbolSolver build(String projectPath) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(projectPath)));
        return new JavaSymbolSolver(combinedTypeSolver);
    }

    public static JavaSymbolSolver build(String projectPath, String projectJarPath) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(projectPath)));
        List<String> jarFiles = Utils.getAllJarFiles(projectJarPath);
        for (String jarFile : jarFiles) {
            try {
                combinedTypeSolver.add(new JarTypeSolver(jarFile));
            } catch (IOException e) {

            }
        }
        return new JavaSymbolSolver(combinedTypeSolver);
    }
}
