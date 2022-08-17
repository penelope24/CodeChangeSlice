package dep;

import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.diff.Edit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GraphSnapshot {
    ProgramDependeceGraph graph;
    List<List<Integer>> chLines;

    public GraphSnapshot(ProgramDependeceGraph graph, List<List<Integer>> chLines) {
        this.graph = graph;
        this.chLines = chLines;
    }
}
