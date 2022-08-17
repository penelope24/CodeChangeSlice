package fy.CDS.track;

import fy.CDS.result.ASTTrackResult;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.ast.ASEdge;
import ghaffarian.progex.graphs.ast.ASNode;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ASTTracker {

    public static AbstractSyntaxTree track(AbstractSyntaxTree oriAST, int line) {
        AbstractSyntaxTree ast = new AbstractSyntaxTree(oriAST.filePath);
        oriAST.copyVertexSet().stream()
                .filter(node -> node.getLineOfCode() == line)
                .forEach(ast::addVertex);
        oriAST.copyEdgeSet().stream()
                .filter(e -> ast.containsVertex(e.source) && ast.containsVertex(e.target))
                .forEach(ast::addEdge);
        ASNode first = ast.copyVertexSet().stream()
                .filter(node -> node != ast.root)
                .filter(node -> ast.copyIncomingEdges(node).isEmpty())
                .min(Comparator.comparing(ASNode::getLineOfCode))
                .orElse(null);
        assert first != null;
        ast.addEdge(new Edge<>(ast.root, new ASEdge(), first));
        return ast;
    }
}
