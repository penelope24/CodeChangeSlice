package fy.commit.repr;

import org.eclipse.jgit.diff.Edit;

public class EditMap {
    public Edit edit;
    public FileDiff fileDiff1;
    public FileDiff fileDiff2;

    public EditMap(Edit edit, FileDiff fileDiff1, FileDiff fileDiff2) {
        this.edit = edit;
        this.fileDiff1 = fileDiff1;
        this.fileDiff2 = fileDiff2;
    }

    public EditMap(Edit edit) {
        this.edit = edit;
    }

    public void setEdit(Edit edit) {
        this.edit = edit;
    }

    public void setFileDiff1(FileDiff fileDiff1) {
        this.fileDiff1 = fileDiff1;
    }

    public void setFileDiff2(FileDiff fileDiff2) {
        this.fileDiff2 = fileDiff2;
    }
}
