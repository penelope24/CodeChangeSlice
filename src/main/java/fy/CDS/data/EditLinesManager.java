package fy.CDS.data;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;
import org.eclipse.jgit.diff.Edit;

import java.util.*;
import java.util.stream.Collectors;

public class EditLinesManager {
    public PDGInfo pdgInfo;
    public List<Integer> hunk;
    public Edit.Type editType;
    public Set<CFNode> extraNodes = new HashSet<>(); // will move to commit diff processor
    public Set<CFNode> entryNodes = new HashSet<>();
    public List<Integer> validLines = new ArrayList<>();
    public List<Integer> invalidLines = new ArrayList<>();
    public Map<CFNode, List<PDNode>> entry2startNodes = new LinkedHashMap<>();
    public List<PDNode> startNodes = new ArrayList<>();

    public EditLinesManager(PDGInfo pdgInfo, List<Integer> hunk) {
        this.pdgInfo = pdgInfo;
        this.hunk = hunk;
        this.entryNodes = analyzeEntryNodes(pdgInfo);
        analyzeHunk();
//        analyzeExtraNodes();
    }

    private void analyzeHunk(){
        this.hunk.forEach(line -> {
            PDNode chNode = pdgInfo.ddg.copyVertexSet().stream()
                    .filter(node -> node.getLineOfCode() == line)
                    .findFirst().orElse(null);
            if (chNode != null) {
                startNodes.add(chNode);
                validLines.add(line);
            }
            else {
                invalidLines.add(line);
            }
        });
        assert hunk.size() == validLines.size() + invalidLines.size();
        Map<PDNode, CFNode> reversedMap = new LinkedHashMap<>();
        validLines.forEach(line -> {
            CFNode nearestEntryNode = findNearestEntryNode(this.entryNodes, line);
            PDNode startNode = startNodes.stream()
                    .filter(node -> node.getLineOfCode() == line)
                    .findFirst().orElse(null);
            assert startNode != null && nearestEntryNode != null;
            reversedMap.put(startNode, nearestEntryNode);
        });
        this.entry2startNodes = reversedMap.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
    }

    public static Set<CFNode> analyzeEntryNodes(PDGInfo pdgInfo) {
        return pdgInfo.cfg.copyVertexSet().stream()
                .filter(node -> node.getType() == NodeType.ROOT)
                .collect(Collectors.toSet());
    }

    public static CFNode findNearestEntryNode(Set<CFNode> entryNodes, int start_pos) {
        return entryNodes.stream()
            .filter(node -> node.getLineOfCode() <= start_pos)
            .min(Comparator.comparing(node -> Math.abs(node.getLineOfCode() - start_pos)))
            .orElse(null);
    }

    // TODO: 2022/8/18
    public DataDependenceGraph getSubGraphByEntryNode () {
//        if (this.entryNodes.size() < 1) return null;
//        if (this.entryNodes.size() == 1) return pdgInfo.ddg;
//        MethodEntrySubGraphSolver solver = new MethodEntrySubGraphSolver(pdgInfo, "", nearestEntryNode, entryNodes);
//        return solver.getSubDDG();
        return null;
    }
}
