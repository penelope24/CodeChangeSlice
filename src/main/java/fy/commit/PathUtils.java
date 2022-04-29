package fy.commit;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class PathUtils {
    static String splitter = File.separator;

    public static String getSplitter() {
        return splitter;
    }

    /**
     * 给定绝对路径，返回简名
     * */
    public static String getProjectName(String projectPath) {
        return projectPath.substring(projectPath.lastIndexOf(splitter) + 1);
    }

    /**
     * 给定一个Git Repository对象，返回其根目录地址
     * */
    public static String getRootPathFromRepository(Repository repository) {
        String gitRoot = repository.getDirectory().getAbsolutePath();
//        System.out.println(gitRoot);
        return gitRoot.substring(0, gitRoot.lastIndexOf(splitter));
    }


    /**
     * get absolute path of A (null if entry type is ADD)
     * @return
     */
    public static String getOldPath(DiffEntry entry, Repository repository) {
        String relA = entry.getOldPath();
        if (relA.equals("/dev/null")) {
            return null;
        }
        else {
            String root = getRootPathFromRepository(repository);
            return root + File.separator + relA;
        }
    }

    /**
     * get absolute path of B (null if entry type is DELETE)
     * @return
     */
    public static String getNewPath(DiffEntry entry, Repository repository) {
        String relB = entry.getNewPath();
        if (relB.equals("/dev/null")) {
            return null;
        }
        else {
            String root = getRootPathFromRepository(repository);
            return root + File.separator + relB;
        }
    }

    public static Stream<Path> getAllDirPaths(String basePath) throws IOException {
        Stream<Path> paths = Files.list(new File(basePath).toPath()).filter(Files::isDirectory).filter(PathUtils::isNotHiddenDirectory);
        return paths;
    }

    public static boolean isNotHiddenDirectory(Path path){
        boolean flag = false;
        try {
            if (Files.isHidden(path)) {
                flag = false;
            }
            else {
                flag = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
