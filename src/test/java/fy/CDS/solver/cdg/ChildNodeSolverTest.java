package fy.CDS.solver.cdg;

import fy.PROGEX.build.MyPDGBuilder;
import fy.PROGEX.parse.PDGInfo;
import fy.PROGEX.parse.PDGInfoParser;
import ghaffarian.progex.graphs.pdg.PDNode;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChildNodeSolverTest {


    @Test
    void test1 () {
        String javaFile = "/Users/fy/Documents/MyProjects/CodeChangeCases/slicing_cases/main/java/casecade_cases/CascadeCase1.java";
        ProgramDependeceGraph pdg = MyPDGBuilder.build(new File(javaFile));
        PDGInfo pdgInfo = new PDGInfo(pdg);
        PDGInfoParser.parse(pdgInfo);
        ChildNodeSolver solver = new ChildNodeSolver(pdgInfo.cdg);
        int line = 14;
        PDNode startNode = pdgInfo.cdg.copyVertexSet().stream()
                .filter(node -> node.getLineOfCode() == line)
                .findFirst().get();
        List<PDNode> res = solver.find_first_level_children(startNode);
        System.out.println(res);
    }

    @Test
    void test2 () {
        String javaFile = "/Users/fy/Documents/MyProjects/CodeChangeCases/bug/program_graphs/bug1/buggy.java";
        ProgramDependeceGraph pdg = MyPDGBuilder.build(new File(javaFile));
        PDGInfo pdgInfo = new PDGInfo(pdg);
        PDGInfoParser.parse(pdgInfo);
        ChildNodeSolver solver = new ChildNodeSolver(pdgInfo.cdg);
        int line = 43;
        PDNode startNode = pdgInfo.cdg.copyVertexSet().stream()
                .filter(node -> node.getLineOfCode() == line)
                .findFirst().get();
        List<List<PDNode>> res = solver.find_all_children_level_order(startNode);
        res.forEach(System.out::println);
    }
}