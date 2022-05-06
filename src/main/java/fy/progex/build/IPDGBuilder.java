package fy.progex.build;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import fy.commit.repr.AtomEdit;
import fy.commit.repr.FileDiff;
import fy.progex.graphs.IPDG;
import fy.progex.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.PDGBuilder;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IPDGBuilder {
    String version;
    Repository repository;
    List<FileDiff> fileDiffs;
    Map<Edit, AtomEdit> atomEditMap;

    public IPDGBuilder(String version, Repository repository, List<FileDiff> fileDiffs, Map<Edit, AtomEdit> atomEditMap) {
        this.version = version;
        this.repository = repository;
        this.fileDiffs = fileDiffs;
        this.atomEditMap = atomEditMap;
    }

    public IPDG build() throws IOException {
        ControlFlowGraph icfg = new ControlFlowGraph("ICFG.java");
        List<String> javaFiles = fileDiffs.stream()
                .map(diff -> diff.javaFile)
                .collect(Collectors.toList());
        ProgramDependeceGraph[] pdgs;
        try {
            pdgs = PDGBuilder.buildForAll("Java", javaFiles.toArray(new String[0]));
        } catch (Exception e) {
            return null;
        }
        // get analyze list
        List<PDGInfo> worklist = new ArrayList<>();
        for (int i=0; i<pdgs.length; i++) {
            PDGInfo pdgInfo = new PDGInfo(pdgs[i]);
            FileDiff fileDiff = fileDiffs.stream()
                    .filter(diff -> diff.javaFile.equals(pdgInfo.abs_path))
                    .findFirst().orElse(null);
            assert fileDiff != null;
            pdgInfo.parseFromFileDiff(fileDiff);
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
        // analyze edits
        for (PDGInfo pdgInfo : worklist) {
            DiffEntry diffEntry = pdgInfo.diffEntry;
            EditList edits = getEditList(diffEntry);
            for (Edit edit : edits) {
                switch (version) {
                    case "v1":
                        atomEditMap.computeIfAbsent(edit, AtomEdit::new).setPdgInfo1(pdgInfo);
                        break;
                    case "v2":
                        atomEditMap.computeIfAbsent(edit, AtomEdit::new).setPdgInfo2(pdgInfo);
                        break;
                }
            }
        }
        return new IPDG(icfg, worklist);
    }

    private EditList getEditList(DiffEntry diffEntry) throws IOException {
        DiffFormatter diffFormatter = new DiffFormatter(null);
        diffFormatter.setContext(0);
        diffFormatter.setRepository(repository);
        return diffFormatter.toFileHeader(diffEntry).toEditList();
    }


}
