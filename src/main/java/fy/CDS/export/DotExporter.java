package fy.CDS.export;

import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.DotPrinter;
import fy.CDS.data.Slice;
import fy.GW.data.CommitDiff;
import ghaffarian.graphs.Edge;
import ghaffarian.nanologger.Logger;
import ghaffarian.progex.graphs.ast.ASEdge;
import ghaffarian.progex.graphs.ast.ASNode;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.utils.StringUtils;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;


// mistyrose3
public class DotExporter {
    String outputDir;
    CommitDiff commitDiff;
    String commit_base;
    String original_graph_base;
    String slices_base;
    String subgraph_base;
    String ipdg_base;

    public DotExporter(String outputDir, CommitDiff commitDiff) {
        this.outputDir = outputDir;
        this.commitDiff = commitDiff;
        mk_commit_diff_dirs();
    }

    private void mk_commit_diff_dirs() {
        // commit base
        String v = commitDiff.getV2();
        this.commit_base = outputDir + "/" + v;
        File commitBaseFile = new File(commit_base);
        if (!commitBaseFile.exists()) {
            commitBaseFile.mkdir();
        }
        // for original graphs
        this.original_graph_base =  this.commit_base + "/" + "original";
        File originalFile = new File(this.original_graph_base);
        if (! originalFile.exists()) {
            originalFile.mkdir();
        }
        // for slices
        this.slices_base = this.commit_base + "/" + "slices";
        File slicesDirFile = new File(this.slices_base);
        if (!slicesDirFile.exists()) {
            slicesDirFile.mkdir();
        }
        // for sub entry graphs
        this.subgraph_base = this.commit_base + "/" + "subgraphs";
        File subEntryDirFile = new File(this.subgraph_base);
        if (!subEntryDirFile.exists()) {
            subEntryDirFile.mkdir();
        }
        // for ipdg
        this.ipdg_base = this.commit_base + "/" + "ipdg";
        File ipdgFile = new File(this.ipdg_base);
        if (!ipdgFile.exists()) {
            ipdgFile.mkdir();
        }
    }

    public static void exportDot(Slice slice, String dotFileName) {
        System.out.println("exporting to : " + dotFileName);
        try (PrintWriter dot = new PrintWriter(dotFileName, "UTF-8")) {
            dot.println("digraph " + "SLICE {");
            Map<CFNode, String> controlFlowNodeIdMap = new LinkedHashMap<>();
            Map<PDNode, String> dataFowNodeIdMap = new LinkedHashMap<>();
            Map<ASNode, String> asNodeStringMap = new LinkedHashMap<>();
            dot.println("  // graph-vertices");
            int nodeCount = 1;
            for (CFNode cfNode : slice.copyVertexSet()) {
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
            for (PDNode pdNode : slice.getDataNodes()) {
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
            for (Edge<CFNode, CFEdge> controlFlowEdge : slice.copyEdgeSet()) {
                String src = controlFlowNodeIdMap.get(controlFlowEdge.source);
                String trg = controlFlowNodeIdMap.get(controlFlowEdge.target);
                String edgeDotStr = DotPalette.getEdgeDotStr(controlFlowEdge);
                dot.println("  " + src + " -> " + trg + edgeDotStr +
                        "label=\"" + controlFlowEdge.label.type + "\"];");
            }
            for (Edge<PDNode, DDEdge> dataEdge : slice.getDataFlowEdges()) {
                String src = dataFowNodeIdMap.get(dataEdge.source);
                String trg = dataFowNodeIdMap.get(dataEdge.target);
                String edgeDotStr = DotPalette.getEdgeDotStr(dataEdge);
                dot.println("  " + src + " -> " + trg + edgeDotStr + "label=\" (" + dataEdge.label.var + ")\"];");
            }
            // sub asts
//            for (AbstractSyntaxTree ast : slice.subAstTrees) {
//                // sub ast nodes
//                for (ASNode node : ast.copyVertexSet()) {
//                    String name = "v" + nodeCount++;
//                    asNodeStringMap.put(node, name);
//                    StringBuilder label = new StringBuilder("  [label=\"");
//                    String coloredDotStr = DotPalette.getColoredNodeStr(node);
//                    label.append(StringUtils.escape(node.getCode())).append("\"").append(coloredDotStr).append("];");
//                    dot.println("  " + name + label.toString());
//                }
//                // sub ast edges
//                for (Edge<ASNode, ASEdge> astEdge : ast.copyEdgeSet()) {
//                    String src = asNodeStringMap.get(astEdge.source);
//                    String tgt = asNodeStringMap.get(astEdge.target);
//                    String edgeDotStr = DotPalette.getEdgeDotStr(astEdge);
//                    dot.println("  " + src + " -> " + tgt + edgeDotStr + "];");
//                }
//                // root to par cf node
//                ASNode root = ast.root;
//                if (root.getProperty("par_line") == null) continue;
//                int start = (int) root.getProperty("par_line");
//                CFNode startNode = slice.copyVertexSet().stream()
//                        .filter(node -> node.getLineOfCode() == start)
//                        .findFirst().orElse(null);
//                if (startNode != null) {
//                    String src = controlFlowNodeIdMap.get(startNode);
//                    String tgt = asNodeStringMap.get(root);
//                    Edge<ASNode, ASEdge> dummy = new Edge<>(null, new ASEdge(), null);
//                    String edgeDotStr = DotPalette.getEdgeDotStr(dummy);
//                    dot.println("  " + src + " -> " + tgt + edgeDotStr + "];");
//                }
//            }
            dot.println("  // end-of-graph\n}");
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            Logger.error(ex);
        }
    }

    public static void exportDot2(Slice slice, String dotFileName) {
        System.out.println("exporting to : " + dotFileName);
        try (PrintWriter dot = new PrintWriter(dotFileName, "UTF-8")) {
            dot.println("digraph " + "SLICE {");
            Map<CFNode, String> controlFlowNodeIdMap = new LinkedHashMap<>();
            Map<PDNode, String> dataFowNodeIdMap = new LinkedHashMap<>();
            Map<ASNode, String> asNodeStringMap = new LinkedHashMap<>();
            dot.println("  // graph-vertices");
            int nodeCount = 1;
            for (CFNode cfNode : slice.copyVertexSet()) {
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
            for (PDNode pdNode : slice.getDataNodes()) {
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
            for (Edge<CFNode, CFEdge> controlFlowEdge : slice.copyEdgeSet()) {
                String src = controlFlowNodeIdMap.get(controlFlowEdge.source);
                String trg = controlFlowNodeIdMap.get(controlFlowEdge.target);
                String edgeDotStr = DotPalette.getEdgeDotStr(controlFlowEdge);
                dot.println("  " + src + " -> " + trg + edgeDotStr +
                        "label=\"" + controlFlowEdge.label.type + "\"];");
            }
            for (Edge<PDNode, DDEdge> dataEdge : slice.getDataFlowEdges()) {
                String src = dataFowNodeIdMap.get(dataEdge.source);
                String trg = dataFowNodeIdMap.get(dataEdge.target);
                String edgeDotStr = DotPalette.getEdgeDotStr(dataEdge);
                dot.println("  " + src + " -> " + trg + edgeDotStr + "label=\" (" + dataEdge.label.var + ")\"];");
            }
            // sub asts
//            MyJPDotPrinter printer = new MyJPDotPrinter(nodeCount, true);
//            Node node = slice.subASTRootNodes.get(3);
//            if (node != null)
//                dot.print(printer.output(node));

            dot.println("  // end-of-graph\n}");
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            Logger.error(ex);
        }
    }

    public static void exportDot(AbstractSyntaxTree ast, String outDir) {
        if (!outDir.endsWith(File.separator))
            outDir += File.separator;
        File outDirFile = new File(outDir);
        outDirFile.mkdirs();
        String filename = ast.fileName.substring(0, ast.fileName.lastIndexOf('.'));
        String filepath = outDir + filename + "-AST.dot";
        try (PrintWriter dot = new PrintWriter(filepath, "UTF-8")) {
            dot.println("digraph " + filename + "_AST {");
            dot.println("  // graph-vertices");
            Map<ASNode, String> nodeNames = new LinkedHashMap<>();
            int nodeCounter = 1;
            for (ASNode node : ast.copyVertexSet()) {
                String name = "n" + nodeCounter++;
                nodeNames.put(node, name);
                StringBuilder label = new StringBuilder("  [label=\"");
                label.append(StringUtils.escape(node.getCode())).append("\"];");
                dot.println("  " + name + label.toString());
            }
            dot.println("  // graph-edges");
            for (Edge<ASNode, ASEdge> edge : ast.copyEdgeSet()) {
                String src = nodeNames.get(edge.source);
                String trg = nodeNames.get(edge.target);
                dot.println("  " + src + " -> " + trg + ";");
            }
            dot.println("  // end-of-graph\n}");
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            Logger.error(ex);
        }
        Logger.info("AST exported to: " + filepath);
    }

    public String getOutputDir() {
        return outputDir;
    }

    public CommitDiff getCommitDiff() {
        return commitDiff;
    }

    public String getCommit_base() {
        return commit_base;
    }

    public String getSlices_base() {
        return slices_base;
    }

    public String getSubgraph_base() {
        return subgraph_base;
    }

    public String getIpdg_base() {
        return ipdg_base;
    }

    public String getOriginal_graph_base() {
        return original_graph_base;
    }
}
