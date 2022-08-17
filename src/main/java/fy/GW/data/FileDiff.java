package fy.GW.data;

import com.github.javaparser.ast.CompilationUnit;
import fy.PROGEX.graphs.IPDG;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileDiff {

    DiffEntry diffEntry;
    List<Edit> edits;
    String v1;
    ProgramDependeceGraph graph1;
    AbstractSyntaxTree ast1;
    String fileName1;
    CompilationUnit cu1;
    IPDG ipdg1;
    String v2;
    ProgramDependeceGraph graph2;
    AbstractSyntaxTree ast2;
    String fileName2;
    CompilationUnit cu2;
    IPDG ipdg2;

    public FileDiff(DiffEntry diffEntry) {
        this.diffEntry = diffEntry;
    }

    public boolean isValid() {
        return graph1 != null && graph2 != null
                && v1 != null && v2 != null
                && edits != null && !edits.isEmpty();
    }

    public DiffEntry getDiffEntry() {
        return diffEntry;
    }

    public void setDiffEntry(DiffEntry diffEntry) {
        this.diffEntry = diffEntry;
    }

    public List<Edit> getEdits() {
        return edits;
    }

    public void setEdits(List<Edit> edits) {
        this.edits = edits;
    }

    public String getV1() {
        return v1;
    }

    public void setV1(String v1) {
        this.v1 = v1;
    }

    public ProgramDependeceGraph getGraph1() {
        return graph1;
    }

    public void setGraph1(ProgramDependeceGraph graph1) {
        this.graph1 = graph1;
    }

    public AbstractSyntaxTree getAst1() {
        return ast1;
    }

    public void setAst1(AbstractSyntaxTree ast1) {
        this.ast1 = ast1;
    }

    public String getFileName1() {
        return fileName1;
    }

    public void setFileName1(String fileName1) {
        this.fileName1 = fileName1;
    }

    public IPDG getIpdg1() {
        return ipdg1;
    }

    public void setIpdg1(IPDG ipdg1) {
        this.ipdg1 = ipdg1;
    }

    public String getV2() {
        return v2;
    }

    public void setV2(String v2) {
        this.v2 = v2;
    }

    public ProgramDependeceGraph getGraph2() {
        return graph2;
    }

    public void setGraph2(ProgramDependeceGraph graph2) {
        this.graph2 = graph2;
    }

    public AbstractSyntaxTree getAst2() {
        return ast2;
    }

    public void setAst2(AbstractSyntaxTree ast2) {
        this.ast2 = ast2;
    }

    public String getFileName2() {
        return fileName2;
    }

    public void setFileName2(String fileName2) {
        this.fileName2 = fileName2;
    }

    public IPDG getIpdg2() {
        return ipdg2;
    }

    public void setIpdg2(IPDG ipdg2) {
        this.ipdg2 = ipdg2;
    }

    public CompilationUnit getCu1() {
        return cu1;
    }

    public void setCu1(CompilationUnit cu1) {
        this.cu1 = cu1;
    }

    @Override
    public String toString() {
        return "FileDiff{" +
                "diffEntry=" + diffEntry +
                ", edits=" + edits +
                ", v1='" + v1 + '\'' +
                ", graph1=" + graph1 +
                ", ast1=" + ast1 +
                ", fileName1='" + fileName1 + '\'' +
                ", cu1=" + cu1 +
                ", ipdg1=" + ipdg1 +
                ", v2='" + v2 + '\'' +
                ", graph2=" + graph2 +
                ", ast2=" + ast2 +
                ", fileName2='" + fileName2 + '\'' +
                ", cu2=" + cu2 +
                ", ipdg2=" + ipdg2 +
                '}';
    }

    public CompilationUnit getCu2() {
        return cu2;
    }

    public void setCu2(CompilationUnit cu2) {
        this.cu2 = cu2;
    }
}
