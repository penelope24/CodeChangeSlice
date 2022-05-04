package fy.slicing.repr;

import fy.progex.parse.PDGInfo;
import fy.progex.utils.export.DotPalette;
import fy.slicing.result.CFGTrackResult;
import ghaffarian.graphs.Edge;
import ghaffarian.nanologger.Logger;
import ghaffarian.progex.graphs.AbstractProgramGraph;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SliceSubGraph extends AbstractProgramGraph<CFNode, CFEdge> {
    public PDGInfo pdgInfo;
    Set<PDNode> dataNodes;
    Set<Edge<PDNode, DDEdge>> dataFlowEdges;

    public SliceSubGraph(PDGInfo pdgInfo, CFGTrackResult<PDNode,DDEdge,CFNode,CFEdge> result) {
        super();
        this.pdgInfo = pdgInfo;
        result.getResControlNodes().forEach(this::addVertex);
        result.getResControlFlowEdges().forEach(this::addEdge);
        dataNodes = result.getResDataNodes();
        dataFlowEdges = result.getResDataFlowEdges();
    }

    @Override
    public void exportDOT(String base) throws IOException {
        File file = new File(base);
        if (!file.exists()) {
            file.mkdir();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(base).append("/");
        sb.append("IPDG.dot");
        String dotFilePath = sb.toString();
//        System.out.println("exporting to : " + dotFilePath);
        try (PrintWriter dot = new PrintWriter(dotFilePath, "UTF-8")) {
            dot.println("digraph " + "SLICE {");
            Map<CFNode, String> controlFlowNodeIdMap = new LinkedHashMap<>();
            Map<PDNode, String> dataFowNodeIdMap = new LinkedHashMap<>();
            dot.println("  // graph-vertices");
            int nodeCount = 1;
            for (CFNode cfNode : this.allVertices) {
                String name = "v" + nodeCount++;
                controlFlowNodeIdMap.put(cfNode, name);
                PDNode pdNode = cfNode.getPDNode();
                if (pdNode != null)
                    dataFowNodeIdMap.put(pdNode, name);
                StringBuilder label = new StringBuilder("  [label=\"");
                if (cfNode.getLineOfCode() > 0)
                    label.append(cfNode.getLineOfCode()).append(":  ");
                String coloredDotStr = DotPalette.getColoredNodeStr(cfNode);
                label.append(StringUtils.escape(cfNode.getCode())).append("\"").append(coloredDotStr).append("];");
                dot.println("  " + name + label.toString());
            }
            for (PDNode pdNode : this.dataNodes) {
                if (!dataFowNodeIdMap.containsKey(pdNode)) {
                    String name = "v" + nodeCount++;
                    dataFowNodeIdMap.put(pdNode, name);
                    StringBuilder label = new StringBuilder("  [label=\"");
                    if (pdNode.getLineOfCode() > 0)
                        label.append(pdNode.getLineOfCode()).append(":  ");
                    label.append(StringUtils.escape(pdNode.getCode())).append("\"];");
                    dot.println("  " + name + label.toString());
                }
            }
            dot.println("  // graph-edges");
            for (Edge<CFNode, CFEdge> controlFlowEdge : this.allEdges) {
                String src = controlFlowNodeIdMap.get(controlFlowEdge.source);
                String trg = controlFlowNodeIdMap.get(controlFlowEdge.target);
                String edgeDotStr = DotPalette.getEdgeDotStr(controlFlowEdge);
                dot.println("  " + src + " -> " + trg + edgeDotStr +
                        "label=\"" + controlFlowEdge.label.type + "\"];");
            }
            for (Edge<PDNode, DDEdge> dataEdge : this.dataFlowEdges) {
                String src = dataFowNodeIdMap.get(dataEdge.source);
                String trg = dataFowNodeIdMap.get(dataEdge.target);
                String edgeDotStr = DotPalette.getEdgeDotStr(dataEdge);
                dot.println("  " + src + " -> " + trg + edgeDotStr + "label=\" (" + dataEdge.label.var + ")\"];");
            }
            dot.println("  // end-of-graph\n}");
        } catch (UnsupportedEncodingException ex) {
            Logger.error(ex);
        }
    }

    @Override
    public void exportGML(String s) throws IOException {

    }

    @Override
    public void exportJSON(String s) throws IOException {

    }

    public Set<PDNode> getDataNodes() {
        return dataNodes;
    }

    public Set<Edge<PDNode, DDEdge>> getDataFLowEdges() {
        return dataFlowEdges;
    }
}
