package fy.ACE;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class MethodKey {
    // location
    File javaFile;
    int line;
    // identity
    String pkgName;
    String clsName;
    String methodName;
    int paramNum;
    List<String> simpleParamTypes;

    public MethodKey(File javaFile, int line) {
        this.javaFile = javaFile;
        this.line = line;
    }

    public MethodKey(String pkgName, String clsName, String methodName, int paramNum, List<String> simpleParamTypes) {
        this.pkgName = pkgName;
        this.clsName = clsName;
        this.methodName = methodName;
        this.paramNum = paramNum;
        this.simpleParamTypes = simpleParamTypes;
    }

    public void setJavaFile(File javaFile) {
        this.javaFile = javaFile;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public void setClsName(String clsName) {
        this.clsName = clsName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParamNum(int paramNum) {
        this.paramNum = paramNum;
    }

    public void setSimpleParamTypes(List<String> simpleParamTypes) {
        this.simpleParamTypes = simpleParamTypes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgName, clsName, methodName, paramNum, simpleParamTypes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodKey)) {
            return false;
        }
        MethodKey other = (MethodKey) obj;
        return Objects.equals(this.pkgName, other.pkgName) &&
                Objects.equals(this.clsName, other.clsName) &&
                Objects.equals(this.methodName, other.methodName) &&
                Objects.equals(this.paramNum, other.paramNum) &&
                Objects.equals(this.simpleParamTypes, other.simpleParamTypes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(pkgName).append(".");
        sb.append(clsName).append(".");
        sb.append(methodName).append("_");
        sb.append(simpleParamTypes);
        return sb.toString();
    }

    public File getJavaFile() {
        return javaFile;
    }

    public int getLine() {
        return line;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getClsName() {
        return clsName;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getParamNum() {
        return paramNum;
    }

    public List<String> getSimpleParamTypes() {
        return simpleParamTypes;
    }
}
