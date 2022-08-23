package fy.ACE;

import org.junit.jupiter.api.Test;

import java.util.List;

class UtilsTest {

    @Test
    void getAllJarFiles() {
        String inputPath = "/Users/fy/Downloads/LibDetect/output/data/extractLib/jars";
        List<String> jarFiles = Utils.getAllJarFiles(inputPath);
        jarFiles.forEach(System.out::println);
        System.out.println(jarFiles.size());
    }

}