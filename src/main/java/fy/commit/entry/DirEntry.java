package fy.commit.entry;

import fy.commit.DirParser;
import fy.progex.graphs.IPDG;

import java.io.IOException;
import java.util.List;

public class DirEntry {

    public static IPDG process(List<String> javaFiles, String dir) throws IOException {
        IPDG graph = DirParser.parse(javaFiles, dir);
        return graph;
    }
}
