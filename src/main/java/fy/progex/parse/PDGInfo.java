package fy.progex.parse;

import com.github.javaparser.ast.expr.MethodCallExpr;
import fy.commit.repr.SnapShot;
import fy.utils.custom.CustomSeparator;
import ghaffarian.graphs.DepthFirstTraversal;
import ghaffarian.graphs.GraphTraversal;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;

import java.util.*;

public class PDGInfo {
    public String abs_path;
    public String rel_path;
    public SnapShot snapShot;
    public List<Edit> edits;
    // graphs
    public ProgramDependeceGraph pdg;
    public ControlDependenceGraph cdg;
    public ControlFlowGraph cfg;
    public DataDependenceGraph ddg;
    public Map<String, PDNode> uid2ddNodes = new LinkedHashMap<>();
    public Map<String, PDNode> uid2cdNodes = new LinkedHashMap<>();
    public Map<String, CFNode> uid2cfNodes = new LinkedHashMap<>();
    public Map<String, CFNode> interUid2cfNodes = new LinkedHashMap<>();
    public List<CFNode> cfgEntryNodes = new ArrayList<>();
    public List<PDNode> ddgEntryNodes = new ArrayList<>();
    public List<PDNode> cdgEntryNodes = new ArrayList<>();
    public Map<CFNode,CFNode> callingMap = new HashMap<>();

    public PDGInfo(ProgramDependeceGraph pdg) {
        this.pdg = pdg;
        this.cdg = pdg.CDS;
        this.cfg = pdg.DDS.getCFG();
        this.ddg = pdg.DDS;
        this.abs_path = pdg.FILE_NAME.getAbsolutePath();
        this.rel_path = pdg.FILE_NAME.getName();
        init_global_info();
    }

    private void init_global_info() {
        ddg.copyVertexSet().forEach(node -> uid2ddNodes.put(node.getUid(), node));
        cdg.copyVertexSet().forEach(node -> uid2cdNodes.put(node.getUid(), node));
        cfg.copyVertexSet().forEach(node -> uid2cfNodes.put(node.getUid(), node));
        cfg.copyVertexSet().forEach(node -> interUid2cfNodes.
                put(node.getUid() + CustomSeparator.getCustomSepSymbol() + abs_path, node));
    }

    public void parse() {
        CDGParser cdgParser = new CDGParser(this);
        cdgParser.parse();
        CFGParser cfgParser = new CFGParser(this);
        cfgParser.parse();
        DDGParser ddgParser = new DDGParser(this);
        ddgParser.parse();
    }

    public void analyzePDGMaps() {
        ddg.copyVertexSet().forEach(node -> node.setProperty("pdg", pdg));
        cdg.copyVertexSet().forEach(node -> node.setProperty("pdg", pdg));
        cfg.copyVertexSet().forEach(node -> node.setProperty("pdg", pdg));
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

    public void setCfgEntryNodes(List<CFNode> cfgEntryNodes) {
        this.cfgEntryNodes = cfgEntryNodes;
    }

    public void setDdgEntryNodes(List<PDNode> ddgEntryNodes) {
        this.ddgEntryNodes = ddgEntryNodes;
    }

    public void setCdgEntryNodes(List<PDNode> cdgEntryNodes) {
        this.cdgEntryNodes = cdgEntryNodes;
    }

    public void setCallingMap(Map<CFNode, CFNode> callingMap) {
        this.callingMap = callingMap;
    }

    public void addCallingRelation(CFNode caller, CFNode callee) {
        this.callingMap.put(caller, callee);
    }

    public void setFileSnapShot(SnapShot snapShot) {
        this.snapShot = snapShot;
    }

    public void setEdits(List<Edit> edits) {
        this.edits = edits;
    }
}
