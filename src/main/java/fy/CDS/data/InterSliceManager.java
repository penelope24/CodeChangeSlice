package fy.CDS.data;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import fy.CDS.solver.subgraph.MethodEntrySubGraphSolver;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.*;
import java.util.stream.Collectors;

public class InterSliceManager {
    public PDGInfo pdgInfo;
    public List<Integer> hunk;
    public CompilationUnit cu;
    public Set<CFNode> extraNodes = new HashSet<>();
    public Set<CFNode> entryNodes = new HashSet<>();
    public List<Integer> validLines = new ArrayList<>();
    public List<Integer> invalidLines = new ArrayList<>();
    public Map<CFNode, List<PDNode>> entry2startNodes = new LinkedHashMap<>();
    public List<PDNode> startNodes = new ArrayList<>();

    public InterSliceManager(PDGInfo pdgInfo, List<Integer> hunk, CompilationUnit cu) {
        this.pdgInfo = pdgInfo;
        this.hunk = hunk;
        this.cu = cu;
        analyzeEntryNodes();
        analyzeHunk();
        analyzeExtraNodes();
    }

    private void analyzeEntryNodes() {
        this.entryNodes = pdgInfo.cfg.copyVertexSet().stream()
                .filter(node -> node.getType() == NodeType.ROOT)
                .collect(Collectors.toSet());
    }

    private void analyzeHunk(){
        this.hunk.forEach(line -> {
            PDNode chNode = pdgInfo.ddg.copyVertexSet().stream()
                    .filter(node -> node.getLineOfCode() == line)
                    .findFirst().orElse(null);
            if (chNode != null) {
                startNodes.add(chNode);
                validLines.add(line);
            }
            else {
                invalidLines.add(line);
            }
        });
        assert hunk.size() == validLines.size() + invalidLines.size();
        Map<PDNode, CFNode> reversedMap = new LinkedHashMap<>();
        validLines.forEach(line -> {
            CFNode nearestEntryNode = findNearestEntryNode(line);
            PDNode startNode = startNodes.stream()
                    .filter(node -> node.getLineOfCode() == line)
                    .findFirst().orElse(null);
            assert startNode != null && nearestEntryNode != null;
            reversedMap.put(startNode, nearestEntryNode);
        });
        this.entry2startNodes = reversedMap.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
    }

    private CFNode findNearestEntryNode(int start_pos) {
        return this.entryNodes.stream()
            .filter(node -> node.getLineOfCode() <= start_pos)
            .min(Comparator.comparing(node -> Math.abs(node.getLineOfCode() - start_pos)))
            .orElse(null);
    }

    private void analyzeExtraNodes() {
        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
        classes.forEach(cls -> {
            cls.getRange().ifPresent(range -> {
                int line = range.begin.line;
                String code = cls.toString();
                CFNode clsNode = new CFNode();
                clsNode.setLineOfCode(line);
                clsNode.setCode(code);
                clsNode.setType(NodeType.CLASS);
                // containing methods
                List<MethodDeclaration> mds = cls.findAll(MethodDeclaration.class);
                List<CFNode> tgtEntryNodes = new ArrayList<>();
                mds.forEach(md -> {
                    if (md.getRange().isPresent()) {
                        int mLine = md.getRange().get().begin.line;
                        CFNode tgt = entryNodes.stream()
                                .filter(node -> node.getLineOfCode() == mLine)
                                .findFirst().orElse(null);
                        if (tgt != null) {
                            tgtEntryNodes.add(tgt);
                        }
                    }
                });
                List<ConstructorDeclaration> cds = cls.findAll(ConstructorDeclaration.class);
                cds.forEach(cd -> {
                    if (cd.getRange().isPresent()) {
                        int cLine = cd.getRange().get().begin.line;
                        CFNode tgt = entryNodes.stream()
                                .filter(node -> node.getLineOfCode() == cLine)
                                .findFirst().orElse(null);
                        if (tgt != null) {
                            tgtEntryNodes.add(tgt);
                        }
                    }
                });
                clsNode.setProperty("targets", tgtEntryNodes);
                tgtEntryNodes.forEach(entryNode -> {
                    entryNode.setProperty("par_cls", clsNode);
                });
                extraNodes.add(clsNode);
            });
        });
        List<ImportDeclaration> imports = cu.findAll(ImportDeclaration.class);
        imports.forEach(id -> {
            id.getRange().ifPresent(range -> {
                int line = range.begin.line;
                String code = id.toString();
                CFNode importNode = new CFNode();
                importNode.setLineOfCode(line);
                importNode.setCode(code);
                importNode.setType(NodeType.CLASS);
                extraNodes.add(importNode);
            });
        });
        List<Comment> comments = cu.getAllComments();
        comments.forEach(comment -> {
            comment.getRange().ifPresent(range -> {
                int line = range.begin.line;
                String code = comment.getContent();
                CFNode commentNode = new CFNode();
                commentNode.setLineOfCode(line);
                commentNode.setCode(code);
                commentNode.setType(NodeType.CLASS);
                extraNodes.add(commentNode);
            });
        });
    }

    // TODO: 2022/8/18
    public DataDependenceGraph getSubGraphByEntryNode () {
//        if (this.entryNodes.size() < 1) return null;
//        if (this.entryNodes.size() == 1) return pdgInfo.ddg;
//        MethodEntrySubGraphSolver solver = new MethodEntrySubGraphSolver(pdgInfo, "", nearestEntryNode, entryNodes);
//        return solver.getSubDDG();
        return null;
    }
}
