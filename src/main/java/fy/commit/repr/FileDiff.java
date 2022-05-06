package fy.commit.repr;

import com.github.javaparser.ast.CompilationUnit;
import org.eclipse.jgit.diff.DiffEntry;


public class FileDiff {
    public String version;
    public DiffEntry diffEntry;
    public String javaFile;
    public CompilationUnit cu;

    public FileDiff(String version, DiffEntry diffEntry, String javaFile, CompilationUnit cu) {
        this.version = version;
        this.diffEntry = diffEntry;
        this.javaFile = javaFile;
        this.cu = cu;
    }
}
