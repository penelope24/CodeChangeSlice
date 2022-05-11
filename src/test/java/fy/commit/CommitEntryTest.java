package fy.commit;

import com.github.javaparser.ast.body.MethodDeclaration;
import fy.commit.entry.CommitEntry;
import fy.commit.repr.CommitDiff;
import fy.progex.parse.type.collect.TypeCollector;
import fy.progex.parse.type.solver.MySimpleTypeSolver;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class CommitEntryTest {

}