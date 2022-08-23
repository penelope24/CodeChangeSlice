package fy.PROGEX.build;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import fy.CDS.export.DotExporter;
import fy.utils.file.SubFileFinder;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.ast.ASEdge;
import ghaffarian.progex.graphs.ast.ASNode;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class MyASTBuilderTest {
    String javaFilePath = "/Users/fy/Documents/MyProjects/java/ProgramGraphs/src/main/java/fy/CDS/CodeDiffSlicer.java";

    @Test
    void build_succ_test() throws IOException {
        List<String> javaFiles = SubFileFinder.findAllJavaFiles("/Users/fy/Documents/MyProjects/java/ProgramGraphs");
        AtomicInteger succ = new AtomicInteger();
        javaFiles.forEach(s -> {
            try {
                AbstractSyntaxTree ast = MyASTBuilder.build(s);
                succ.getAndIncrement();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println(succ.get());
        System.out.println(javaFiles.size());
    }

    @Test
    void buildFromOriAST() throws IOException {
        String output = "/Users/fy/Documents/MyProjects/java/ProgramGraphs/src/test/resources/tmp_output";
        int line = 32;
        AbstractSyntaxTree ast = MyASTBuilder.build(javaFilePath);
        ast.copyVertexSet().forEach(v -> {
            System.out.println(v.getLineOfCode());
        });
        List<ASNode> nodes = ast.copyVertexSet().stream()
                .filter(asNode -> asNode.getLineOfCode() == line)
                .collect(Collectors.toList());
        List<Edge<ASNode, ASEdge>> edges = ast.copyEdgeSet().stream()
                .filter(e -> nodes.contains(e.source) && nodes.contains(e.target))
                .collect(Collectors.toList());
        AbstractSyntaxTree ast2 = new AbstractSyntaxTree("ast2.java");
        nodes.forEach(ast2::addVertex);
        edges.forEach(ast2::addEdge);
        ast2.exportDOT(output);
    }

    @Test
    void buildFromCU() throws FileNotFoundException {
        AbstractSyntaxTree ast = new AbstractSyntaxTree("test123.java");
        int startLine = 32;
        CompilationUnit cu = StaticJavaParser.parse(new File(javaFilePath));
        Set<Node> nodeSet = cu.findAll(Node.class).stream()
                .filter(node -> node.getRange().isPresent())
                .filter(node -> node.getRange().get().begin.line == startLine)
                .collect(Collectors.toSet());
        Set<ASNode> vertexSet = new HashSet<>();
        nodeSet.forEach(node -> {
            ASNode asNode = buildAsNodeFromJavaParserNode(node);
            vertexSet.add(asNode);
        });
        Set<Edge<ASNode, ASEdge>> edgeSet = new HashSet<>();
        nodeSet.forEach(node -> {
            List<Node> children = node.getChildNodes();
            children.forEach(child -> {
                if (nodeSet.contains(child)) {
                    ASNode src = vertexSet.stream()
                            .filter(v -> v.getProperty("node") == node)
                            .findFirst().orElse(null);
                    assert src != null;
                    ASNode tgt = vertexSet.stream()
                            .filter(v -> v.getProperty("node") == child)
                            .findFirst().orElse(null);
                    assert tgt != null;
                    edgeSet.add(new Edge<>(src, new ASEdge(), tgt));
                }
            });
        });
        vertexSet.forEach(ast::addVertex);
        edgeSet.forEach(ast::addEdge);
        ast.root.setCode("root");
        ASNode first = vertexSet.stream()
                .filter(node -> ast.copyIncomingEdges(node).isEmpty())
                .min(Comparator.comparing(ASNode::getLineOfCode))
                .orElse(null);
        assert first != null;
        ast.addEdge(new Edge<>(ast.root, new ASEdge(), first));
        String output = "/Users/fy/Documents/MyProjects/java/ProgramGraphs/src/test/resources/tmp_output";
        DotExporter.exportDot(ast, output);
    }

    @Test
    void testDotPrinter() throws IOException {
        String output = "/Users/fy/Documents/MyProjects/java/ProgramGraphs/src/test/resources/tmp_output";
        int startLine = 32;
        CompilationUnit cu = StaticJavaParser.parse(new File(javaFilePath));
        Set<Node> nodeSet = cu.findAll(Node.class).stream()
                .filter(node -> node.getRange().isPresent())
                .filter(node -> node.getRange().get().begin.line == startLine)
                .collect(Collectors.toSet());
        ClassOrInterfaceDeclaration cls = cu.findFirst(ClassOrInterfaceDeclaration.class).get();


    }

    private ASNode buildAsNodeFromJavaParserNode(Node node) {
        int line = node.getRange().isPresent() ?
                node.getRange().get().begin.line
                :
                -1;
        String code = analyzeNodeCodeStr(node);
        ASNode asNode = new ASNode(ASNode.Type.DEFAULT);
        asNode.setCode(code);
        asNode.setLineOfCode(line);
        asNode.setProperty("node", node);
        return asNode;
    }

    private String analyzeNodeCodeStr(Node node) {
        String code = "";
        if (node.getChildNodes().size() == 0) {
            code = node.toString();
        }
        else {
            String fullClsName = node.getClass().toString();
            String[] ss = fullClsName.split("\\.");
            code = ss[ss.length-1];
        }
        return code;
    }

    @Test
    void findSubASTRootNode() throws FileNotFoundException {
        int line = 29;
        CompilationUnit cu = StaticJavaParser.parse(new File(javaFilePath));
        Node subASTRoot = MyASTBuilder.findSubASTRootNode(cu, line);
//        System.out.println(subASTRoot);
        cu.getAllComments().forEach(comment -> {
            System.out.println(comment.getClass());
        });
    }
}