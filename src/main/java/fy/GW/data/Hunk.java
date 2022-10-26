package fy.GW.data;

import com.github.javaparser.ast.CompilationUnit;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.diff.Edit;

import java.util.LinkedList;
import java.util.List;

public class Hunk {
    public Edit edit;
    public List<Integer> linesADD = new LinkedList<>();
    public List<Integer> linesREM = new LinkedList<>();
    public List<String> stmtsADD = new LinkedList<>();
    public List<String> stmtsREM = new LinkedList<>();

    public Hunk(Edit edit) {
        this.edit = edit;
    }

    public boolean is_v1_valid () {
        return !linesREM.isEmpty() && ! stmtsREM.isEmpty();
    }

    public boolean is_v2_valid() {
        return !linesADD.isEmpty() && !stmtsADD.isEmpty();
    }

    public boolean is_valid() {
        return is_v1_valid() || is_v2_valid();
    }
}
