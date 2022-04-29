package fy.progex.build;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import fy.progex.graphs.IPDG;
import fy.progex.parse.PDGInfo;
import fy.progex.utils.data.MethodKey;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class IPDGBuilder {

    public static IPDG buildForAll(String[] javaFiles, JavaSymbolSolver symbolSolver) throws IOException {
        ControlFlowGraph icfg = new ControlFlowGraph("ICFG.java");
        ProgramDependeceGraph[] pdgs = PDGBuilder.buildForAll("Java", javaFiles);
        // get analyze list
        List<PDGInfo> worklist = new ArrayList<>();
        for (int i=0; i<pdgs.length; i++) {
            PDGInfo pdgInfo = new PDGInfo(pdgs[i], symbolSolver);
            pdgInfo.analyzePDGMaps();
            worklist.add(pdgInfo);
        }
        // key to entry nodes
        Map<String, CFNode> key2Entry = new HashMap<>();
        for (PDGInfo pdgInfo : worklist) {
            CompilationUnit cu = pdgInfo.cu;
            List<CFNode> entries = pdgInfo.analyzeEntryNodes();
            for (CFNode entryNode : entries) {
                // analyze qualified name
                MethodDeclaration md = cu.findAll(MethodDeclaration.class).stream()
                        .filter(m -> m.getRange().get().begin.line == entryNode.getLineOfCode())
                        .findFirst().orElse(null);
                if (md != null) {
                    String qualifiedSignature = md.resolve().getQualifiedSignature();
                    key2Entry.put(qualifiedSignature, entryNode);
                }
                icfg.addMethodEntry(entryNode);
            }
        }
        // parse call relations
        Map<CFNode, CFNode> callMap = new HashMap<>();
        for (PDGInfo pdgInfo : worklist) {
            Map<CFNode, List<String>> call2keys = pdgInfo.analyzeCallSites();
            for (CFNode caller : call2keys.keySet()) {
                List<String> keys = call2keys.get(caller);
                for(String key : keys) {
                    CFNode callee = key2Entry.get(key);
                    if (callee != null) {
                        callMap.put(caller, callee);
                        pdgInfo.addCallingRelation(caller, callee);
                    }
                }
            }
        }
        // add each cfg to icfg, each ddg to iddg
        worklist.forEach(parser -> icfg.addGraph(parser.cfg));
        // add call relationships
        callMap.forEach((src, tgt) -> {
            if (!icfg.containsEdge(src, tgt)) {
                icfg.addEdge(new Edge<>(src, new CFEdge(CFEdge.Type.CALLS), tgt));
                for (CFNode exitNode : (ArrayList<CFNode>) tgt.getProperty("exits")) {
                    icfg.addEdge(new Edge<>(exitNode, new CFEdge(CFEdge.Type.RETURN), src));
                }
            }
        });

        // parse each pdg
        worklist.forEach(interPDGParsedInfo -> {
            interPDGParsedInfo.parse(icfg);
        });
        IPDG ipdg = new IPDG(icfg, worklist);
        return ipdg;
    }
}
