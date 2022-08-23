package fy;

import fy.utils.file.SubFileFinder;
import ghaffarian.progex.graphs.cfg.CFGBuilder;
import ghaffarian.progex.graphs.cfg.ControlFlowGraph;
import ghaffarian.progex.graphs.cfg.ICFGBuilder;
import ghaffarian.progex.graphs.pdg.PDGBuilder;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;

import java.io.IOException;
import java.util.List;

public class TestUtils {

    public static List<String> getPaths(String base) {
        return SubFileFinder.findAllJavaFiles(base);
    }

    public static ProgramDependeceGraph generatePDG (String path) throws IOException {
        String[] paths = new String[1];
        paths[0] = path;
        return PDGBuilder.buildForAll("Java", paths)[0];
    }

    public static ControlFlowGraph generateICFG(String base) throws IOException {
        String[] paths = getPaths(base).toArray(new String[0]);
        return ICFGBuilder.buildForAll("Java", paths);
    }

    public static ControlFlowGraph generateCFG(String path) throws IOException {
        String[] paths = new String[1];
        paths[0] = path;
        return CFGBuilder.build("Java", path);
    }


}
