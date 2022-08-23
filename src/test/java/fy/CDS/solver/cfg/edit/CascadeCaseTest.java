package fy.CDS.solver.cfg.edit;

import fy.PROGEX.build.MyPDGBuilder;
import fy.PROGEX.parse.CDGParser;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class CascadeCaseTest {

    String javaFile = "/Users/fy/Documents/MyProjects/CodeChangeCases/slicing_cases/main/java/casecade_cases/CascadeCase1.java";
    String output = "/Users/fy/Documents/MyProjects/CodeChangeCases/slicing_cases/main/java/casecade_cases";

    @Test
    void rep_cdg_parse() {
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(javaFile));
        PDGInfo pdgInfo = new PDGInfo(graph);
        CDGParser cdgParser = new CDGParser(pdgInfo);
        cdgParser.parse();
    }

    @Test
    void generateDOT() throws IOException {
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(javaFile));
        graph.DDS.exportDOT(output);
        graph.DDS.getCFG().exportDOT(output);
        graph.CDS.exportDOT(output);
    }
}
