package fy.PROGEX.parse;

import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.pdg.PDNode;

public class CFGParser {
    PDGInfo pdgInfo;
    ControlFlowGraph graph;



    public CFGParser(PDGInfo pdgInfo) {
        this.pdgInfo = pdgInfo;
        this.graph = pdgInfo.cfg;
    }

    /**
     * 对CFG node的解析主要就是把CDG中已分析的性质转移过来
     */
    public void parse() {
        analyze_node_type();
    }

    private void analyze_node_type() {
        for (CFNode node : graph.copyVertexSet()) {
            if (node.getLineOfCode() == 0) {
                node.setType(NodeType.HELP);
                node.setBranch(false);
                node.setTerminal(false);
            }
            else {
                if (node.getCode().equals("for ( ; )")) {
                    node.setCode("for (;)");
                }
                if (node.getCode().equals(" ; ")) {
                    node.setBranch(false);
                    node.setTerminal(false);
                    continue;
                }
                PDNode cdNode = pdgInfo.findCDNode(node);
                transfer(node, cdNode);
            }
        }
    }


    private void transfer(CFNode tgt, PDNode src) {
        tgt.setType(src.getType());
        tgt.setBranch(src.isBranch());
        tgt.setTerminal(src.isTerminal());
    }
}
