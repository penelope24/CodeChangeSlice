package fy.CDS.track;

import fy.CDS.data.SliceManager;
import fy.CDS.solver.cfg.edit.*;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.progex.graphs.cfg.CFNode;

import java.util.LinkedHashMap;
import java.util.Map;

public class CFGTracker {
    SliceManager sliceManager;
    PDGInfo pdgInfo;

    public CFGTracker(SliceManager sliceManager) {
        this.pdgInfo = sliceManager.pdgInfo;
        this.sliceManager = sliceManager;
    }

    public void track() {
        // parse
        Map<CFNode, FlowEditor> editorMap = new LinkedHashMap<>();
        for (CFNode cfNode : sliceManager.worklist) {
            switch (cfNode.getType()) {
                case SWITCH: {
                    FlowEditor editor = new SwitchNodeEditor(pdgInfo, sliceManager.skeletonNodes, sliceManager, cfNode);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                case WHILE: {
                    FlowEditor editor = new WhileNodeFlowEditor(pdgInfo, sliceManager.skeletonNodes, sliceManager, cfNode);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                case FOR: {
                    FlowEditor editor = new ForNodeFlowEditor(pdgInfo, sliceManager.skeletonNodes, sliceManager, cfNode);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                case IF: {
                    FlowEditor editor = new IfNodeFlowEditor(pdgInfo, sliceManager.skeletonNodes, sliceManager, cfNode);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                default:
                    throw new IllegalStateException("new branch node type");
            }
        }
        // edit
        for (CFNode cfNode : editorMap.keySet()) {
            FlowEditor editor = editorMap.get(cfNode);
            editor.edit();
        }

        RootNodeEditor rootNodeEditor = new RootNodeEditor(pdgInfo, sliceManager.skeletonNodes, sliceManager, sliceManager.entryNode);
        rootNodeEditor.parse();
        rootNodeEditor.edit();
    }



    public SliceManager getTrackManager() {
        return sliceManager;
    }
}
