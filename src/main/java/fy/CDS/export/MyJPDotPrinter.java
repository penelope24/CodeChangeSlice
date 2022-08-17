package fy.CDS.export;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;

import java.util.List;

import static com.github.javaparser.utils.Utils.SYSTEM_EOL;
import static java.util.stream.Collectors.toList;

public class MyJPDotPrinter  {
    public int nodeCount;
    private final boolean outputNodeType;

    public MyJPDotPrinter(int nodeCount, boolean outputNodeType) {
        this.nodeCount = nodeCount;
        this.outputNodeType = outputNodeType;
    }

    public String output(Node node) {
        StringBuilder output = new StringBuilder();
        output(node, null, "root", output);
        return output.toString();
    }

    public void output(Node node, String parentNodeName, String name, StringBuilder builder) {
        assert node != null;
        NodeMetaModel metaModel = node.getMetaModel();
        List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        List<PropertyMetaModel> attributes = allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute)
                .filter(PropertyMetaModel::isSingular).collect(toList());
        List<PropertyMetaModel> subNodes = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode)
                .filter(PropertyMetaModel::isSingular).collect(toList());
        List<PropertyMetaModel> subLists = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList)
                .collect(toList());

        String ndName = nextNodeName();
        if (outputNodeType)
            builder.append(SYSTEM_EOL + ndName + " [label=\"" + escape(name) + " (" + metaModel.getTypeName()
                    + ")\"];");
        else
            builder.append(SYSTEM_EOL + ndName + " [label=\"" + escape(name) + "\"];");

        if (parentNodeName != null)
            builder.append(SYSTEM_EOL + parentNodeName + " -> " + ndName + ";");

        for (PropertyMetaModel a : attributes) {
            String attrName = nextNodeName();
            builder.append(SYSTEM_EOL + attrName + " [label=\"" + escape(a.getName()) + "='"
                    + escape(a.getValue(node).toString()) + "'\"];");
            builder.append(SYSTEM_EOL + ndName + " -> " + attrName + ";");

        }

        for (PropertyMetaModel sn : subNodes) {
            Node nd = (Node) sn.getValue(node);
            if (nd != null)
                output(nd, ndName, sn.getName(), builder);
        }

        for (PropertyMetaModel sl : subLists) {
            NodeList<? extends Node> nl = (NodeList<? extends Node>) sl.getValue(node);
            if (nl != null && nl.isNonEmpty()) {
                String ndLstName = nextNodeName();
                builder.append(SYSTEM_EOL + ndLstName + " [label=\"" + escape(sl.getName()) + "\"];");
                builder.append(SYSTEM_EOL + ndName + " -> " + ndLstName + ";");
                String slName = sl.getName().substring(0, sl.getName().length() - 1);
                for (Node nd : nl)
                    output(nd, ndLstName, slName, builder);
            }
        }
    }

    private String nextNodeName() {
        return "n" + (nodeCount++);
    }

    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }
}