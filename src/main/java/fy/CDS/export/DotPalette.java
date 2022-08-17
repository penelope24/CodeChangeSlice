package fy.CDS.export;

import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.ast.ASEdge;
import ghaffarian.progex.graphs.ast.ASNode;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;

public class DotPalette {
    public enum NodeColorType {
        Entry           ("gray"),
        CallSite        ("crimson"),
        Exit            ("gray"),
        SlicingStart    ("darkseagreen"),
        DataBind        ("deepskyblue3"),
        ControlBind     ("cornsilk1"),
        AST             ("mistyrose3"),
        CLASS           ("bisque"),
        Default         ("default");
        public final String label;
        NodeColorType(String lbl) {
            this.label = lbl;
        }
    }

    public enum EdgeColor {
        DataFlow        ("black"),
        ControlFLow     ("grey"),
        Call            ("lightcoral"),
        AST             ("firebrick"),
        CONTAIN         ("default"),
        Default         ("default");
        public String label;
        EdgeColor(String lbl) {
            this.label = lbl;
        }
    }

    public enum EdgeStyle {
        DataFlow        ("solid"),
        ControlFLow     ("bold"),
        Call            ("bashed"),
        AST             ("default"),
        CONTAIN         ("default"),
        Default         ("default");
        public String label;
        EdgeStyle(String lbl) {
            this.label = lbl;
        }
    }

    public enum EdgeArrow {
        DataFlow        ("normal"),
        ControlFLow     ("empty"),
        Call            ("open"),
        AST             ("default"),
        CONTAIN         ("normal"),
        Default         ("default");
        public String label;
        EdgeArrow(String lbl) {
            this.label = lbl;
        }
    }

    public static NodeColorType getNodeColorType(CFNode node) {
        if ((boolean) node.getPropertyWithDefault("entry")) {
            return NodeColorType.Entry;
        }
        if ((boolean) node.getPropertyWithDefault("start")) {
            return NodeColorType.SlicingStart;
        }
        if ((boolean) node.getPropertyWithDefault("exit")) {
            return NodeColorType.Exit;
        }
        if ((boolean) node.getPropertyWithDefault("data_bind")) {
            return NodeColorType.DataBind;
        }
        if ((boolean) node.getPropertyWithDefault("control_bind")) {
            return NodeColorType.ControlBind;
        }
        if ((boolean) node.getPropertyWithDefault("class")) {
            return NodeColorType.CLASS;
        }
        if ((boolean) node.getPropertyWithDefault("callsite")) {
            return NodeColorType.Default;
        }
        return NodeColorType.Default;
    }

    public static String getColoredNodeStr(CFNode node) {
        NodeColorType type = getNodeColorType(node);
        if (type == NodeColorType.Default) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(",");
        sb.append("color=").append(type.label).append(", ");
        sb.append("style=").append("filled");
        return sb.toString();
    }

    public static String getColoredNodeStr(ASNode node) {
        NodeColorType type = NodeColorType.AST;
        StringBuilder sb = new StringBuilder();
        sb.append(",");
        sb.append("color=").append(type.label).append(", ");
        sb.append("style=").append("filled");
        return sb.toString();
    }

    public static EdgeColor getEdgeColorType(Edge edge) {
        if (edge.label instanceof CFEdge) {
            CFEdge.Type type = ((CFEdge) edge.label).type;
            switch (type) {
                case CALLS:
                case RETURN:
                    return EdgeColor.Call;
                case CONTAIN:
                    return EdgeColor.CONTAIN;
                default:
                    return EdgeColor.Default;
            }
        }
        if (edge.label instanceof DDEdge) {
            return EdgeColor.DataFlow;
        }
        if (edge.label instanceof ASEdge) {
            return EdgeColor.AST;
        }
        return EdgeColor.Default;
    }

    public static EdgeStyle getEdgeStyle(Edge edge) {
        if (edge.label instanceof CFEdge) {
            CFEdge.Type type = ((CFEdge) edge.label).type;
            switch (type) {
                case CALLS:
                case RETURN:
                    return EdgeStyle.Call;
                case CONTAIN:
                    return EdgeStyle.CONTAIN;
                default:
                    return EdgeStyle.Default;
            }
        }
        if (edge.label instanceof DDEdge) {
            return EdgeStyle.DataFlow;
        }
        if (edge.label instanceof ASEdge) {
            return EdgeStyle.AST;
        }
        return EdgeStyle.Default;
    }

    public static EdgeArrow getEdgeArrow(Edge edge) {
        if (edge.label instanceof CFEdge) {
            CFEdge.Type type = ((CFEdge) edge.label).type;
            switch (type) {
                case CALLS:
                case RETURN:
                    return EdgeArrow.Call;
                case CONTAIN:
                    return EdgeArrow.CONTAIN;
                default:
                    return EdgeArrow.Default;
            }
        }
        if (edge.label instanceof DDEdge) {
            return EdgeArrow.DataFlow;
        }
        if (edge.label instanceof ASEdge) {
            return EdgeArrow.AST;
        }
        return EdgeArrow.Default;
    }

    public static String getEdgeDotStr(Edge edge) {
        EdgeColor color = getEdgeColorType(edge);
        EdgeStyle style = getEdgeStyle(edge);
        EdgeArrow arrow = getEdgeArrow(edge);
        StringBuilder sb = new StringBuilder();
        sb.append("  [");
        sb.append("arrowhead=").append(arrow.label).append(", ");
        sb.append("color=").append(color.label).append(", ");
        sb.append("style=").append(style.label).append(", ");
        return sb.toString();
    }
}
