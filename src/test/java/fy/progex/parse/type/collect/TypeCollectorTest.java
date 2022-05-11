package fy.progex.parse.type.collect;

import fy.utils.file.DirTraveler;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

class TypeCollectorTest {

    String project = "/Users/fy/Documents/fyCodeGraphs";
    String case_base = "/Users/fy/Documents/fyJavaProjects/MyCases";

    @Test
    void test() throws FileNotFoundException {
        List<String> javaFiles = DirTraveler.findAllJavaFiles(project);
        TypeCollector typeCollector = new TypeCollector(javaFiles.toArray(new String[0]));
        typeCollector.collect();
        System.out.println(typeCollector.package2types.size());
        System.out.println(typeCollector.typeName2Methods.size());
    }

    @Test
    void test2() throws FileNotFoundException {
        List<String> javaFiles = DirTraveler.findAllJavaFiles(case_base);
        TypeCollector typeCollector = new TypeCollector(javaFiles.toArray(new String[0]));
        typeCollector.collect();
        typeCollector.package2types.keySet().forEach(pkg -> {
            typeCollector.package2types.get(pkg).forEach(type -> {
                System.out.println(type.getFullyQualifiedName());
            });
        });
    }
}