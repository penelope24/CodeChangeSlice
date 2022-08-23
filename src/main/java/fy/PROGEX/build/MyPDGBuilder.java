package fy.PROGEX.build;

import ghaffarian.progex.graphs.pdg.PDGBuilder;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import ghaffarian.progex.java.JavaPDGBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyPDGBuilder {

    public static List<ProgramDependeceGraph> buildForAll (List<File> javaFiles) throws IOException {
        ProgramDependeceGraph[] graphs;
        List<String> srcJavaFiles = javaFiles.stream()
                .map(file -> file.getAbsolutePath())
                .collect(Collectors.toList());
        graphs = PDGBuilder.buildForAll("Java", srcJavaFiles.toArray(new String[0]));
        return Arrays.asList(graphs);
    }

    public static ProgramDependeceGraph build(File javaFile) {
        ProgramDependeceGraph graph;
        graph = JavaPDGBuilder.build(javaFile);
        return graph;
    }
}
