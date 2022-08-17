package fy.ACE;

import fy.utils.file.SubFileFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static List<String> getAllJarFiles(String inputPath) {
        List<String> res = new ArrayList<>();
        new SubFileFinder( (level, path, file) -> path.endsWith(".jar"),
                (level, path, file) -> res.add(file.getAbsolutePath()))
                .explore(new File(inputPath));
        return res;
    }

    public static List<String> getAllJavaFiles(String inputPath) {
        List<String> res = new ArrayList<>();
        new SubFileFinder( (level, path, file) -> path.endsWith(".java"),
                (level, path, file) -> res.add(file.getAbsolutePath()))
                .explore(new File(inputPath));
        return res;
    }

    public static MethodKey parseQualifiedSignature(String signature) {
        String methodMainBody = signature.replaceAll("\\((.*)\\)", "");
        List<String> ss = Arrays.asList(methodMainBody.split("\\."));
        String methodName = ss.get(ss.size() - 1);
        String clsName = ss.get(ss.size() - 2);
        List<String> sss = ss.subList(0, ss.size()-2);
        String pkgName = String.join(".", sss);

        String paramString = null;
        int paramNum = 0;
        List<String> simpleParamTypes = new ArrayList<>();
        Pattern p = Pattern.compile("\\((.*)\\)");
        Matcher m = p.matcher(signature);
        while (m.find()){
            paramString = m.group(1);
        }
        if (paramString != null) {
            List<String> params = Arrays.asList(paramString.split(","));
            paramNum = params.size();
            for (String param : params) {
                String[] sp = param.split("\\.");
                String paramTypeName = sp[sp.length-1];
                simpleParamTypes.add(paramTypeName);
            }

        }
        MethodKey methodKey = new MethodKey(pkgName, clsName, methodName, paramNum, simpleParamTypes);
        return methodKey;
    }
}
