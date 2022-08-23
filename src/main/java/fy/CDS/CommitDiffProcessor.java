package fy.CDS;

import com.github.javaparser.ast.CompilationUnit;
import fy.ACE.MethodCall;
import fy.ACE.MethodKey;
import fy.ACE.MyPatchSolver;
import fy.CDS.data.InterProcedureSlice;
import fy.CDS.data.Slice;
import fy.CDS.export.DotExporter;
import fy.GW.data.CommitDiff;
import fy.GW.data.FileDiff;
import fy.PROGEX.graphs.IPDG;
import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class CommitDiffProcessor {
    CommitDiff commitDiff;
    String outputDir;
    DotExporter dotExporter;
    InterProcedureSlice IPS1;
    InterProcedureSlice IPS2;

    public CommitDiffProcessor(CommitDiff commitDiff, String outputDir) {
        this.commitDiff = commitDiff;
        this.outputDir = outputDir;
        this.dotExporter = new DotExporter(outputDir, commitDiff);
    }

    public void process () throws IOException {
        System.out.println("file diffs size : " + commitDiff.fileDiffs.size());
        for (FileDiff fileDiff : commitDiff.fileDiffs) {
            FileDiffProcessor processor = new FileDiffProcessor(fileDiff);
            processor.process();
//            export_original_text(processor);
//            export_original_graph(processor);
            export_slices(processor);
            export_slice_cfg_paths(processor);
//            export_inter_procedure_slice(processor);
        }
    }

    public void custom_check() {
        for (FileDiff fileDiff : commitDiff.fileDiffs) {
            File file1 = fileDiff.graph1.FILE_NAME;
            File file2 = fileDiff.graph2.FILE_NAME;
            assert file1 != file2;
        }
    }

    private void export_inter_procedure_slice(FileDiffProcessor processor) {
        String base = this.dotExporter.getIps_base();
        // v1
        {
            MyPatchSolver solver = commitDiff.solver1;
            List<Slice> slices = processor.slices1;
            IPS1 = new InterProcedureSlice(slices);
            // method call
            List<MethodCall> methodCalls = solver.solveMethodCalls();
            methodCalls.forEach(methodCall -> {
                CFNode src = findCFNodeByMethodKey(slices, methodCall.caller);
                CFNode tgt = findCFNodeByMethodKey(slices, methodCall.callee);
                if (src != null && tgt != null) {
                    IPS1.addEdge(new Edge<>(src, new CFEdge(CFEdge.Type.CALLS), tgt));
                }
            });
            // class nodes
            Set<CFNode> classNodes = solver.solveClassNodes();
            classNodes.forEach(node -> {
                IPS1.addVertex(node);
                List<MethodKey> targets = (List<MethodKey>) node.getProperty("targets");
                if (targets != null) {
                    targets.forEach(key -> {
                        CFNode tgt = findCFNodeByMethodKey(slices, key);
                        if (tgt != null) {
                            IPS1.addEdge(new Edge<>(node, new CFEdge(CFEdge.Type.CALLS), tgt));
                        }
                    });
                }
            });
            DotExporter.exportDot(IPS1, base + "/IPS1.dot");
        }
        // v2
        {
            MyPatchSolver solver = commitDiff.solver2;
            List<Slice> slices = processor.slices2;
            IPS2 = new InterProcedureSlice(slices);
            // method call
            List<MethodCall> methodCalls = solver.solveMethodCalls();
            methodCalls.forEach(methodCall -> {
                CFNode src = findCFNodeByMethodKey(slices, methodCall.caller);
                CFNode tgt = findCFNodeByMethodKey(slices, methodCall.callee);
                if (src != null && tgt != null) {
                    IPS2.addEdge(new Edge<>(src, new CFEdge(CFEdge.Type.CALLS), tgt));
                }
            });
            // class nodes
            Set<CFNode> classNodes = solver.solveClassNodes();
            classNodes.forEach(node -> {
                IPS2.addVertex(node);
                List<MethodKey> targets = (List<MethodKey>) node.getProperty("targets");
                if (targets != null) {
                    targets.forEach(key -> {
                        CFNode tgt = findCFNodeByMethodKey(slices, key);
                        if (tgt != null) {
                            IPS2.addEdge(new Edge<>(node, new CFEdge(CFEdge.Type.CALLS), tgt));
                        }
                    });
                }
            });
            DotExporter.exportDot(IPS2, base + "/IPS2.dot");
        }
    }

    private CFNode findCFNodeByMethodKey(List<Slice> slices, MethodKey key) {
        return slices.stream()
                .filter(slice -> slice.sliceManager.pdgInfo.pdg.FILE_NAME.equals(key.getJavaFile())
                        && slice.sliceManager.entryNode.getLineOfCode() == key.getLine())
                .map(slice -> slice.sliceManager.entryNode)
                .findFirst().orElse(null);
    }

    private void export_original_text(FileDiffProcessor processor) throws IOException {
        String base = this.dotExporter.getOriginal_graph_base();
        FileDiff fileDiff = processor.fileDiff;
        // v1
        File file1 = fileDiff.graph1.FILE_NAME;
        String name1 = file1.getName().replaceAll(".java", "");
        File outputFile1 = new File(base + "/" + name1 + "_v1.java");
        CompilationUnit cu1 = fileDiff.cu1;
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(outputFile1));
        bw1.write(cu1.toString());
        bw1.newLine();
        fileDiff.edits.forEach(edit -> {
            try {
                bw1.write(edit.toString());
                bw1.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bw1.close();
        // v2
        File file2 = processor.fileDiff.graph2.FILE_NAME;
        String name2 = file2.getName().replaceAll(".java", "");
        File outputFile2 = new File(base + "/" + name2 + "_v2.java");
        CompilationUnit cu2 = fileDiff.cu2;
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(outputFile2));
        bw2.write(cu2.toString());
        bw2.newLine();
        fileDiff.edits.forEach(edit -> {
            try {
                bw2.write(edit.toString());
                bw2.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bw2.close();
    }

    private void export_original_graph(FileDiffProcessor processor) throws IOException {
        String base = this.dotExporter.getOriginal_graph_base();
        // v1
        ProgramDependeceGraph graph1 = processor.fileDiff.graph1;
        graph1.DDS.exportDOT(base);
        graph1.CDS.exportDOT(base);
        // v2
        ProgramDependeceGraph graph2 = processor.fileDiff.graph2;
        graph2.DDS.exportDOT(base);
        graph2.CDS.exportDOT(base);
    }

    private void export_slices(FileDiffProcessor processor) {
        String base = this.dotExporter.getSlices_base();
        List<Slice> slices1 = processor.slices1;
        slices1.forEach(slice -> {
            int index = slices1.indexOf(slice);
            String fileName = slice.sliceManager.fileName;
            String name = base + "/" + fileName + "_slice_v1" + "@edit_" + index + ".dot";
            DotExporter.exportDot(slice, name);
        });
        List<Slice> slices2 = processor.slices2;
        slices2.forEach(slice -> {
            int index = slices2.indexOf(slice);
            String fileName = slice.sliceManager.fileName;
            String name = base + "/" + fileName + "_slice_v2" + "@edit_" + index + ".dot";
            DotExporter.exportDot(slice, name);
        });
    }

    public void export_slice_cfg_paths(FileDiffProcessor processor) {
        String base= this.dotExporter.getCfg_path_base();
        // v1
        {
            List<Slice> slices = processor.slices1;
            slices.forEach(slice -> {
                int index1 = slices.indexOf(slice);
                String sliceName = base + "/" + slice.sliceManager.fileName + "_slice_v1" + "@edit_" + index1;
                CFGPathSlicer pathSlicer = new CFGPathSlicer(slice);
                pathSlicer.slice();
                Set<Slice> cfgPaths = pathSlicer.cfgPaths;
                int index2 = 0;
                for (Slice path : cfgPaths) {
                    path.setPaletteResult(slice.paletteResult);
                    String pathName = sliceName + "_path" + index2++ + ".dot";
                    DotExporter.exportDot(path, pathName);
                }
            });
        }
        // v2
        {
            List<Slice> slices = processor.slices2;
            slices.forEach(slice -> {
                int index1 = slices.indexOf(slice);
                System.out.println(index1);
                String sliceName = base + "/" + slice.sliceManager.fileName + "_slice_v2" + "@edit_" + index1;
                CFGPathSlicer pathSlicer = new CFGPathSlicer(slice);
                pathSlicer.slice();
                Set<Slice> cfgPaths = pathSlicer.cfgPaths;
                System.out.println("paths num: " + cfgPaths.size());
                int index2 = 0;
                for (Slice path : cfgPaths) {
                    path.setPaletteResult(slice.paletteResult);
                    String pathName = sliceName + "_path" + index2++ + ".dot";
                    DotExporter.exportDot(path, pathName);
                }
            });
        }
    }

}
