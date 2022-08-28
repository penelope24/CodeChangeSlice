package fy.CDS.track;

import fy.CDS.data.SliceManager;
import fy.CDS.solver.cfg.edit.*;
import fy.PROGEX.parse.PDGInfo;
import ghaffarian.progex.graphs.cfg.CFNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CFGTracker {
    SliceManager sliceManager;
    PDGInfo pdgInfo;

    public CFGTracker(SliceManager sliceManager) {
        this.pdgInfo = sliceManager.pdgInfo;
        this.sliceManager = sliceManager;
    }

    // for test use
    public CFGTracker(PDGInfo pdgInfo) {
        this.pdgInfo = pdgInfo;
    }

    public void track() {
        Map<CFNode, FlowEditor> editorMap = parse(sliceManager.worklist);
        edit(editorMap);
    }

    public Map<CFNode, FlowEditor> parse (Set<CFNode> worklist) {
        // parse
        Map<CFNode, FlowEditor> editorMap = new LinkedHashMap<>();
        for (CFNode cfNode : worklist) {
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
        return editorMap;
    }

    // for test only
    public FlowEditor parse(CFNode targetNode, Set<CFNode> worklist) {
        Map<CFNode, FlowEditor> editorMap = new LinkedHashMap<>();
        for (CFNode cfNode : worklist) {
            switch (cfNode.getType()) {
                case SWITCH: {
                    FlowEditor editor = new SwitchNodeEditor(pdgInfo, cfNode);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                case WHILE: {
                    FlowEditor editor = new WhileNodeFlowEditor(pdgInfo, cfNode);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                case FOR: {
                    FlowEditor editor = new ForNodeFlowEditor(pdgInfo, cfNode);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                case IF: {
                    FlowEditor editor = new IfNodeFlowEditor(pdgInfo, cfNode);
                    editor.parse();
                    editorMap.put(cfNode, editor);
                    break;
                }
                default:

            }
        }
        return editorMap.get(targetNode);
    }

    public void edit(Map<CFNode, FlowEditor> editorMap) {
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
