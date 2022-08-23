package fy.CDS.solver.cfg.edit;

import fy.PROGEX.build.MyPDGBuilder;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FlowEditorTest {
    String testJavaFileSrc = "/Users/fy/Documents/slicing_cases/custom/control_flow_test/SwitchNodeTestCase.java";
    String[] ss = testJavaFileSrc.split("/");
    String name = ss[ss.length-1].replaceAll(".java", "");
    List<Integer> chLines = new ArrayList<>();
    String outputPath = "/Users/fy/Documents/slicing_cases/custom/control_flow_test";

    @Test
    void test_edit() throws IOException {

    }

    @Test
    void exportDot() throws IOException {
        ProgramDependeceGraph graph = MyPDGBuilder.build(new File(testJavaFileSrc));
        graph.DDS.exportDOT(outputPath);
        graph.CDS.exportDOT(outputPath);
    }


}