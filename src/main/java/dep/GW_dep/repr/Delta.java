//package dep.GW_dep.repr;
//
//import fy.PROGEX.build.MyPDGBuilder;
//import fy.PROGEX.parse.PDGInfo;
//import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
//import org.eclipse.jgit.diff.DiffEntry;
//import org.eclipse.jgit.lib.Repository;
//
//import java.io.IOException;
//import java.util.*;
//
///**
// * 先空转跑delta，然后git checkout 每一个commit，对照到delta
// */
//public class Delta {
//    public Repository repository;
//    public String project;
//    public String v1;
//    public String v2;
//    public List<DiffEntry> validDiffEntries;
//    public Set<FileSnapShot> snapShots1 = new HashSet<>();
//    public Set<FileSnapShot> snapShots2 = new HashSet<>();
//
//
//    public Delta(String project, Repository repository, String v1, String v2, List<DiffEntry> validDiffEntries) {
//        this.repository = repository;
//        this.project = project;
//        this.v1 = v1;
//        this.v2 = v2;
//        this.validDiffEntries = validDiffEntries;
//    }
//
//    public void setSnapshotV1(Set<FileSnapShot> snapShots) throws IOException {
//        this.snapShots1 = snapShots;
//    }
//
//    public void setSnapShotV2(Set<FileSnapShot> snapShots) throws IOException {
//        this.snapShots2 = snapShots;
//    }
//
//    public void buildGraphs() throws IOException {
//        // v1
//        snapShots1.forEach(fileSnapShot -> {
//            ProgramDependeceGraph pdg = MyPDGBuilder.build(fileSnapShot);
//            if (pdg == null || pdg.DDS.vertexCount() == 0) {
//                return;
//            }
//            fileSnapShot.graph = pdg;
//            fileSnapShot.pdgInfo = new PDGInfo(fileSnapShot);
//        });
//        // v2
//        snapShots2.forEach(fileSnapShot -> {
//            ProgramDependeceGraph pdg = MyPDGBuilder.build(fileSnapShot);
//            if (pdg == null || pdg.DDS.vertexCount() == 0) {
//                return;
//            }
//            fileSnapShot.graph = pdg;
//            fileSnapShot.pdgInfo = new PDGInfo(fileSnapShot);
//        });
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 3;
//        hash = 53 * hash + Objects.hash(this.v1, this.v2);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        Delta other = (Delta) obj;
//        return Objects.equals(this.v1, other.v1) && Objects.equals(this.v2, other.v2);
//    }
//}
