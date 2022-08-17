package fy.PROGEX.parse;

import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;

public class PDGInfoParser {

    public static void parse(PDGInfo pdgInfo) {
        CDGParser cdgParser = new CDGParser(pdgInfo);
        cdgParser.parse();
        CFGParser cfgParser = new CFGParser(pdgInfo);
        cfgParser.parse();
        DDGParser ddgParser = new DDGParser(pdgInfo);
        ddgParser.parse();
    }
}
