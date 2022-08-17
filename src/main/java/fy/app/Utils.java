package fy.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static List<String> getAllProjects(String base) {
        File f = new File(base);
        File[] projectsArray = f.listFiles(Utils::is_project_path);
        if (projectsArray != null) {
            return Arrays.stream(projectsArray).map(File::getAbsolutePath)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private static boolean is_project_path(File file) {
        return file.isDirectory() && !file.isHidden();
    }
}
