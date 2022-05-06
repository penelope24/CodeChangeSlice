package fy.commit;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import fy.commit.entry.Logger;
import fy.commit.repr.AtomEdit;
import fy.commit.repr.CommitDiff;
import fy.commit.repr.FileDiff;
import fy.progex.build.IPDGBuilder;
import fy.progex.graphs.IPDG;
import fy.utils.jgit.JGitUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommitParser {
    public Repository repository;
    public JGitUtils jgit;
    public List<CommitDiff> commitDiffs = new ArrayList<>();

    public CommitParser(Repository repository, JGitUtils jgit) {
        this.repository = repository;
        this.jgit = jgit;
    }

    public void parse (List<RevCommit> commits) throws GitAPIException, IOException {
        jgit.reset();
        for (RevCommit commit : commits) {
            System.out.println(commit);
            RevCommit par = getMainParent(repository, commit);
            if (par == null) {
                continue;
            }
            List<DiffEntry> diffEntries = listDiffEntries(commit, par, ".java");
            // filter1
            if (diffEntries.size() > 20 || diffEntries.size() < 1) {
                continue;
            }
            // valid entries
            List<DiffEntry> validEntries = diffEntries.stream()
                    .filter(this::is_valid_entry)
                    .collect(Collectors.toList());
            // filter2
            if (validEntries.isEmpty()) {
                continue;
            }
            //res
            Map<Edit, AtomEdit> atomEditMap = new LinkedHashMap<>();
            // init graphs
            IPDG graph1;
            IPDG graph2;
            // v1
            {
                List<FileDiff> fileDiffs = new ArrayList<>();
                jgit.safe_checkout(par.getId().name());
                for (DiffEntry diffEntry : validEntries) {
                    // path
                    String path = PathUtils.getOldPath(diffEntry, repository);
                    assert path != null;
                    // symbol solver
                    JavaSymbolSolver javaSymbolSolver = init_java_symbol_solver();
                    CompilationUnit cu = init_parse_tree(path, javaSymbolSolver);
                    // file diff
                    FileDiff fileDiff = new FileDiff("v1", diffEntry, path, cu);
                    fileDiffs.add(fileDiff);
                }
                try {
                    IPDGBuilder builder = new IPDGBuilder("v1", repository, fileDiffs, atomEditMap);
                    graph1 = builder.build();
                } catch (Exception e) {
                    graph1 = null;
                    Logger.writeLog("/Users/fy/Documents/fyJavaProjects/ProgramGraphs/src/test/resources/running_log.txt", e.toString());
                }
            }
            // v2
            {
                List<FileDiff> fileDiffs = new ArrayList<>();
                jgit.safe_checkout(commit.getId().name());
                for (DiffEntry diffEntry : validEntries) {
                    // path
                    String path = PathUtils.getNewPath(diffEntry, repository);
                    assert path != null;
                    // symbol solver
                    JavaSymbolSolver javaSymbolSolver = init_java_symbol_solver();
                    CompilationUnit cu = init_parse_tree(path, javaSymbolSolver);
                    // file diff
                    FileDiff fileDiff = new FileDiff("v2", diffEntry, path, cu);
                    fileDiffs.add(fileDiff);
                }
                try {
                    IPDGBuilder builder = new IPDGBuilder("v2", repository, fileDiffs, atomEditMap);
                    graph2 = builder.build();
                } catch (Exception e) {
                    graph2 = null;
                    Logger.writeLog("/Users/fy/Documents/fyJavaProjects/ProgramGraphs/src/test/resources/running_log.txt", e.toString());
                }
            }
            if (graph1 != null || graph2 != null) {
                CommitDiff commitDiff = new CommitDiff(commit, graph1, graph2, atomEditMap);
                commitDiffs.add(commitDiff);
            }
//            CommitDiff commitDiff = new CommitDiff(commit, graph1, graph2, atomEditMap);
//            commitDiffs.add(commitDiff);
        }
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

    /**
     * 对单个文件的限制：
     *      1. 不能是file add / delete
     *      2. 不包含test
     */
    private boolean is_valid_entry(DiffEntry diffEntry) {
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

    private static RevCommit getMainParent(Repository repository, RevCommit curr) {
        if (curr.getParentCount() < 1) {
            return null;
        }
        ObjectId parId = curr.getParent(0).getId();
        try {
            RevCommit par = repository.parseCommit(parId);
            assert par.getTree() != null;
            return par;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
