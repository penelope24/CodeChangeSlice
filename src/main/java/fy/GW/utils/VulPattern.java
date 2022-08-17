package fy.GW.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * 用正则表达式定义常见vulnerable commit的模式
 */
public class VulPattern {
    private String pattern_path = "/Users/fy/Documents/MyProjects/java/ProgramGraphs/src/main/resources/vul_patterns";

    public String buildPattern(String patternFile) throws IOException {
        String path = pattern_path + File.separator + patternFile;
        StringBuilder sb = new StringBuilder();
        BufferedReader br1 = new BufferedReader(new FileReader(path + ".prefix"));
        sb.append(br1.readLine());
        br1.close();
        BufferedReader br2 = new BufferedReader(new FileReader(path));
        String str;
        while ((str = br2.readLine()) != null) {
            sb.append(str.stripTrailing()).append("|");
        }
        sb.deleteCharAt(sb.length()-1);
        br2.close();
        BufferedReader br3 = new BufferedReader(new FileReader(path + ".suffix"));
        sb.append(br3.readLine());
        br3.close();
        return sb.toString();
    }



    public static void main(String[] args) throws IOException {
        VulPattern vulPattern = new VulPattern();
        System.out.println(vulPattern.buildPattern("vuln"));
    }
}
