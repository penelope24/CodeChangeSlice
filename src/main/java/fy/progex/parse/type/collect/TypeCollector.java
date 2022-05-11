package fy.progex.parse.type.collect;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import fy.progex.parse.PDGInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class TypeCollector {
    public List<CompilationUnit> parseTrees = new LinkedList<>();
    public Map<String, List<ClassOrInterfaceDeclaration>> package2types = new LinkedHashMap<>();
    public Map<String, List<MethodDeclaration>> typeName2Methods = new LinkedHashMap<>();
    public Set<MethodDeclaration> allAvailableMethods = new LinkedHashSet<>();

    public TypeCollector(String[] javaFiles) throws FileNotFoundException {
        for (String javaFile : javaFiles) {
            CompilationUnit cu = StaticJavaParser.parse(new File(javaFile));
            parseTrees.add(cu);
        }
    }

    public TypeCollector(List<PDGInfo> worklist) {
        worklist.forEach(pdgInfo -> {
            parseTrees.add(pdgInfo.snapShot.cu);
        });
    }

    public void collect () {
        for (CompilationUnit cu : parseTrees) {
            boolean is_pkg_present = cu.findFirst(PackageDeclaration.class).isPresent();
            String pkgName = is_pkg_present ?
                    cu.findFirst(PackageDeclaration.class).get().getNameAsString()
                    :
                    "";
            package2types.computeIfAbsent(pkgName, k -> new LinkedList<>()).addAll(cu.findAll(ClassOrInterfaceDeclaration.class));
        }
        package2types.forEach((pkg, types) -> {
            types.forEach(type -> {
                typeName2Methods.computeIfAbsent(type.getNameAsString(), k -> new LinkedList<>()).addAll(type.findAll(MethodDeclaration.class));
            });
        });
        typeName2Methods.values().forEach(methods -> {
            allAvailableMethods.addAll(methods);
        });
    }
}
