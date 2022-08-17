package fy.CDS;

import com.github.javaparser.ast.CompilationUnit;
import fy.CDS.data.Slice;
import fy.CDS.export.DotExporter;
import fy.GW.data.CommitDiff;
import fy.GW.data.FileDiff;
import fy.PROGEX.graphs.IPDG;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;

import java.io.*;
import java.util.List;

public class CommitDiffProcessor {
    CommitDiff commitDiff;
    String outputDir;
    DotExporter dotExporter;
    IPDG ipdg1;
    IPDG ipdg2;

    public CommitDiffProcessor(CommitDiff commitDiff, String outputDir) {
        this.commitDiff = commitDiff;
        this.outputDir = outputDir;
        this.dotExporter = new DotExporter(outputDir, commitDiff);
    }

    public void process () throws Exception {
        System.out.println("file diffs size : " + commitDiff.getFileDiffs().size());
        for (FileDiff fileDiff : commitDiff.getFileDiffs()) {
            FileDiffProcessor processor = new FileDiffProcessor(fileDiff);
            processor.process();
            export_original_text(processor);
            export_original_graph(processor);
            export_slices(processor);
//            export_entry_sub_graphs(processor);
        }
    }

    public void custom_check() {
        for (FileDiff fileDiff : commitDiff.getFileDiffs()) {
            File file1 = fileDiff.getGraph1().FILE_NAME;
            File file2 = fileDiff.getGraph2().FILE_NAME;
            assert file1 != file2;
        }
    }

    private void export_original_text(FileDiffProcessor processor) throws Exception {
        String base = this.dotExporter.getOriginal_graph_base();
        FileDiff fileDiff = processor.fileDiff;
        // v1
        File file1 = fileDiff.getGraph1().FILE_NAME;
        File outputFile1 = new File(base + "/" + fileDiff.getFileName1() + "_v1.java");
        CompilationUnit cu1 = fileDiff.getCu1();
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(outputFile1));
        bw1.write(cu1.toString());
        bw1.newLine();
        fileDiff.getEdits().forEach(edit -> {
            try {
                bw1.write(edit.toString());
                bw1.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bw1.close();
        // v2
        File file2 = processor.fileDiff.getGraph2().FILE_NAME;
        File outputFile2 = new File(base + "/" + fileDiff.getFileName2() + "_v2.java");
        CompilationUnit cu2 = fileDiff.getCu2();
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(outputFile2));
        bw2.write(cu2.toString());
        bw2.newLine();
        fileDiff.getEdits().forEach(edit -> {
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
        ProgramDependeceGraph graph1 = processor.fileDiff.getGraph1();
        graph1.DDS.exportDOT(base);
        graph1.CDS.exportDOT(base);
        // v2
        ProgramDependeceGraph graph2 = processor.fileDiff.getGraph2();
        graph2.DDS.exportDOT(base);
        graph2.CDS.exportDOT(base);
    }

    private void export_slices(FileDiffProcessor processor) {
        String base = this.dotExporter.getSlices_base();
        List<Slice> slices1 = processor.getSlices1();
        slices1.forEach(slice -> {
            int index = slices1.indexOf(slice);
            String fileName = slice.sliceManager.fileName;
            String name = base + "/" + fileName + "_slice_v1" + "@edit_" + index + ".dot";
            DotExporter.exportDot(slice, name);
        });
        List<Slice> slices2 = processor.getSlices2();
        slices2.forEach(slice -> {
            int index = slices2.indexOf(slice);
            String fileName = slice.sliceManager.fileName;
            String name = base + "/" + fileName + "_slice_v2" + "@edit_" + index + ".dot";
            DotExporter.exportDot2(slice, name);
        });
    }



    private void export_ipdg() {
        // todo
    }

    public CommitDiff getCommitDiff() {
        return commitDiff;
    }

    public IPDG getIpdg1() {
        return ipdg1;
    }

    public IPDG getIpdg2() {
        return ipdg2;
    }
}
