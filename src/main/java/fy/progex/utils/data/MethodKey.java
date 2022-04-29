package fy.progex.utils.data;

public class MethodKey {
    String qualifiedName;
    int line;

    public MethodKey(String qualifiedName, int line) {
        this.qualifiedName = qualifiedName;
        this.line = line;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public int getLine() {
        return line;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodKey)) {
            return false;
        }
        MethodKey key = (MethodKey) obj;
        return this.qualifiedName.equals(key.qualifiedName)
                && this.line == key.line;
    }
}
