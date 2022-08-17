package fy.PROGEX.type.collect;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class TypeCollector2 {

    public List<CompilationUnit> parseTrees = new LinkedList<>();
    public Map<String, Set<String>> pkg2types = new LinkedHashMap<>();
    public Map<String, List<MethodDeclaration>> type2methods = new LinkedHashMap<>();

    public TypeCollector2(List<String> javaFiles) throws FileNotFoundException {
        for (String javaFile : javaFiles) {
            CompilationUnit cu = StaticJavaParser.parse(new File(javaFile));
            parseTrees.add(cu);
        }
    }

    public void parsePackageToTypes() {
        for (CompilationUnit cu : parseTrees) {
            boolean is_pkg_present = cu.findFirst(PackageDeclaration.class).isPresent();
            String pkg = is_pkg_present ?
                    cu.findFirst(PackageDeclaration.class).get().getNameAsString()
                    :
                    "";
            Set<String> typeSet = new LinkedHashSet<>();
            HashMap<TypeDeclaration, String> type2parent = new HashMap<>();
            Queue<TypeDeclaration> worklist = new LinkedList<>();
            NodeList<TypeDeclaration<?>> types = cu.getTypes();
            types.forEach(typeDeclaration -> {
                worklist.add(typeDeclaration);
                type2parent.put(typeDeclaration, pkg);
            });
            while (!worklist.isEmpty()) {
                TypeDeclaration t = worklist.poll();
                boolean has_parent = !type2parent.get(t).equals("");
                String curType = has_parent ?
                        type2parent.get(t) + "." + t.getNameAsString()
                        :
                        t.getNameAsString();
                typeSet.add(curType);
                t.getChildNodes().forEach(node -> {
                    if (node instanceof TypeDeclaration) {
                        TypeDeclaration ct = ((TypeDeclaration<?>) node).asTypeDeclaration();
                        worklist.add(ct);
                        type2parent.put(ct, curType);
                    }
                });
            }
            pkg2types.computeIfAbsent(pkg, k -> new HashSet<>(typeSet)).addAll(typeSet);
        }
    }

    public void parseTypeToMethods() {
        pkg2types.values().forEach(types -> {
            types.forEach(type -> {

            });
        });
    }
}
