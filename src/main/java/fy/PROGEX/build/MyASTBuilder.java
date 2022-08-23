package fy.PROGEX.build;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.ast.ASEdge;
import ghaffarian.progex.graphs.ast.ASNode;
import ghaffarian.progex.graphs.ast.ASTBuilder;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MyASTBuilder {

    public static AbstractSyntaxTree build (String javaFile) throws IOException {
        AbstractSyntaxTree ast = ASTBuilder.build("Java", javaFile);
        return ast;
    }

    public static AbstractSyntaxTree buildFromCU(CompilationUnit cu, String name, int startLine) {
        AbstractSyntaxTree ast = new AbstractSyntaxTree( name + ".java");
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
        if (first != null) {
            ast.addEdge(new Edge<>(ast.root, new ASEdge(), first));
        }
        return ast;
    }

    public static Node findSubASTRootNode(CompilationUnit cu, int line) {
        Set<Node> nodeSet = cu.findAll(Node.class).stream()
                .filter(node -> node.getRange().isPresent())
                .filter(node -> node.getRange().get().begin.line == line)
                .collect(Collectors.toSet());
        Node subASTRoot = nodeSet.stream()
                .filter(node -> node.getParentNode().isPresent())
                .filter(node -> !nodeSet.contains(node.getParentNode().get()))
                .findFirst().orElse(null);
        if (subASTRoot != null) {
            return subASTRoot;
        }
        return null;
    }

    private static ASNode buildAsNodeFromJavaParserNode(Node node) {
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

    private static String analyzeNodeCodeStr(Node node) {
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
}
