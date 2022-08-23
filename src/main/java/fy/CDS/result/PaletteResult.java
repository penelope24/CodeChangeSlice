package fy.CDS.result;

import ghaffarian.progex.graphs.cfg.CFNode;

import java.util.Set;

public class PaletteResult {

    public Set<CFNode> startNodes;
    public CFNode entryNode;
    public Set<CFNode> dataBindNodes;
    public Set<CFNode> controlBindNodes;
    public Set<CFNode> callSites;
    public Set<CFNode> exitNodes;

    public PaletteResult(Set<CFNode> startNodes, CFNode entryNode, Set<CFNode> dataBindNodes, Set<CFNode> controlBindNodes, Set<CFNode> callSites, Set<CFNode> exitNodes) {
        this.startNodes = startNodes;
        this.entryNode = entryNode;
        this.dataBindNodes = dataBindNodes;
        this.controlBindNodes = controlBindNodes;
        this.callSites = callSites;
        this.exitNodes = exitNodes;
    }
}
