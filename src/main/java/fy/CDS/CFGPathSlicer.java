package fy.CDS;

import fy.CDS.data.Slice;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;

import java.util.*;

public class CFGPathSlicer {
    // init
    Slice slice;
    String sep = "||";
    int num = 0;
    // analyze
    Stack<CFNode> connectionPath = new Stack<>();
    List<Stack<CFNode>> connectionPaths = new ArrayList<>();
    ArrayDeque<Edge<CFNode, CFEdge>> visited = new ArrayDeque<>();
    // result
    List<Slice> cfgPaths = new LinkedList<>();

    public CFGPathSlicer(Slice slice) {
        this.slice = slice;
    }

    public void slice () {
        CFNode src = slice.sliceManager.entryNode;
        Set<CFNode> tgts = slice.sliceManager.exitNodes;
        assert src != null;
        assert tgts != null && !tgts.isEmpty();
        connectionPath.push(src);
        findAllPaths(src, tgts);
        addDatFlowEdges();
    }

    public void slice_test(CFNode src, CFNode tgt) {
        connectionPath.push(src);
        findAllPaths(src, tgt);
    }

    public void findAllPaths(CFNode src, CFNode tgt) {
//        System.out.println(src + "  ->  " + tgt);
        Set<Edge<CFNode, CFEdge>> outs = slice.copyOutgoingEdges(src);
        for (Edge<CFNode, CFEdge> edge : outs) {
            CFNode node = edge.target;
            // if terminate
            if (node.equals(tgt)) {
                Stack<CFNode> temp = new Stack<>();
                for (CFNode node1 : connectionPath)
                    temp.add(node1);
                connectionPaths.add(temp);
                // add
                Slice cfgPath = new Slice();
                temp.forEach(cfgPath::addVertex);
                cfgPath.addVertex(node);
                visited.forEach(e -> {
                    cfgPath.addEdge(e);
                });
                cfgPath.addEdge(edge);
                cfgPaths.add(cfgPath);
            }
            else if (!visited.contains(edge)){
                connectionPath.push(node);
                visited.push(edge);
                findAllPaths(node, tgt);
                connectionPath.pop();
                visited.pop();
            }
        }
    }

    public void findAllPaths(CFNode src, Set<CFNode> tgts) {
        Set<Edge<CFNode, CFEdge>> outs = slice.copyOutgoingEdges(src);
        for (Edge<CFNode, CFEdge> edge : outs) {
            CFNode node = edge.target;
            if (tgts.contains(node)) {
                Stack<CFNode> temp = new Stack<>();
                for (CFNode node1 : connectionPath)
                    temp.add(node1);
                connectionPaths.add(temp);
                // add
                Slice cfgPath = new Slice();
                temp.forEach(cfgPath::addVertex);
                cfgPath.addVertex(node);
                visited.forEach(e -> {
                    cfgPath.addEdge(e);
                });
                cfgPath.addEdge(edge);
                cfgPaths.add(cfgPath);
            } else if (!visited.contains(edge)){
                connectionPath.push(node);
                visited.push(edge);
                findAllPaths(node, tgts);
                connectionPath.pop();
                visited.pop();
            }
        }
    }

    private void addDatFlowEdges() {
        Set<Edge<PDNode, DDEdge>> dataFlowEdges = slice.dataFlowEdges;
        for (Slice path : cfgPaths) {
            Set<CFNode> controlNodes = path.copyVertexSet();
            dataFlowEdges.forEach(edge -> {
                CFNode src = slice.sliceManager.pdgInfo.findCFNodeByDDNode(edge.source);
                CFNode tgt = slice.sliceManager.pdgInfo.findCFNodeByDDNode(edge.target);
                if (controlNodes.contains(src) && controlNodes.contains(tgt)) {
                    path.addDataFlowEdge(edge);
                }
            });
        }
    }


}
