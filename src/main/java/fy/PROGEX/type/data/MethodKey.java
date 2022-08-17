package fy.PROGEX.type.data;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithType;

import java.util.List;
import java.util.stream.Collectors;

public class MethodKey {
    String name;
    List<String> paramTypeList;
    int paramNum;

    public MethodKey(String name, List<String> paramTypeList, int paramNum) {
        this.name = name;
        this.paramTypeList = paramTypeList;
        this.paramNum = paramNum;
    }

    public MethodKey(MethodDeclaration md) {
        this.name = md.getNameAsString();
        this.paramNum = md.getParameters().size();
        this.paramTypeList = md.getParameters().stream()
                .map(NodeWithType::getTypeAsString)
                .collect(Collectors.toList());
    }

    public MethodKey(String name, int paramNum) {
        this.name = name;
        this.paramNum = paramNum;
    }


}
