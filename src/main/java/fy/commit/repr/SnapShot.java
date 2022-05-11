package fy.commit.repr;

import com.github.javaparser.ast.CompilationUnit;
import org.eclipse.jgit.diff.DiffEntry;


public class SnapShot {
    public String version;
    public String javaFile;
    public CompilationUnit cu;

    public SnapShot(String version, String javaFile, CompilationUnit cu) {
        this.version = version;
        this.javaFile = javaFile;
        this.cu = cu;
    }
}
