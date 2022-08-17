package fy.ACE;

public class MethodCall {
    public MethodKey caller;
    public MethodKey callee;

    public MethodCall(MethodKey caller, MethodKey callee) {
        this.caller = caller;
        this.callee = callee;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(caller.javaFile + "  " + caller.line);
        sb.append("    ---->    ");
        sb.append(callee.javaFile + "  " + callee.line);
        return sb.toString();
    }

    public MethodKey getCaller() {
        return caller;
    }

    public MethodKey getCallee() {
        return callee;
    }
}
