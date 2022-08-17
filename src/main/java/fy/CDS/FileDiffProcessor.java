package fy.CDS;

import com.github.javaparser.ast.CompilationUnit;
import fy.CDS.data.Slice;
import fy.CDS.data.SliceManager;
import fy.GW.data.FileDiff;
import fy.PROGEX.parse.PDGInfo;
import fy.PROGEX.parse.PDGInfoParser;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.diff.Edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FileDiffProcessor {
    FileDiff fileDiff;
    List<List<Integer>> chLinesList1 = new ArrayList<>();
    List<List<Integer>> chLinesList2 = new ArrayList<>();
    List<Slice> slices1 = new ArrayList<>();
    List<Slice> slices2 = new ArrayList<>();

    public FileDiffProcessor(FileDiff fileDiff) {
        this.fileDiff = fileDiff;
    }

    public void process () {
        List<Edit> edits = fileDiff.getEdits();
        if (edits == null) return;
        for (Edit edit : edits) {
            Edit.Type editType = edit.getType();
            // v1
            List<Integer> chLines1 = IntStream.range(edit.getBeginA() + 1, edit.getEndA() + 1)
                    .boxed()
                    .collect(Collectors.toList());
            ProgramDependeceGraph graph1 = fileDiff.getGraph1();
            AbstractSyntaxTree ast1 = null;
            CompilationUnit cu1 = fileDiff.getCu1();
            if (graph1 != null && !chLines1.isEmpty()) {
                PDGInfo pdgInfo = new PDGInfo(graph1);
                PDGInfoParser.parse(pdgInfo);
                Slice slice = CodeDiffSlicer.slice(pdgInfo, chLines1, ast1, cu1);
                if (slice != null) {
                    slice.setEditType(editType);
                    slices1.add(slice);
                }
                String name1 = graph1.FILE_NAME.getName().replaceAll(".java", "");
                fileDiff.setFileName1(name1);
            }
            // v2
            List<Integer> chLines2 = IntStream.range(edit.getBeginB() + 1, edit.getEndB() + 1)
                    .boxed()
                    .collect(Collectors.toList());
            ProgramDependeceGraph graph2 = fileDiff.getGraph2();
            AbstractSyntaxTree ast2 = null;
            CompilationUnit cu2 = fileDiff.getCu2();
            if (graph2 != null && !chLines2.isEmpty()) {
                PDGInfo pdgInfo = new PDGInfo(graph2);
                PDGInfoParser.parse(pdgInfo);
                Slice slice = CodeDiffSlicer.slice(pdgInfo, chLines2, ast2, cu2);
                if (slice != null) {
                    slice.setEditType(editType);
                    slices2.add(slice);
                }
                String name2 = fileDiff.getGraph2().FILE_NAME.getName().replaceAll(".java", "");
                fileDiff.setFileName2(name2);
            }
        }
        slices1.removeIf(Objects::isNull);
        slices1.removeIf(slice -> slice.vertexCount() == 0 && slice.subAstTrees.size() == 0);
        slices2.removeIf(Objects::isNull);
        slices2.removeIf(slice -> slice.vertexCount() == 0 && slice.subAstTrees.size() == 0);

    }

    public FileDiff getFileDiff() {
        return fileDiff;
    }

    public List<Slice> getSlices1() {
        return slices1;
    }

    public List<Slice> getSlices2() {
        return slices2;
    }
}
