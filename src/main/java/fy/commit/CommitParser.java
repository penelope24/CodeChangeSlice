package fy.commit;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import fy.commit.repr.AtomEdit;
import fy.commit.repr.CommitDiff;
import fy.commit.repr.SnapShot;
import fy.progex.build.IPDGBuilder;
import fy.progex.graphs.IPDG;
import fy.progex.parse.PDGInfo;
import fy.utils.jgit.JGitUtils;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CommitParser {

    Repository repository;
    JGitUtils jgit;
    RevCommit curr;
    RevCommit par;
    int index;
    List<DiffEntry> allJavaEntries;
    List<DiffEntry> validJavaEntries;

    public CommitParser(Repository repository, JGitUtils jgit, RevCommit curr, RevCommit par, int index) throws IOException, GitAPIException {
        this.repository = repository;
        this.jgit = jgit;
        this.curr = curr;
        this.par = par;
        this.index = index;
        allJavaEntries = listDiffEntries(curr, par, ".java");
        validJavaEntries = allJavaEntries.stream()
                .filter(this::is_valid)
                .collect(Collectors.toList());
    }

    public boolean is_valid() throws IOException, GitAPIException {
        if (validJavaEntries.size() > 20) {
            return false;
        }
        return !validJavaEntries.isEmpty();
    }

    public CommitDiff parse() throws IOException, GitAPIException {
        //res
        Map<Edit, AtomEdit> atomEditMap = new LinkedHashMap<>();
        // init graphs
        IPDG graph1;
        IPDG graph2;
        // v1
        {
            List<String> javaFiles = new LinkedList<>();
            Map<String, SnapShot> pathSnapShotMap = new LinkedHashMap<>();
            Map<String, DiffEntry> pathDiffEntryMap = new LinkedHashMap<>();
            jgit.safe_checkout(par.getId().name());
            for (DiffEntry diffEntry : validJavaEntries) {
                // path
                String path = PathUtils.getOldPath(diffEntry, repository);
                assert path != null;
                javaFiles.add(path);
                pathDiffEntryMap.put(path, diffEntry);
                // symbol solver
                JavaSymbolSolver javaSymbolSolver = init_java_symbol_solver();
                CompilationUnit cu = init_parse_tree(path, javaSymbolSolver);
                // file info
                SnapShot snapShot = new SnapShot("v1", path, cu);
                pathSnapShotMap.put(path, snapShot);
            }
            List<PDGInfo> worklist = new LinkedList<>();
            ProgramDependeceGraph[] pdgArray;
            try {
                pdgArray = PDGBuilder.buildForAll("Java", javaFiles.toArray(new String[0]));
            } catch (Exception e) {
                pdgArray = new ProgramDependeceGraph[0];
            }
            for (int i=0; i<pdgArray.length; i++) {
                PDGInfo pdgInfo = new PDGInfo(pdgArray[i]);
                pdgInfo.setFileSnapShot(pathSnapShotMap.get(pdgInfo.abs_path));
                pdgInfo.analyzePDGMaps();
                // edits
                DiffEntry diffEntry = pathDiffEntryMap.get(pdgInfo.abs_path);
                EditList edits = getEditList(diffEntry);
                for (Edit edit : edits) {
                    atomEditMap.computeIfAbsent(edit, AtomEdit::new).setPdgInfo1(pdgInfo);
                }
                pdgInfo.setEdits(edits);
                worklist.add(pdgInfo);
            }
            graph1 = IPDGBuilder.build(worklist);
        }
        // v2
        {
            List<String> javaFiles = new LinkedList<>();
            Map<String, SnapShot> pathSnapShotMap = new LinkedHashMap<>();
            Map<String, DiffEntry> pathDiffEntryMap = new LinkedHashMap<>();
            jgit.safe_checkout(curr.getId().name());
            for (DiffEntry diffEntry : validJavaEntries) {
                // path
                String path = PathUtils.getNewPath(diffEntry, repository);
                assert path != null;
                javaFiles.add(path);
                pathDiffEntryMap.put(path, diffEntry);
                // symbol solver
                JavaSymbolSolver javaSymbolSolver = init_java_symbol_solver();
                CompilationUnit cu = init_parse_tree(path, javaSymbolSolver);
                // file info
                SnapShot snapShot = new SnapShot("v2", path, cu);
                pathSnapShotMap.put(path, snapShot);
            }
            List<PDGInfo> worklist = new LinkedList<>();
            ProgramDependeceGraph[] pdgArray;
            try {
                pdgArray = PDGBuilder.buildForAll("Java", javaFiles.toArray(new String[0]));
            } catch (Exception e) {
                pdgArray = new ProgramDependeceGraph[0];
            }
            for (int i=0; i<pdgArray.length; i++) {
                PDGInfo pdgInfo = new PDGInfo(pdgArray[i]);
                pdgInfo.setFileSnapShot(pathSnapShotMap.get(pdgInfo.abs_path));
                pdgInfo.analyzePDGMaps();
                // edits
                DiffEntry diffEntry = pathDiffEntryMap.get(pdgInfo.abs_path);
                EditList edits = getEditList(diffEntry);
                for (Edit edit : edits) {
                    atomEditMap.computeIfAbsent(edit, AtomEdit::new).setPdgInfo2(pdgInfo);
                }
                pdgInfo.setEdits(edits);
                worklist.add(pdgInfo);
            }
            graph2 = IPDGBuilder.build(worklist);
        }
        return new CommitDiff(curr, graph1, graph2, atomEditMap);
    }

    private List<DiffEntry> listDiffEntries(RevCommit curr, RevCommit par, String filter) throws IOException, GitAPIException {
        List<DiffEntry> diffEntries = new ArrayList<>();
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(reader, par.getTree());
            CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, curr.getTree());
            try (Git git = new Git(this.repository)) {
                diffEntries = git.diff()
                        .setOldTree(oldTree)
                        .setNewTree(newTree)
                        .setPathFilter(PathSuffixFilter.create(filter))
                        .call();
                return diffEntries;
            }
        }
    }

    private boolean is_valid(DiffEntry diffEntry) {
        if (diffEntry.getChangeType() != DiffEntry.ChangeType.MODIFY) {
            return false;
        }
        // name
        String path = diffEntry.getOldPath();
        String[] split = path.split("/");
        String name = split[split.length-1].replace(".java", "");
        if (name.toLowerCase().contains("test")) {
            return false;
        }
        return true;
    }

    private JavaSymbolSolver init_java_symbol_solver() {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(repository.getDirectory().getAbsoluteFile()));
        return new JavaSymbolSolver(typeSolver);
    }

    private CompilationUnit init_parse_tree(String path, JavaSymbolSolver javaSymbolSolver) throws FileNotFoundException {
        StaticJavaParser.getConfiguration().setSymbolResolver(javaSymbolSolver);
        return StaticJavaParser.parse(new File(path));
    }

    private EditList getEditList(DiffEntry diffEntry) throws IOException {
        DiffFormatter diffFormatter = new DiffFormatter(null);
        diffFormatter.setContext(0);
        diffFormatter.setRepository(repository);
        return diffFormatter.toFileHeader(diffEntry).toEditList();
    }
}
