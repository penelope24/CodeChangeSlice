//package fy.commit.repr;
//
//import fy.progex.parse.PDGInfo;
//import org.eclipse.jgit.diff.DiffEntry;
//import org.eclipse.jgit.diff.DiffFormatter;
//import org.eclipse.jgit.diff.Edit;
//import org.eclipse.jgit.diff.EditList;
//import org.eclipse.jgit.lib.Repository;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Objects;
//
///**
// *  将PDG与对应的edits联系起来
// */
//public class FileDiff {
//    public Repository repository;
//    public DiffEntry diffEntry;
//    public PDGInfo pdgInfo;
//    public List<AtomEdit> atomEdits;
//
//    public FileDiff() {
//    }
//
//    public FileDiff(Repository repository, DiffEntry diffEntry) throws IOException {
//        this.repository = repository;
//        this.diffEntry = diffEntry;
//        EditList edits = getEditList(diffEntry);
//        for (Edit edit : edits) {
//            AtomEdit atomEdit = new AtomEdit(edit);
//        }
//    }
//
//    private EditList getEditList(DiffEntry diffEntry) throws IOException {
//        DiffFormatter diffFormatter = new DiffFormatter(null);
//        diffFormatter.setContext(0);
//        diffFormatter.setRepository(repository);
//        return diffFormatter.toFileHeader(diffEntry).toEditList();
//    }
//
//    public void setDiffEntry(DiffEntry diffEntry) {
//        this.diffEntry = diffEntry;
//    }
//
//    public void setPdgInfo(PDGInfo pdgInfo) {
//        this.pdgInfo = pdgInfo;
//    }
//
//    public void setAtomEdits(List<AtomEdit> atomEdits) {
//        this.atomEdits = atomEdits;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        final FileDiff other = (FileDiff) obj;
//        return  Objects.equals(this.diffEntry, other.diffEntry);
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 3;
//        hash = 53 * hash + Objects.hashCode(this.diffEntry);
//        return hash;
//    }
//}
