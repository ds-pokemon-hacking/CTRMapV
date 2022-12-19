package ctrmap.editor.gui.editors.gen5.level.tools;

import ctrmap.formats.pokemon.WorldObject;
import ctrmap.editor.gui.editors.gen5.level.entities.VWarpEditor;
import xstandard.math.vec.RGBA;
import xstandard.util.ListenableList;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;

public class VWarpTool extends VGridObjToolBase {
    private VWarpEditor wef;

    public VWarpTool(VWarpEditor wef) {
        super(wef.editors);
        this.wef = wef;
    }

    @Override
    public AbstractToolbarEditor getEditor() {
        return wef;
    }

    @Override
    public JComponent getGUI() {
        return wef;
    }

    @Override
    public String getFriendlyName() {
        return "Warp";
    }

    @Override
    public boolean getNaviEnabled() {
        return true;
    }

    @Override
    public ListenableList getGridObjects() {
        if (wef.zone == null) {
            return new ListenableList();
        }
        return wef.zone.entities.warps;
    }

    @Override
    public void showWorldObjInEditor(int index) {
        wef.setWarp(index);
    }

    @Override
    public String getResGroup() {
        return "warp";
    }

    @Override
    public RGBA getSelectionColor() {
        return RGBA.RED;
    }

    @Override
    public RGBA getRegLineColor() {
        return new RGBA(0, 230, 0, 255);
    }

    @Override
    public RGBA getRegFillColor() {
        return new RGBA(0, 230, 0, 50);
    }

    @Override
    public void updateComponents() {
        wef.refreshNoSave();
    }

    @Override
    public boolean getIsGizmoEnabled() {
        return true;
    }

    @Override
    public WorldObject getSelectedEditorObject() {
        return wef.warp;
    }
}