package fy.commit;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import fy.commit.repr.SnapShot;
import fy.progex.build.IPDGBuilder;
import fy.progex.graphs.IPDG;
import fy.progex.parse.PDGInfo;
import fy.utils.file.DirTraveler;
import ghaffarian.progex.graphs.pdg.PDGBuilder;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DirParser {


    public static IPDG parse(List<String> javaFiles, String symbolSolverBase) throws IOException {
        ProgramDependeceGraph[] pdgArray;
        List<PDGInfo> worklist = new LinkedList<>();
        try {
            pdgArray = PDGBuilder.buildForAll("Java", javaFiles.toArray(new String[0]));
        } catch (Exception e) {
            pdgArray = new ProgramDependeceGraph[0];
        }
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(symbolSolverBase));
        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);
        for (int i=0; i< pdgArray.length; i++) {
            PDGInfo pdgInfo = new PDGInfo(pdgArray[i]);
            pdgInfo.analyzePDGMaps();
            StaticJavaParser.getConfiguration().setSymbolResolver(javaSymbolSolver);
            String path = pdgInfo.abs_path;
            CompilationUnit cu = StaticJavaParser.parse(new File(path));
            SnapShot snapShot = new SnapShot(null, path, cu);
            pdgInfo.setFileSnapShot(snapShot);
            worklist.add(pdgInfo);
        }
        return IPDGBuilder.build(worklist);
    }
}
