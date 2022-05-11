package fy;

import fy.commit.GitHistoryWalker;
import fy.commit.repr.CommitDiff;
import fy.slicing.entry.CommitEntry;
import fy.utils.jgit.JGitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Execution {
    public enum RunType {
        run_all_projects,
        run_single_project,
        run_continue,
        run_reproduce
    }
    RunType runType = RunType.run_single_project;
    String all_projects_base = null;
    String curr_project_path;
    String curr_version = null;
    String output_path;

    public Execution() {
    }

    public void execute() throws GitAPIException, IOException {
        System.out.println(runType);
        switch (runType) {
            case run_all_projects:
                run_all();
                break;
            case run_single_project:
                run_single();
                break;
            case run_continue:
                run_continue();
                break;
            case run_reproduce:
                run_reproduce();
            default:
        }
    }

    public void run_all() throws GitAPIException, IOException {
        List<String> projects = Utils.getAllProjects(all_projects_base);
        for (String project : projects) {
            run_single(project);
        }
    }

    public void run_single () throws GitAPIException, IOException {
        run_single(curr_project_path);
    }

    public void run_continue() throws IOException, GitAPIException {
        run_continue(curr_project_path, curr_version);
    }

    public void run_reproduce() throws IOException, GitAPIException {
        run_reproduce(curr_project_path, curr_version);
    }

    private void run_single(String curr_project_path) throws GitAPIException, IOException {

    }

    private void run_continue(String project, String version) throws IOException, GitAPIException {

    }

    private void run_reproduce(String project, String version) throws IOException, GitAPIException {

    }

    public void setRunType(RunType runType) {
        this.runType = runType;
    }

    public void setAll_projects_base(String all_projects_base) {
        this.all_projects_base = all_projects_base;
    }

    public void setCurr_project_path(String curr_project_path) {
        this.curr_project_path = curr_project_path;
    }

    public void setCurr_version(String curr_version) {
        this.curr_version = curr_version;
    }

    public void setOutput_path(String output_path) {
        this.output_path = output_path;
    }
}
