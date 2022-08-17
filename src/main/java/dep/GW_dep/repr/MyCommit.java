//package fy.commit.repr;
//
//import fy.javaparser.methodcall.MyTypeSolver;
//import org.eclipse.jgit.lib.Repository;
//import org.eclipse.jgit.revwalk.RevCommit;
//
//import java.io.IOException;
//import java.util.Set;
//
///**
// * wrapper class of RevCommit
// * avoid unnecessary git checkouts
// */
//public class MyCommit {
//    public Repository repository;
//    public RevCommit commit;
//    public Set<FileSnapShot> snapShots;
//    public MyTypeSolver myTypeSolver;
//
//    public MyCommit(Repository repository, RevCommit commit, Set<FileSnapShot> snapShots) throws IOException {
//        this.repository = repository;
//        this.commit = commit;
//        this.snapShots = snapShots;
//    }
//
//    public void setRepository(Repository repository) {
//        this.repository = repository;
//    }
//
//    public void setCommit(RevCommit commit) {
//        this.commit = commit;
//    }
//
//    public void setSnapShots(Set<FileSnapShot> snapShots) {
//        this.snapShots = snapShots;
//    }
//
//    public void setSolver(MyTypeSolver myTypeSolver) {
//        this.myTypeSolver = myTypeSolver;
//    }
//}
