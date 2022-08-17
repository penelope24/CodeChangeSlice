package fy.GW;

import fy.GW.utils.VulPattern;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析commit message，判断一个commit是否是具有bug的正例
 */
public class CommitParse {

    public static boolean is_positive(RevCommit commit) {
        if (is_bug_fix(commit) || is_vul(commit)) {
            return true;
        }
        return false;
    }

    public static boolean is_bug_fix(RevCommit patch) {
        String msg = patch.getShortMessage();
        if (rule3(msg)) {
            return true;
        }
        return false;
    }

    public static boolean is_vul(RevCommit patch) {
        String msg = patch.getFullMessage();
        if (rule4(msg)) {
            return true;
        }
        return false;
    }


    /**
     * 包含bug fix关键字的
     * */
    public static boolean rule1(String msg) {
        if (msg.toLowerCase().contains("bug fix")) {
            return true;
        }
        return false;
    }

    /**
     * 只要包含bug这个词的
     * */
    public static boolean rule2(String msg) {
        if (msg.toLowerCase().contains("bug")) {
            return true;
        }
        return false;
    }

    /**
     * 同时出现bug和fix两个关键词，不关注顺序
     * */
    public static boolean rule3(String msg) {
        String[] tokens = msg.toLowerCase().split(" ");
        List<String> list = Arrays.asList(tokens);
        if (list.contains("fix") || list.contains("fixed")) {
            if (list.contains("bug") || list.contains("bugs")) {
                return true;
            }
        }
        return false;
    }

    public static boolean rule4(String msg) {
        VulPattern vul = new VulPattern();
        try {
            String regex1 = vul.buildPattern("c");
//            String regex2 = vul.buildPattern("crypto");
            String regex3 = vul.buildPattern("vuln");
            Pattern p1 = Pattern.compile(regex1);
//            Pattern p2 = Pattern.compile(regex2);
            Pattern p3 = Pattern.compile(regex3);
            Matcher m1 = p1.matcher(msg);
//            Matcher m2 = p2.matcher(msg);
            Matcher m3 = p3.matcher(msg);
            if (m1.find() || m3.find()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
