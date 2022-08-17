package fy.app;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class Config {
    public static void setAllProjectPaths(List<String> projects) {
        try (OutputStream os = new FileOutputStream("src/main/resources/all_projects.properties")) {
            Properties prop = new Properties();
            // set the properties
            StringBuilder sb = new StringBuilder();
            projects.forEach(s -> {
                sb.append(s).append(";\n");
            });
            prop.setProperty("projects", sb.toString());
            // save to disk
            prop.store(os, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties loadProjects() {
        try (InputStream is = new FileInputStream("src/main/resources/all_projects.properties")) {
            Properties prop = new Properties();
            prop.load(is);
            return prop;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
