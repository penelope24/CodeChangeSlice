package fy.PROGEX.parse;

import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;

import java.util.*;

public class PDGInfo {
    public ProgramDependeceGraph pdg;
    public ControlDependenceGraph cdg;
    public ControlFlowGraph cfg;
    public DataDependenceGraph ddg;
    public Map<String, PDNode> uid2ddNodes = new LinkedHashMap<>();
    public Map<String, PDNode> uid2cdNodes = new LinkedHashMap<>();
    public Map<String, CFNode> uid2cfNodes = new LinkedHashMap<>();
    public Map<String, CFNode> interUid2cfNodes = new LinkedHashMap<>();

    public PDGInfo(ProgramDependeceGraph pdg) {
        this.pdg = pdg;
        this.cdg = pdg.CDS;
        this.cfg = pdg.DDS.getCFG();
        this.ddg = pdg.DDS;
        init_global_info();
    }

    private void init_global_info() {
        ddg.copyVertexSet().forEach(node -> uid2ddNodes.put(node.getUid(), node));
        cdg.copyVertexSet().forEach(node -> uid2cdNodes.put(node.getUid(), node));
        cfg.copyVertexSet().forEach(node -> uid2cfNodes.put(node.getUid(), node));
//        cfg.copyVertexSet().forEach(node -> interUid2cfNodes.
//                put(node.getUid() + CustomSeparator.getCustomSepSymbol() + javaFile.getAbsolutePath(), node));
    }

    public PDNode findDataNode(PDNode cdNode) {
        return uid2ddNodes.get(cdNode.getUid());
    }

    public PDNode findDataNode(CFNode cfNode) {
        return uid2ddNodes.get(cfNode.getUid());
    }

    public PDNode findCDNode(PDNode ddNode) {
        return uid2cdNodes.get(ddNode.getUid());
    }

    public PDNode findCDNode(CFNode cfNode) {
        return uid2cdNodes.get(cfNode.getUid());
    }

    public CFNode findCFNodeByDDNode(PDNode ddNode) {
        return uid2cfNodes.get(ddNode.getUid());
    }

    public CFNode findCFNodeByCDNode(PDNode cdNode) {
        return uid2cfNodes.get(cdNode.getUid());
    }
}
