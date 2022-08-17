package fy.PROGEX.parse;

import ghaffarian.progex.graphs.pdg.DataDependenceGraph;

/**
 *  phase：生成PDG之前
 */
public class DDGParser {

    PDGInfo pdgInfo;
    DataDependenceGraph graph;

    public DDGParser(PDGInfo pdgInfo) {
        this.pdgInfo = pdgInfo;
        this.graph = pdgInfo.ddg;
    }

    /**
     * 分析dataflow，补充PROGEX分析不到的几种数据流情况
     *
     *  1. field access
     *  2. type cast
     *  3. instance of
     *  4. inter-procedure
     */
    public void parse() {
//        CompilationUnit cu = pdgInfo.fileSnapShot.cu;
//        if (cu == null) {
//            return;
//        }
//        Set<PDNode> vertexSet = graph.copyVertexSet();
//        List<PDNode> entries = pdgInfo.ddgEntryNodes;
//        // this expr
//        Map<PDNode, List<String>> thisVarUsesMap = new HashMap<>();
//        for (PDNode node : vertexSet) {
//            Arrays.stream(node.getAllUSEs())
//                    .filter(s -> s.startsWith("$THIS"))
//                    .forEach(s -> {
//                        thisVarUsesMap.computeIfAbsent(node, k -> new ArrayList<>()).add(s);
//                    });
//        }
//        Map<PDNode, List<String>> thisVarDefsMap = new HashMap<>();
//        for (PDNode node : vertexSet) {
//            Arrays.stream(node.getAllDEFs())
//                    .filter(s -> s.startsWith("$THIS"))
//                    .forEach(s -> {
//                        thisVarDefsMap.computeIfAbsent(node, k -> new ArrayList<>()).add(s);
//                    });
//        }
//        for (PDNode node : thisVarUsesMap.keySet()) {
//            PDNode entry = entries.stream()
//                    .filter(node1 -> node1.getLineOfCode() < node.getLineOfCode())
//                    .min(Comparator.comparing(node1 -> Math.abs(node.getLineOfCode() - node1.getLineOfCode())))
//                    .orElse(null);
//            if (entry != null) {
//                for (String use : thisVarUsesMap.get(node)) {
//                    add_edge(entry, node, use);
//                    PDNode toDel = thisVarDefsMap.keySet().stream()
//                            .filter(node1 -> thisVarDefsMap.get(node1).contains(use))
//                            .findFirst().orElse(null);
//                    assert toDel != null;
//                    delete_edge(toDel, node, use);
//                }
//            }
//        }
//        // field access
//        cu.findAll(FieldAccessExpr.class).forEach(fieldAccessExpr -> {
//            if (!fieldAccessExpr.getScope().isThisExpr()) {
//                int n = fieldAccessExpr.getBegin().get().line;
//                PDNode node = vertexSet.stream()
//                        .filter(node1 -> node1.getLineOfCode() == n)
//                        .findFirst().orElse(null);
//                if (node != null) {
//                    String var = fieldAccessExpr.getScope().toString();
//                    node.addUSE(fieldAccessExpr.getScope().toString());
//                    vertexSet.stream()
//                            .filter(node1 -> Arrays.asList(node1.getAllDEFs()).contains(var))
//                            .forEach(node1 -> add_edge(node1, node, var));
//                }
//            }
//        });
//        // cast expression
//        cu.findAll(CastExpr.class).forEach(castExpr -> {
//            int n = castExpr.getBegin().get().line;
//            PDNode node = graph.copyVertexSet().stream()
//                    .filter(node1 -> node1.getLineOfCode() == n)
//                    .findFirst().orElse(null);
//            if (node != null) {
//                String var = castExpr.getExpression().toString();
//                node.addUSE(castExpr.getExpression().toString());
//                vertexSet.stream()
//                        .filter(node1 -> Arrays.asList(node1.getAllDEFs()).contains(var))
//                        .forEach(node1 -> add_edge(node1, node, var));
//            }
//        });
//        // instance of expr
//        cu.findAll(InstanceOfExpr.class).forEach(instanceOfExpr -> {
//            int n = instanceOfExpr.getBegin().get().line;
//            PDNode node = graph.copyVertexSet().stream()
//                    .filter(node1 -> node1.getLineOfCode() == n)
//                    .findFirst().orElse(null);
//            if (node != null) {
//                String var = instanceOfExpr.getExpression().toString();
//                node.addUSE(instanceOfExpr.getExpression().toString());
//                vertexSet.stream()
//                        .filter(node1 -> Arrays.asList(node1.getAllDEFs()).contains(var))
//                        .forEach(node1 -> add_edge(node1, node, var));
//            }
//        });
//    }
//
//    private boolean has_edge(Set<Edge<PDNode, DDEdge>> edgeSet, Edge<PDNode, DDEdge> edge) {
//        return edgeSet.stream()
//                .anyMatch(edge1 -> Objects.equals(edge1.target, edge.target) && Objects.equals(edge1.source, edge.source)
//                        && Objects.equals(edge1.label.type, edge.label.type) && Objects.equals(edge1.label.var, edge.label.var));
//    }
//
//    private void add_edge(PDNode src, PDNode tgt, String var) {
//        Edge<PDNode, DDEdge> edge = new Edge<>(src, new DDEdge(DDEdge.Type.FLOW, var), tgt);
//        if (!has_edge(graph.copyEdgeSet(), edge)) {
//            graph.addVertex(src);
//            graph.addVertex(tgt);
//            graph.addEdge(edge);
//        }
//    }
//
//    private void delete_edge(PDNode src, PDNode tgt, String var) {
//        Edge<PDNode, DDEdge> toDel = graph.copyEdgeSet().stream()
//                .filter(edge -> Objects.equals(edge.source, src) && Objects.equals(edge.target, tgt)
//                        )
//                .findFirst().orElse(null);
//        System.out.println(toDel);
//        if (toDel != null) {
//            graph.removeEdge(toDel);
//        }
    }
}
