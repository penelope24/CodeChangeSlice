package fy.GW.data;

import com.github.javaparser.ast.CompilationUnit;
import ghaffarian.progex.graphs.ast.AbstractSyntaxTree;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;

import java.util.LinkedList;
import java.util.List;

public class FileDiffNew {

    // change
    public DiffEntry diffEntry;
    public List<Hunk> hunks = new LinkedList<>();
    // v1
    public String v1;
    public String path1;
    public ProgramDependeceGraph graph1;
    public AbstractSyntaxTree ast1;
    public CompilationUnit cu1;
    // v2
    public String v2;
    public String path2;
    public ProgramDependeceGraph graph2;
    public AbstractSyntaxTree ast2;
    public CompilationUnit cu2;

    public FileDiffNew(DiffEntry diffEntry) {
        this.diffEntry = diffEntry;
    }

    public boolean is_v1_valid() {
        if (v1 == null || path1 == null || graph1 == null) {
            return false;
        }
        if (graph1.DDS.vertexCount() < 1) {
            return false;
        }
        return true;
    }

    public boolean is_v2_valid() {
        if (v2 == null || path2 == null || graph2 == null) {
            return false;
        }
        if (graph2.DDS.vertexCount() < 1) {
            return false;
        }
        return true;
    }

    public boolean is_valid() {
        return is_v1_valid() || is_v2_valid();
    }
}
