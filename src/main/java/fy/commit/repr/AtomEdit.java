package fy.commit.repr;

import fy.annotation.KeyMethod;
import fy.progex.parse.PDGInfo;
import org.eclipse.jgit.diff.Edit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AtomEdit {

    public PDGInfo pdgInfo;
    public List<Integer> editLines;

    public AtomEdit(PDGInfo pdgInfo, List<Integer> editLines) {
        this.pdgInfo = pdgInfo;
        this.editLines = editLines;
    }
}
