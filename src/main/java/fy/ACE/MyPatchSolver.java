package fy.ACE;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.cfg.CFNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class MyPatchSolver {
    String projectPath;
    JavaSymbolSolver javaSymbolSolver;
    List<File> diffJavaFiles;
    public Map<File, CompilationUnit> parseTreeMap = new HashMap<>();
    public Set<MethodKey> candidates = new HashSet<>();

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
                md.getRange().ifPresent(r -> {
                    int line = r.begin.line;
                    MethodKey key = new MethodKey(javaFile, line);
                    try {
                        String signature = md.resolve().getQualifiedSignature();
                        Utils.parseQualifiedSignature(key, signature);
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
                        key.setPkgName(pkgName);
                        key.setClsName(clsName);
                        key.setMethodName(methodName);
                        key.setParamNum(paramNum);
                        key.setSimpleParamTypes(simpleParamTypes);
                    }
                    candidates.add(key);
                });
            });
            List<ConstructorDeclaration> cds = cu.findAll(ConstructorDeclaration.class);
            cds.forEach(md -> {
                md.getRange().ifPresent(r -> {
                    int line = r.begin.line;
                    MethodKey key = new MethodKey(javaFile, line);
                    try {
                        String signature = md.resolve().getQualifiedSignature();
                        Utils.parseQualifiedSignature(key, signature);
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
                        key.setPkgName(pkgName);
                        key.setClsName(clsName);
                        key.setMethodName(methodName);
                        key.setParamNum(paramNum);
                        key.setSimpleParamTypes(simpleParamTypes);
                    }
                    candidates.add(key);
                });
            });
        }
    }

    public List<MethodCall> solveMethodCalls(File javaFile) {
        List<MethodCall> allMethodCalls = new ArrayList<>();
        CompilationUnit cu = this.parseTreeMap.get(javaFile);
        if (cu == null) throw new IllegalStateException("cannot find parse tree");
        cu.findAll(MethodDeclaration.class).forEach(md -> {
            md.getRange().ifPresent(r -> {
//                int ml = r.begin.line;
                List<MethodCallExpr> mces = md.findAll(MethodCallExpr.class);
                if (!mces.isEmpty()) {
//                    MethodKey src = this.candidates.stream()
//                            .filter(key -> key.javaFile.equals(javaFile) && key.line == ml)
//                            .findFirst().orElse(null);
//                    if (src == null) throw new IllegalStateException("cannot find src method key");
                    mces.forEach(mce -> {
                        mce.getRange().ifPresent(mcr -> {
                            int line = mcr.begin.line;
                            MethodKey src = new MethodKey(javaFile, line);
                            try {
                                String signature = mce.resolve().getQualifiedSignature();
                                MethodKey dummy = new MethodKey(javaFile, -1);
                                Utils.parseQualifiedSignature(dummy, signature);
                                if (this.candidates.contains(dummy)) {
                                    MethodKey tgt = candidates.stream()
                                            .filter(methodKey -> methodKey.equals(dummy))
                                            .findFirst().orElse(null);
                                    assert tgt != null;
                                    MethodCall call = new MethodCall(src, tgt);
                                    allMethodCalls.add(call);
                                }
                            } catch (RuntimeException e) {

                            }
                        });
                    });

                }
            });
        });
        return allMethodCalls;
    }

    public List<MethodCall> solveMethodCalls() {
        List<MethodCall> methodCalls = new ArrayList<>();
        diffJavaFiles.forEach(javaFile -> {
            methodCalls.addAll(solveMethodCalls(javaFile));
        });
        return methodCalls;
    }

    public Set<CFNode> solveClassNodes() {
        Set<CFNode> classNodes = new LinkedHashSet<>();
        for (Map.Entry<File, CompilationUnit> entry : this.parseTreeMap.entrySet()) {
            CompilationUnit cu = entry.getValue();
            File javaFile = entry.getKey();
            List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
            classes.forEach(cls -> {
                cls.getRange().ifPresent(range -> {
                    int line = range.begin.line;
                    String code = cls.getNameAsString();
                    CFNode clsNode = new CFNode();
                    clsNode.setLineOfCode(line);
                    clsNode.setCode(code);
                    clsNode.setType(NodeType.CLASS);
                    // containing methods
                    List<MethodDeclaration> mds = cls.findAll(MethodDeclaration.class);
                    List<MethodKey> targets = new ArrayList<>();
                    mds.forEach(md -> {
                        md.getRange().ifPresent(r -> {
                            int ml = r.begin.line;
                            MethodKey key = this.candidates.stream()
                                    .filter(key1 -> key1.javaFile.equals(javaFile) && key1.line == ml)
                                    .findFirst().orElse(null);
                            assert key != null;
                            targets.add(key);
                        });
                    });
                    List<ConstructorDeclaration> cds = cls.findAll(ConstructorDeclaration.class);
                    cds.forEach(cd -> {
                        cd.getRange().ifPresent(r -> {
                            int cl = r.begin.line;
                            MethodKey key = this.candidates.stream()
                                    .filter(key1 -> key1.javaFile.equals(javaFile) && key1.line == cl)
                                    .findFirst().orElse(null);
                            assert key != null;
                            targets.add(key);
                        });
                    });
                    clsNode.setProperty("targets", targets);
                    classNodes.add(clsNode);
                });
            });
        }
        return classNodes;
    }

    // todo target to ?
    public Set<CFNode> solveImports() {
        Set<CFNode> importNodes = new HashSet<>();
        for (Map.Entry<File, CompilationUnit> entry : this.parseTreeMap.entrySet()) {
            CompilationUnit cu = entry.getValue();
            File javaFile = entry.getKey();
            List<ImportDeclaration> imports = cu.findAll(ImportDeclaration.class);
            imports.forEach(id -> {
                id.getRange().ifPresent(range -> {
                    int line = range.begin.line;
                    String code = id.toString();
                    CFNode importNode = new CFNode();
                    importNode.setLineOfCode(line);
                    importNode.setCode(code);
                    importNode.setType(NodeType.CLASS);
                    importNodes.add(importNode);
                });
            });
        }
        return importNodes;
    }

    // todo target to ?
    public Set<CFNode> solveComments() {
        Set<CFNode> commentNodes = new HashSet<>();
        for (Map.Entry<File, CompilationUnit> entry : this.parseTreeMap.entrySet()) {
            CompilationUnit cu = entry.getValue();
            File javaFile = entry.getKey();
            List<Comment> comments = cu.getAllComments();
            comments.forEach(comment -> {
                comment.getRange().ifPresent(range -> {
                    int line = range.begin.line;
                    String code = comment.getContent();
                    CFNode commentNode = new CFNode();
                    commentNode.setLineOfCode(line);
                    commentNode.setCode(code);
                    commentNode.setType(NodeType.CLASS);
                    commentNodes.add(commentNode);
                });
            });
        }
        return commentNodes;
    }


}
