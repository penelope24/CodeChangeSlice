package dep;

import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;

import java.util.List;

public class GraphSnapshot {
    ProgramDependeceGraph graph;
    List<List<Integer>> chLines;

    public GraphSnapshot(ProgramDependeceGraph graph, List<List<Integer>> chLines) {
        this.graph = graph;
        this.chLines = chLines;
    }
}
