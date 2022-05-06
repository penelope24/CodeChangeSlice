package fy.commit.repr;

import fy.progex.parse.PDGInfo;
import org.eclipse.jgit.diff.Edit;

public class AtomEdit {

    public Edit edit;
    public PDGInfo pdgInfo1;
    public PDGInfo pdgInfo2;

    public AtomEdit(Edit edit) {
        this.edit = edit;
    }

    public AtomEdit(Edit edit, PDGInfo pdgInfo1, PDGInfo pdgInfo2) {
        this.edit = edit;
        this.pdgInfo1 = pdgInfo1;
        this.pdgInfo2 = pdgInfo2;
    }

    public void setEdit(Edit edit) {
        this.edit = edit;
    }

    public void setPdgInfo1(PDGInfo pdgInfo1) {
        this.pdgInfo1 = pdgInfo1;
    }

    public void setPdgInfo2(PDGInfo pdgInfo2) {
        this.pdgInfo2 = pdgInfo2;
    }
}
