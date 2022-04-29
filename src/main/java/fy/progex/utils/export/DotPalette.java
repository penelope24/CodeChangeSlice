package fy.progex.utils.export;

import ghaffarian.graphs.Edge;
import ghaffarian.progex.graphs.cfg.CFEdge;
import ghaffarian.progex.graphs.cfg.CFNode;
import ghaffarian.progex.graphs.pdg.DDEdge;

public class DotPalette {
    public enum NodeColorType {

        Entry           ("darkseagreen1"),
        CallSite        ("crimson"),
        Exit            ("gray"),
        SlicingStart    ("darkseagreen"),
        DataBind        ("deepskyblue3"),
        ControlBind     ("cornsilk1"),
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
        Default         ("default");
        public String label;
        EdgeArrow(String lbl) {
            this.label = lbl;
        }
    }

    public static NodeColorType getNodeColorType(CFNode node) {
        if ((boolean) node.getPropertyWithDefault("start")) {
            return NodeColorType.SlicingStart;
        }
        if ((boolean) node.getPropertyWithDefault("entry")) {
            return NodeColorType.Entry;
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

    public static EdgeColor getEdgeColorType(Edge edge) {
        if (edge.label instanceof CFEdge) {
            if (((CFEdge) edge.label).type == CFEdge.Type.CALLS
                    || ((CFEdge) edge.label).type == CFEdge.Type.RETURN) {
                return EdgeColor.Call;
            }
            else {
                return EdgeColor.ControlFLow;
            }
        }
        if (edge.label instanceof DDEdge) {
            return EdgeColor.DataFlow;
        }
        return EdgeColor.Default;
    }

    public static EdgeStyle getEdgeStyle(Edge edge) {
        if (edge.label instanceof CFEdge) {
            if (((CFEdge) edge.label).type == CFEdge.Type.CALLS
                    || ((CFEdge) edge.label).type == CFEdge.Type.RETURN) {
                return EdgeStyle.Call;
            }
            else {
                return EdgeStyle.ControlFLow;
            }
        }
        if (edge.label instanceof DDEdge) {
            return EdgeStyle.DataFlow;
        }
        return EdgeStyle.Default;
    }

    public static EdgeArrow getEdgeArrow(Edge edge) {
        if (edge.label instanceof CFEdge) {
            if (((CFEdge) edge.label).type == CFEdge.Type.CALLS
                    || ((CFEdge) edge.label).type == CFEdge.Type.RETURN) {
                return EdgeArrow.Call;
            }
            else {
                return EdgeArrow.ControlFLow;
            }
        }
        if (edge.label instanceof DDEdge) {
            return EdgeArrow.DataFlow;
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
