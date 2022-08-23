package fy.GW;

import com.github.javaparser.ast.CompilationUnit;
import fy.ACE.MethodCall;

import java.util.ArrayList;
import java.util.List;

public class VersionedInterProcessInfoManager {
    public String v;
    public List<CompilationUnit> parseTrees;
    public List<MethodCall> methodCalls = new ArrayList<>();

    public VersionedInterProcessInfoManager(String v, List<CompilationUnit> parseTrees) {
        this.v = v;
        this.parseTrees = parseTrees;
    }

    private void solveMethodCalls() {

    }
}
