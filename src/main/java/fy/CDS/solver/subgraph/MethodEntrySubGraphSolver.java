package fy.CDS.solver.subgraph;

import fy.PROGEX.parse.PDGInfo;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.*;

public class MethodEntrySubGraphSolver  {
    PDGInfo pdgInfo;
    String fileName;
    ControlFlowGraph cfg;
    DataDependenceGraph ddg;
    CFNode entryNode;
    PDNode entryPDNode;
    Set<CFNode> allEntryNodes;

    public MethodEntrySubGraphSolver(PDGInfo pdgInfo, String fileName, CFNode entryNode, Set<CFNode> allEntryNodes) {
        this.pdgInfo = pdgInfo;
        this.fileName = fileName;
        this.cfg = pdgInfo.cfg;
        this.ddg = pdgInfo.ddg;
        this.entryNode = entryNode;
        this.entryPDNode = pdgInfo.findDataNode(entryNode);
        this.allEntryNodes = allEntryNodes;
    }

    public ControlFlowGraph getSubCFG() {
        String name = getSubGraphName();
        ControlFlowGraph subCFG = new ControlFlowGraph(name);
        // bfs
        Deque<Edge<CFNode, CFEdge>> visiting = new ArrayDeque<>();
        Deque<Edge<CFNode, CFEdge>> visited = new ArrayDeque<>();
        Edge<CFNode, CFEdge> dummy = new Edge<>(null, null, entryNode);
        visiting.add(dummy);
        while (!visiting.isEmpty()) {
            Edge<CFNode, CFEdge> edge = visiting.pop();
            if (edge.source != null) {
                visited.add(edge);
            }
            CFNode node = edge.target;
            subCFG.addVertex(node);
            cfg.copyOutgoingEdges(node).forEach(e -> {
                if (!visited.contains(e)) {
                    visiting.add(e);
                }
            });
        }
        visited.forEach(subCFG::addEdge);
        return subCFG;
    }

    public DataDependenceGraph getSubDDG() {
        String name = getSubGraphName();
        DataDependenceGraph subDDG = new DataDependenceGraph(name);
        ControlFlowGraph subCFG = getSubCFG();
        subDDG.attachCFG(subCFG);
        if (entryPDNode == null) {
            return subDDG;
        }
        // bfs
        Deque<Edge<PDNode, DDEdge>> visiting = new ArrayDeque<>();
        Deque<Edge<PDNode, DDEdge>> visited = new ArrayDeque<>();
        Edge<PDNode, DDEdge> dummy = new Edge<>(null, null, entryPDNode);
        visiting.add(dummy);
        while (!visiting.isEmpty()) {
            Edge<PDNode, DDEdge> edge = visiting.pop();
            PDNode node = edge.target;
            if (!subDDG.containsVertex(node)) {
                subDDG.addVertex(node);
            }
            if (edge.source != null) {
                visited.add(edge);
            }
            ddg.copyOutgoingEdges(node).forEach(e -> {
                if (!visited.contains(e)) {
                    visiting.add(e);
                }
            });
        }
        visited.forEach(subDDG::addEdge);
        return subDDG;
    }


    private String getSubGraphName() {
        List<CFNode> nodeList = new ArrayList<>(this.allEntryNodes);
        int index = nodeList.indexOf(entryNode);
        return fileName + "_subgraph_" + index + ".java";
    }
}
