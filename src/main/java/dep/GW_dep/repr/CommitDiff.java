package dep.GW_dep.repr;

import org.eclipse.jgit.lib.Repository;

import java.util.ArrayList;
import java.util.List;

public class CommitDiff {
    Repository repository;
    String projectPath;
    String v1;
    String v2;
    List<FileDiff> fileDiffs = new ArrayList<>();
}
