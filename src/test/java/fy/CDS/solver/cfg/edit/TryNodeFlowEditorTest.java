package fy.CDS.solver.cfg.edit;

import fy.PROGEX.build.MyPDGBuilder;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TryNodeFlowEditorTest {

    String javaFile = "/Users/fy/Documents/MyProjects/CCS_cases/slicing_cases/basic_structure/try_catch/TryCatchCase.java";
    String output = "/Users/fy/Documents/MyProjects/CCS_cases/slicing_cases/basic_structure/try_catch";

    @Test
    void generateDot () throws IOException {
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(javaFile));
        graph.CDS.exportDOT(output);
        graph.DDS.exportDOT(output);
    }
}
