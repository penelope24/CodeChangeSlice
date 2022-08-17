package fy.CDS.data;

import com.github.javaparser.ast.Node;
import fy.PROGEX.parse.PDGInfo;
import fy.CDS.export.DotPalette;
import ghaffarian.graphs.Edge;
import ghaffarian.nanologger.Logger;
import ghaffarian.progex.NodeType;
import ghaffarian.progex.graphs.AbstractProgramGraph;
import ghaffarian.progex.graphs.ast.ASEdge;
import ghaffarian.progex.graphs.ast.ASNode;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.utils.StringUtils;
import org.eclipse.jgit.diff.Edit;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Slice extends AbstractProgramGraph<CFNode, CFEdge> {
    public SliceManager sliceManager;
    public PDGInfo pdgInfo;
    public Edit.Type editType;
    public Set<PDNode> dataNodes = new HashSet<>();
    public Set<Edge<PDNode, DDEdge>> dataFlowEdges = new HashSet<>();
    public List<AbstractSyntaxTree> subAstTrees;

    public Slice() {
    }

    public Slice(SliceManager sliceManager) {
        super();
        this.sliceManager = sliceManager;
        this.pdgInfo = sliceManager.pdgInfo;
        this.dataNodes = sliceManager.resDataFlowNodes;
        this.dataFlowEdges = sliceManager.resDataFlowEdges;
        sliceManager.resControlFlowNodes.forEach(this::addVertex);
        sliceManager.resControlFlowEdges.forEach(this::addEdge);
        boolean no_island = allVertices.stream()
                .filter(node -> node.getType() != NodeType.ROOT)
                .noneMatch(node -> this.copyIncomingEdges(node).isEmpty());
        assert no_island;
    }

    @Override
    public void exportDOT(String dotFileName) throws IOException {
        System.out.println("exporting to : " + dotFileName);
        try (PrintWriter dot = new PrintWriter(dotFileName, "UTF-8")) {
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

    public void add(Slice slice) {
        this.addGraph(slice);
        this.dataNodes.addAll(slice.dataNodes);
        this.dataFlowEdges.addAll(slice.dataFlowEdges);
    }

    public Set<PDNode> getDataNodes() {
        return dataNodes;
    }

    public Set<Edge<PDNode, DDEdge>> getDataFlowEdges() {
        return dataFlowEdges;
    }

    public SliceManager getSliceManager() {
        return sliceManager;
    }

    public PDGInfo getPdgInfo() {
        return pdgInfo;
    }

    public Edit.Type getEditType() {
        return editType;
    }

    public void setEditType(Edit.Type editType) {
        this.editType = editType;
    }
}
