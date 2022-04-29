package fy.commit;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Pair;
import fy.progex.build.IPDGBuilder;
import fy.progex.graphs.IPDG;
import fy.utils.jgit.JGitUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommitParser {
    Repository repository;
    JGitUtils jgit;

    public CommitParser(Repository repository, JGitUtils jgit) {
        this.repository = repository;
        this.jgit = jgit;
    }

    public List<Pair<IPDG, IPDG>> parse (List<RevCommit> commits) throws GitAPIException, IOException {
        List<Pair<IPDG, IPDG>> result = new ArrayList<>();
        for (RevCommit commit : commits) {
            RevCommit par = getMainParent(repository, commit);
            if (par == null) {
                continue;
            }
            jgit.reset();
            List<DiffEntry> diffEntries = listDiffEntries(commit, par, ".java");
            if (diffEntries.size() < 20) {
                List<DiffEntry> validEntries = diffEntries.stream()
                        .filter(this::is_valid_entry)
                        .collect(Collectors.toList());
                IPDG ipdg1;
                IPDG ipdg2;
                CombinedTypeSolver typeSolver1 = new CombinedTypeSolver();
                typeSolver1.add(new ReflectionTypeSolver());
                CombinedTypeSolver typeSolver2 = new CombinedTypeSolver();
                typeSolver2.add(new ReflectionTypeSolver());
                // v1
                jgit.safe_checkout(par.getId().name());
                List<String> validPaths1 = new ArrayList<>();
                for (DiffEntry diffEntry : validEntries) {
                    String path = PathUtils.getOldPath(diffEntry, repository);
                    // 因为在is_valid_entry中限定diffEntry为MODIFY类型，所以path不为null
                    assert path != null;
                    validPaths1.add(path);
                    typeSolver1.add(new JavaParserTypeSolver(new File(path)));
                }
                try {
                    JavaSymbolSolver symbolSolver1 = new JavaSymbolSolver(typeSolver1);
                    ipdg1 = IPDGBuilder.buildForAll(validPaths1.toArray(new String[0]), symbolSolver1);
                } catch (Exception e) {
                    ipdg1 = null;
                }
                // v2
                jgit.safe_checkout(commit.getId().name());
                List<String> validPaths2 = new ArrayList<>();
                for (DiffEntry diffEntry : validEntries) {
                    String path = PathUtils.getNewPath(diffEntry, repository);
                    // 因为在is_valid_entry中限定diffEntry为MODIFY类型，所以path不为null
                    assert path != null;
                    validPaths2.add(path);
                    typeSolver2.add(new JavaParserTypeSolver(new File(path)));
                }
                try {
                    JavaSymbolSolver symbolSolver2 = new JavaSymbolSolver(typeSolver2);
                    ipdg2 = IPDGBuilder.buildForAll(validPaths2.toArray(new String[0]), symbolSolver2);
                } catch (Exception e) {
                    ipdg2 = null;
                }
                // fed to result
                if (ipdg1 != null && ipdg2 != null) {
                    Pair<IPDG, IPDG> pair = new Pair<>(ipdg1, ipdg2);
                    result.add(pair);
                }
            }
        }
        return result;
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
