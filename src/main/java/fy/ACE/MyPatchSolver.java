package fy.ACE;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import fy.ACE.MethodCall;
import fy.ACE.MethodKey;
import fy.ACE.Utils;
import fy.GW.data.FileDiff;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyPatchSolver {
    String projectPath;
    JavaSymbolSolver javaSymbolSolver;
    List<File> diffJavaFiles;
    public Map<File, CompilationUnit> parseTreeMap = new HashMap<>();
    public Map<MethodDeclaration, MethodKey> candidates = new HashMap<>();

    public MyPatchSolver(String projectPath, JavaSymbolSolver javaSymbolSolver, List<File> diffJavaFiles) {
        this.projectPath = projectPath;
        this.javaSymbolSolver = javaSymbolSolver;
        this.diffJavaFiles = diffJavaFiles;
        setUpParseTrees();
        setUpCandidates();
    }

    private void setUpParseTrees() {
        StaticJavaParser.getConfiguration().setSymbolResolver(this.javaSymbolSolver);
        for (File javaFile : diffJavaFiles) {
            try {
                CompilationUnit cu = StaticJavaParser.parse(javaFile);
                this.parseTreeMap.put(javaFile, cu);
            } catch (FileNotFoundException e) {

            }
        }
    }

    private void setUpCandidates() {
        for (Map.Entry<File, CompilationUnit> entry : this.parseTreeMap.entrySet()) {
            File javaFile = entry.getKey();
            CompilationUnit cu = entry.getValue();
            List<MethodDeclaration> mds = cu.findAll(MethodDeclaration.class);
            mds.forEach(md -> {
                int line = md.getRange().isPresent() ?
                        md.getRange().get().begin.line
                        :
                        -1;
                try {
                    String signature = md.resolve().getQualifiedSignature();
                    MethodKey key = Utils.parseQualifiedSignature(signature);
                    key.setJavaFile(javaFile);
                    key.setLine(line);
                    candidates.put(md, key);
                } catch (RuntimeException e) {
                    Node root = md.findRootNode();
                    // method name
                    String methodName = md.getNameAsString();
                    // cls name
                    String clsName = null;
                    List<ClassOrInterfaceDeclaration> cids = root.findAll(ClassOrInterfaceDeclaration.class);
                    for (ClassOrInterfaceDeclaration c : cids) {
                        List<MethodDeclaration> methodsInClass = c.findAll(MethodDeclaration.class);
                        if (methodsInClass.stream().anyMatch(m -> m.getNameAsString().equals(methodName))) {
                            clsName = c.getNameAsString();
                        }
                        break;
                    }
                    // pkg name
                    String pkgName = "";
                    PackageDeclaration pkgNode = root.findFirst(PackageDeclaration.class).orElse(null);
                    if (pkgNode != null) {
                        pkgName = pkgNode.getNameAsString();
                    }
                    // params
                    int paramNum = md.getParameters().size();
                    List<String> simpleParamTypes = new ArrayList<>();
                    md.getParameters().forEach(parameter -> {
                        String[] sp = parameter.getTypeAsString().split("\\.");
                        String simpleTypeName = sp[sp.length-1];
                        simpleParamTypes.add(simpleTypeName);
                    });
                    MethodKey key = new MethodKey(pkgName, clsName, methodName, paramNum, simpleParamTypes);
                    key.setJavaFile(javaFile);
                    key.setLine(line);
                    candidates.put(md, key);
                }
            });
        }
    }

    public List<MethodCall> solve(File javaFile) {
        List<MethodCall> allMethodCalls = new ArrayList<>();
        CompilationUnit cu = this.parseTreeMap.get(javaFile);
        if (cu == null) throw new IllegalStateException("cannot find parse tree");
        cu.findAll(MethodDeclaration.class).forEach(md -> {
            List<MethodCallExpr> mces = md.findAll(MethodCallExpr.class);
            if (!mces.isEmpty()) {
                MethodKey src = this.candidates.get(md);
                if (src == null) throw new IllegalStateException("cannot find src method key");
                mces.forEach(mce -> {
//                    int line = mce.getRange().isPresent() ?
//                            mce.getRange().get().begin.line
//                            :
//                            -1;
//                    src.setLine(line);
                    try {
                        String signature = mce.resolve().getQualifiedSignature();
                        MethodKey key = Utils.parseQualifiedSignature(signature);
                        if (this.candidates.containsValue(key)) {
                            MethodKey tgt = candidates.values().stream()
                                    .filter(methodKey -> methodKey.equals(key))
                                    .findFirst().orElse(null);
                            assert tgt != null;
                            MethodCall call = new MethodCall(src, tgt);
                            allMethodCalls.add(call);
                        }
                    } catch (RuntimeException e) {

                    }
                });

            }
        });
        return allMethodCalls;
    }

    public List<MethodCall> solve() {
        List<MethodCall> methodCalls = new ArrayList<>();
        diffJavaFiles.forEach(javaFile -> {
            methodCalls.addAll(solve(javaFile));
        });
        return methodCalls;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public JavaSymbolSolver getJavaSymbolSolver() {
        return javaSymbolSolver;
    }

    public List<File> getDiffJavaFiles() {
        return diffJavaFiles;
    }

    public Map<File, CompilationUnit> getParseTreeMap() {
        return parseTreeMap;
    }

    public Map<MethodDeclaration, MethodKey> getCandidates() {
        return candidates;
    }


}
