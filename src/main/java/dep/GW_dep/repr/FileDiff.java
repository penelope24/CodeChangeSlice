package dep.GW_dep.repr;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;

import java.util.List;

public class FileDiff {

    DiffEntry diffEntry;
    FileSnapShot file1;
    FileSnapShot file2;
    List<Edit> edits;
}
