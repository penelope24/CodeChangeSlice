package fy.CDS;

import com.github.javaparser.ast.CompilationUnit;
import fy.CDS.data.Slice;
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
    public FileDiff fileDiff;
    public List<Slice> slices1 = new ArrayList<>();
    public List<Slice> slices2 = new ArrayList<>();

    public FileDiffProcessor(FileDiff fileDiff) {
        this.fileDiff = fileDiff;
    }

    public void process () {
        List<Edit> edits = fileDiff.edits;
        if (edits == null) return;
        for (Edit edit : edits) {
            Edit.Type editType = edit.getType();
            // v1
            List<Integer> chLines1 = IntStream.range(edit.getBeginA() + 1, edit.getEndA() + 1)
                    .boxed()
                    .collect(Collectors.toList());
            ProgramDependeceGraph graph1 = fileDiff.graph1;
            AbstractSyntaxTree ast1 = null;
            CompilationUnit cu1 = fileDiff.cu1;
            if (graph1 != null && !chLines1.isEmpty()) {
                PDGInfo pdgInfo = new PDGInfo(graph1);
                PDGInfoParser.parse(pdgInfo);
                List<Slice> slices = CodeDiffSlicer.slice(pdgInfo, chLines1, editType);
                slices1.addAll(slices);
            }
            // v2
            List<Integer> chLines2 = IntStream.range(edit.getBeginB() + 1, edit.getEndB() + 1)
                    .boxed()
                    .collect(Collectors.toList());
            ProgramDependeceGraph graph2 = fileDiff.graph2;
            AbstractSyntaxTree ast2 = null;
            CompilationUnit cu2 = fileDiff.cu2;
            if (graph2 != null && !chLines2.isEmpty()) {
                PDGInfo pdgInfo = new PDGInfo(graph2);
                PDGInfoParser.parse(pdgInfo);
                List<Slice> slices = CodeDiffSlicer.slice(pdgInfo, chLines2, editType);
                slices2.addAll(slices);
            }
        }
        slices1.removeIf(Objects::isNull);
        slices1.removeIf(slice -> slice.vertexCount() == 0);
        slices1.removeIf(slice -> slice.paletteResult == null);
        slices2.removeIf(Objects::isNull);
        slices2.removeIf(slice -> slice.vertexCount() == 0);
        slices2.removeIf(slice -> slice.paletteResult == null);
    }
}
