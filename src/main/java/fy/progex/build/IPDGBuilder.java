package fy.progex.build;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import fy.annotation.KeyMethod;
import fy.commit.PathUtils;
import fy.commit.repr.AtomEdit;
import fy.progex.graphs.IPDG;
import fy.progex.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.PDGBuilder;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IPDGBuilder {
    String version;
    Repository repository;
    List<DiffEntry> diffEntries;
    List<String> javaFiles;
    JavaSymbolSolver symbolSolver;

    public IPDGBuilder(String version, Repository repository, List<DiffEntry> diffEntries, List<String> javaFiles, JavaSymbolSolver symbolSolver) {
        this.version = version;
        this.repository = repository;
        this.diffEntries = diffEntries;
        this.javaFiles = javaFiles;
        this.symbolSolver = symbolSolver;
    }

    public IPDG build() throws IOException {
        ControlFlowGraph icfg = new ControlFlowGraph("ICFG.java");
        ProgramDependeceGraph[] pdgs = PDGBuilder.buildForAll("Java", javaFiles.toArray(new String[0]));
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
        // analyze edit lines
        analyze_edits(worklist);

        IPDG ipdg = new IPDG(icfg, worklist);
        return ipdg;
    }

    @KeyMethod
    private void analyze_edits(List<PDGInfo> worklist) throws IOException {
        switch (version) {
            case "v1": {
                for (DiffEntry diffEntry : diffEntries) {
                    PDGInfo pdgInfo = worklist.stream()
                            .filter(pdgInfo1 -> pdgInfo1.abs_path.equals(PathUtils.getOldPath(diffEntry, repository)))
                            .findFirst().orElse(null);
                    if (pdgInfo != null) {
                        List<Integer> validLineNums = analyze_valid_line_nums(pdgInfo.cu);
                        EditList edits = getEditList(diffEntry);
                        List<AtomEdit> atomEdits = new ArrayList<>();
                        for (Edit edit : edits) {
                            List<Integer> editLines = IntStream.range(edit.getBeginA() + 1, edit.getEndA() + 1)
                                    .boxed()
                                    .collect(Collectors.toList());
                            if (has_intersection(editLines, validLineNums)) {
                                AtomEdit atomEdit = new AtomEdit(pdgInfo, editLines);
                                atomEdits.add(atomEdit);
                            }
                        }
                        pdgInfo.setAtomEdits(atomEdits);
                    }
                }
                break;
            }
            case "v2": {
                for (DiffEntry diffEntry : diffEntries) {
                    PDGInfo pdgInfo = worklist.stream()
                            .filter(pdgInfo1 -> pdgInfo1.abs_path.equals(PathUtils.getNewPath(diffEntry, repository)))
                            .findFirst().orElse(null);
                    if (pdgInfo != null) {
                        List<Integer> validLineNums = analyze_valid_line_nums(pdgInfo.cu);
                        EditList edits = getEditList(diffEntry);
                        List<AtomEdit> atomEdits = new ArrayList<>();
                        for (Edit edit : edits) {
                            List<Integer> editLines = IntStream.range(edit.getBeginB() + 1, edit.getEndB() + 1)
                                    .boxed()
                                    .collect(Collectors.toList());
                            if (has_intersection(editLines, validLineNums)) {
                                AtomEdit atomEdit = new AtomEdit(pdgInfo, editLines);
                                atomEdits.add(atomEdit);
                            }
                        }
                        pdgInfo.setAtomEdits(atomEdits);
                    }
                }
                break;
            }
            default:
        }
    }

    private EditList getEditList(DiffEntry diffEntry) throws IOException {
        DiffFormatter diffFormatter = new DiffFormatter(null);
        diffFormatter.setContext(0);
        diffFormatter.setRepository(repository);
        return diffFormatter.toFileHeader(diffEntry).toEditList();
    }

    /**
     * 只有位于某个方法体内部的变动才是需要考虑的
     */
    private List<Integer> analyze_valid_line_nums(CompilationUnit cu) {
        List<Integer> valid_line_nums = new ArrayList<>();
        cu.findAll(MethodDeclaration.class).forEach(md -> {
            if (md.getRange().isPresent()) {
                int s = md.getRange().get().begin.line;
                int t = md.getRange().get().end.line;
                for (int i=s; i<t; i++) {
                    valid_line_nums.add(i);
                }
            }
        });
        return valid_line_nums;
    }

    // TODO: 2022/5/5 add is_valid_edit()
    private boolean has_intersection(List<Integer> list1, List<Integer> list2) {
        List<Integer> result = list1.stream()
                .distinct()
                .filter(list2::contains)
                .collect(Collectors.toList());
        return !result.isEmpty();
    }

}
