package fy.CDS.data;

import fy.CDS.export.DotPalette;
import ghaffarian.graphs.Edge;
import ghaffarian.nanologger.Logger;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.utils.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

public class InterSlice extends Slice{

    public InterSlice(SliceManager sliceManager) {
        super(sliceManager);
    }

    @Override
    public void exportDOT(String dotFileName) throws IOException {
        System.out.println("exporting to : " + dotFileName);
        try (PrintWriter dot = new PrintWriter(dotFileName, "UTF-8")) {
            dot.println("digraph " + "INTER_PROCEDURE_SLICE {");
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
}
