package fy.progex.build;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import fy.commit.entry.Logger;
import fy.progex.graphs.IPDG;
import fy.progex.parse.PDGInfo;
import fy.progex.parse.type.collect.TypeCollector;
import fy.progex.parse.type.solver.MySimpleTypeSolver;
import ghaffarian.graphs.DepthFirstTraversal;
import ghaffarian.graphs.Edge;
import ghaffarian.graphs.GraphTraversal;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;

import java.io.IOException;
import java.util.*;

public class IPDGBuilder {

    public static IPDG build(List<PDGInfo> worklist) throws IOException {
        ControlFlowGraph icfg = new ControlFlowGraph("ICFG.java");
        // parse each pdg
        worklist.forEach(PDGInfo::parse);
        // add each cfg to icfg
        worklist.forEach(pdgInfo -> icfg.addGraph(pdgInfo.cfg));
//        // parse call relations
//        Map<CFNode, CFNode> callSet = new LinkedHashMap<>();
//        TypeCollector collector = new TypeCollector(worklist);
//        collector.collect();
//        for (PDGInfo pdgInfo : worklist) {
//            MySimpleTypeSolver simpleTypeSolver = new MySimpleTypeSolver(collector, pdgInfo.abs_path);
//            for (MethodCallExpr mce : simpleTypeSolver.getLocalCalls()) {
//                MethodDeclaration md = simpleTypeSolver.solveMethodCall(mce);
//                if (md != null) {
//                    CFNode caller = pdgInfo.cfg.copyVertexSet().stream()
//                            .filter(node -> node.getLineOfCode() == mce.getRange().get().begin.line)
//                            .findFirst().orElse(null);
//                }
//            }
//        }
        // call relations
        Map<CFNode, CFNode> callRelations = new LinkedHashMap<>();
        // analyze entry methods
        Map<String, CFNode> keyEntryMap = new LinkedHashMap<>();
        for (PDGInfo pdgInfo : worklist) {
            CompilationUnit cu = pdgInfo.snapShot.cu;
            CFNode[] entries = pdgInfo.cfg.getAllMethodEntries();
            for (CFNode entryNode : entries) {
                entryNode.setProperty("entry", true);
                // analyze exit nodes
                ArrayList<CFNode> exitpoints = new ArrayList<>();
                GraphTraversal<CFNode, CFEdge> iter = new DepthFirstTraversal<>(pdgInfo.cfg, entryNode);
                while (iter.hasNext()) {
                    CFNode node = iter.nextVertex();
                    if (pdgInfo.cfg.getOutDegree(node) == 0) {
                        node.setProperty("exit", true);
                        exitpoints.add(node);
                    }
                }
                entryNode.setProperty("exits", exitpoints);
                // analyze qualified name
                MethodDeclaration md = cu.findAll(MethodDeclaration.class).stream()
                        .filter(m -> m.getRange().get().begin.line == entryNode.getLineOfCode())
                        .findFirst().orElse(null);
                if (md != null) {
                    String qualifiedSignature = md.resolve().getQualifiedSignature();
                    keyEntryMap.put(qualifiedSignature, entryNode);
                }
                icfg.addMethodEntry(entryNode);
            }
        }
        // analyze call sites
        for (PDGInfo pdgInfo : worklist) {
            // list all method calls
            List<MethodCallExpr> methodCalls = pdgInfo.snapShot.cu.findAll(MethodCallExpr.class);
            // analyze full signature & rearrange to each calling nodes
            Map<CFNode, List<String>> callSiteKeysMap = new HashMap<>();
            for (MethodCallExpr mce : methodCalls) {
                // 1. 如果全名key解析失败，2.如果全名key不包含于entry method中
                String key;
                try {
                    key = mce.resolve().getQualifiedSignature();
                    if (!keyEntryMap.containsKey(key)) {
                        key = null;
                    }
                }
                catch (Exception e) {
                    key = null;
                }
                if (key != null) {
                    CFNode caller = pdgInfo.cfg.copyVertexSet().stream()
                            .filter(node -> node.getLineOfCode() == mce.getRange().get().begin.line)
                            .findFirst().orElse(null);
                    if (caller != null) {
                        caller.setProperty("callsite", true);
                        caller.setProperty("callingkey", key);
                        callSiteKeysMap.computeIfAbsent(caller, k -> new LinkedList<>()).add(key);
                    }
                }
            }
            // for each calling node, add call relation
            for (CFNode caller : callSiteKeysMap.keySet()) {
                List<String> keys = callSiteKeysMap.get(caller);
                for(String key : keys) {
                    CFNode callee = keyEntryMap.get(key);
                    if (callee != null) {
                        callRelations.put(caller, callee);
                    }
                }
            }
        }
        System.out.println("all calls: " + callRelations.size());
        Logger.writeLog("/Users/fy/Documents/fyJavaProjects/ProgramGraphs/src/test/resources/running_log.txt", "s");
        // add call relationships
        callRelations.forEach((src, tgt) -> {
            if (!icfg.containsEdge(src, tgt)) {
                icfg.addEdge(new Edge<>(src, new CFEdge(CFEdge.Type.CALLS), tgt));
                for (CFNode exitNode : (ArrayList<CFNode>) tgt.getProperty("exits")) {
                    icfg.addEdge(new Edge<>(exitNode, new CFEdge(CFEdge.Type.RETURN), src));
                }
            }
        });
        return new IPDG(icfg, worklist);
    }

}
