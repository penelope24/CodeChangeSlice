package dep.GW_dep.repr;

import com.github.javaparser.ast.CompilationUnit;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.EditList;

import java.io.File;
import java.util.Objects;


public class FileSnapShot {
    public DiffEntry diffEntry;
    public File javaFile;
    public CompilationUnit cu;
    public EditList edits;
    public String version;
    public ProgramDependeceGraph graph;
    public PDGInfo pdgInfo;

    public FileSnapShot(DiffEntry diffEntry, File javaFile, CompilationUnit cu, EditList edits, String v) {
        this.diffEntry = diffEntry;
        this.javaFile = javaFile;
        this.cu = cu;
        this.edits = edits;
        this.version = v;
    }

    public FileSnapShot(DiffEntry diffEntry, File javaFile, EditList edits, String v) {
        this.diffEntry = diffEntry;
        this.javaFile = javaFile;
        this.edits = edits;
        this.version = v;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.diffEntry);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        FileSnapShot other = (FileSnapShot) obj;
        return Objects.equals(this.diffEntry, other.diffEntry);
    }

    public void setCu(CompilationUnit cu) {
        this.cu = cu;
    }
}
