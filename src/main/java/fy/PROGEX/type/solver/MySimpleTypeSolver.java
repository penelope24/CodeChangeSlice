package fy.PROGEX.type.solver;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import fy.PROGEX.type.collect.TypeCollector;
import fy.PROGEX.type.data.MethodKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MySimpleTypeSolver extends MyTypeSolver{
    CompilationUnit cu;
    public TypeSolver typeSolver = new ReflectionTypeSolver();
    List<ImportDeclaration> explicitImports;
    List<ImportDeclaration> implicitImports;
    String pkgName;
    Map<String, String> varNameActualTypeMap = new LinkedHashMap<>();
    List<MethodDeclaration> localMethods;
    List<MethodCallExpr> localCalls = new LinkedList<>();

    public MySimpleTypeSolver(TypeCollector collector, String targetFile) throws FileNotFoundException {
        super(collector);
        CompilationUnit cu = StaticJavaParser.parse(new File(targetFile));
        this.cu = cu;
        this.implicitImports = cu.getImports().stream()
                .filter(importDeclaration -> importDeclaration.toString().strip().endsWith("*;"))
                .collect(Collectors.toList());
        this.explicitImports = cu.getImports().stream()
                .filter(importDeclaration -> !importDeclaration.toString().strip().endsWith("*;"))
                .collect(Collectors.toList());
        boolean is_pkg_present = cu.findFirst(PackageDeclaration.class).isPresent();
        this.pkgName = is_pkg_present ?
                cu.findFirst(PackageDeclaration.class).get().getNameAsString()
                :
                "";
        this.localMethods = cu.findAll(MethodDeclaration.class);
        localMethods.forEach(methodDeclaration -> {
            if (methodDeclaration.findAll(MethodCallExpr.class) != null) {
                localCalls.addAll(methodDeclaration.findAll(MethodCallExpr.class));
            }
        });
        analyze_type_declaration();
    }


    private void analyze_type_declaration() {
        cu.findAll(VariableDeclarator.class).forEach(var -> {
            String varName = var.getNameAsString();
            String actualTypeName;
            if (var.getInitializer().isPresent()) {
                Expression initializer = var.getInitializer().get();
                if (initializer.isObjectCreationExpr()) {
                    ObjectCreationExpr oce = (ObjectCreationExpr) initializer;
                    actualTypeName = oce.getType().getNameAsString();
                }
                else {
                    actualTypeName = var.getType().asString();
                }
            }
            else {
                actualTypeName = var.getType().asString();
            }
            varNameActualTypeMap.put(varName, actualTypeName);
        });
        cu.findAll(Parameter.class).forEach(parameter -> {
            varNameActualTypeMap.put(parameter.getNameAsString(), parameter.getTypeAsString());
        });
    }

    // TODO: 2022/5/10 type parameters
    public String solveSimpleTypeName(String simpleTypeName) {
        // find in reflection type solver
        // find in imports
        for (ImportDeclaration importDeclaration : explicitImports) {
            String name = importDeclaration.getNameAsString();
            if (simpleTypeName.equals(name.substring(name.lastIndexOf(".") + 1))) {
                return name;
            }
        }
        //if not in imports , find in types in current package
        List<ClassOrInterfaceDeclaration> currentPackageTypes = pkg2types.get(pkgName);
        if (currentPackageTypes != null) {
            for (ClassOrInterfaceDeclaration type : currentPackageTypes) {
                String name = type.getFullyQualifiedName().get();
                if (simpleTypeName.equals(name.substring(name.lastIndexOf(".") + 1))) {
                    return name;
                }
            }
        }
        List<String> implicitNames = new LinkedList<>();
        for (ImportDeclaration importDeclaration : implicitImports) {
            implicitNames.add(importDeclaration.getNameAsString() + "." + simpleTypeName);
        }
        try {
            String name = typeSolver.solveType("java.lang." + simpleTypeName).getQualifiedName();
            return name;
        } catch (Exception e) {
            return implicitNames.get(0);
        }
    }

    public String solveVarUse(String varName) {
        return varNameActualTypeMap.get(varName);
    }

    public MethodDeclaration solveMethodCall(MethodCallExpr mce) {
        boolean is_scope_present = mce.getScope().isPresent();
        if (is_scope_present) {
            Expression scope = mce.getScope().get();
            String fullScopeName = analyzeScope(scope);
            List<MethodDeclaration> candidates = null;
            if (fullScopeName != null) {
                if (fullScopeName.equals("local")) {
                    candidates = localMethods;
                }
                else {
                    candidates = typeName2Methods.get(fullScopeName);
                }
            }
            if (candidates != null) {
                return findMethod(mce, candidates);
            }
            else {
                return null;
            }
        }
        else {
            return findMethod(mce, localMethods);
        }
    }

    public String analyzeScope(Expression scope) {
        if (scope.isObjectCreationExpr()) {
            String simpleTypeName = scope.asObjectCreationExpr().getTypeAsString();
            String fullTypeName = solveSimpleTypeName(simpleTypeName);
            return fullTypeName;
        }
        if (scope.isNameExpr()) {
            String name = scope.asNameExpr().getNameAsString();
            if (Character.isUpperCase(name.charAt(0))) {
                String fullTypeName = solveSimpleTypeName(name);
                return fullTypeName;
            }
            else {
                String simpleTypeName = solveVarUse(name);
                String fullTypeName = solveSimpleTypeName(simpleTypeName);
                return fullTypeName;
            }
        }
        if (scope.isThisExpr()) {
            return "local";
        }
        if (scope.isSuperExpr()) {
            if (scope.asSuperExpr().getTypeName().isPresent()) {
                String simpleTypeName = scope.asSuperExpr().getTypeName().get().getIdentifier();
                String fullTypeName = solveSimpleTypeName(simpleTypeName);
                return fullTypeName;
            }
        }
        if (scope.isMethodCallExpr()) {
            return "local";
        }
        if (scope.isArrayAccessExpr()) {
            String simpleTypeName = scope.asArrayAccessExpr().getName().toString();
            String fullTypeName = solveSimpleTypeName(simpleTypeName);
            return fullTypeName;
        }
        return null;
    }

    private MethodDeclaration findMethod(MethodCallExpr mce, List<MethodDeclaration> methods) {
        String name = mce.getNameAsString();
        int paramNum = mce.getArguments().size();
        List<String> paramTypeList = new LinkedList<>();
        mce.getArguments().forEach(arg -> {
            paramTypeList.add(solveVarUse(arg.toString()));
        });
        MethodKey key = new MethodKey(name, paramTypeList, paramNum);
        return methods.stream()
                .filter(md -> {
                    MethodKey mdKey = new MethodKey(md);
                    return mdKey.equals(key);
                })
                .findFirst().orElse(null);
    }

    public CompilationUnit getCu() {
        return cu;
    }

    public TypeSolver getTypeSolver() {
        return typeSolver;
    }

    public List<ImportDeclaration> getExplicitImports() {
        return explicitImports;
    }

    public List<ImportDeclaration> getImplicitImports() {
        return implicitImports;
    }

    public String getPkgName() {
        return pkgName;
    }

    public Map<String, String> getVarNameActualTypeMap() {
        return varNameActualTypeMap;
    }

    public List<MethodDeclaration> getLocalMethods() {
        return localMethods;
    }

    public List<MethodCallExpr> getLocalCalls() {
        return localCalls;
    }
}
