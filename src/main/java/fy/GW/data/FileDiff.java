package fy.GW.data;

import com.github.javaparser.ast.CompilationUnit;
import fy.PROGEX.graphs.IPDG;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;

import java.util.List;

public class FileDiff {
    // change
    public DiffEntry diffEntry;
    public List<Edit> edits;
    // v1
    public String v1;
    public ProgramDependeceGraph graph1;
    public AbstractSyntaxTree ast1;
    public CompilationUnit cu1;
    // v2
    public String v2;
    public ProgramDependeceGraph graph2;
    public AbstractSyntaxTree ast2;
    public CompilationUnit cu2;

    public FileDiff(DiffEntry diffEntry) {
        this.diffEntry = diffEntry;
    }

    public boolean isValid() {
        return graph1 != null && graph2 != null
                && v1 != null && v2 != null
                && edits != null && !edits.isEmpty();
    }

    @Override
    public String toString() {
        return "FileDiff{" +
                "diffEntry=" + diffEntry +
                ", edits=" + edits +
                ", v1='" + v1 + '\'' +
                ", graph1=" + graph1 +
                ", ast1=" + ast1 +
                ", cu1=" + cu1 +
                ", v2='" + v2 + '\'' +
                ", graph2=" + graph2 +
                ", ast2=" + ast2 +
                ", cu2=" + cu2 +
                '}';
    }

    public void setDiffEntry(DiffEntry diffEntry) {
        this.diffEntry = diffEntry;
    }

    public void setEdits(List<Edit> edits) {
        this.edits = edits;
    }

    public void setV1(String v1) {
        this.v1 = v1;
    }

    public void setGraph1(ProgramDependeceGraph graph1) {
        this.graph1 = graph1;
    }

    public void setAst1(AbstractSyntaxTree ast1) {
        this.ast1 = ast1;
    }

    public void setCu1(CompilationUnit cu1) {
        this.cu1 = cu1;
    }

    public void setV2(String v2) {
        this.v2 = v2;
    }

    public void setGraph2(ProgramDependeceGraph graph2) {
        this.graph2 = graph2;
    }

    public void setAst2(AbstractSyntaxTree ast2) {
        this.ast2 = ast2;
    }

    public void setCu2(CompilationUnit cu2) {
        this.cu2 = cu2;
    }
}
