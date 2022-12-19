package ctrmap.editor.gui.editors.gen5.level.tools;

import ctrmap.editor.gui.editors.common.tools.worldobj.WorldObjToolBase;
import xstandard.math.vec.RGBA;
import ctrmap.formats.pokemon.WorldObject;
import xstandard.util.ListenableList;
import ctrmap.editor.gui.editors.gen5.level.building.VPropEditor;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;

public class VPropTool extends WorldObjToolBase {
	
	private VPropEditor pef;
	
	public VPropTool(VPropEditor pef){
		super(pef.editors, VFieldConstants.TILE_REAL_SIZE);
		this.pef = pef;
	}

	@Override
	public AbstractToolbarEditor getEditor() {
		return pef;
	}

	@Override
	public JComponent getGUI() {
		return pef;
	}

	@Override
	public String getFriendlyName() {
		return "Prop";
	}

	@Override
	public boolean getNaviEnabled() {
		return true;
	}

	@Override
	public ListenableList getWorldObjects() {
		if (pef.map == null){
			return new ListenableList();
		}
		return pef.map.buildings;
	}

	@Override
	public void showWorldObjInEditor(int index) {
		pef.setProp(index);
	}

	@Override
	public String getResGroup() {
		return "prop";
	}

	@Override
	public RGBA getSelectionColor() {
		return RGBA.RED;
	}

	@Override
	public RGBA getRegLineColor() {
		return new RGBA(210, 210, 0, 255);
	}

	@Override
	public RGBA getRegFillColor() {
		return new RGBA(210, 210, 0, 50);
	}
	
	@Override
	public void updateComponents(){
		pef.refreshNoSave();
	}

	@Override
	public boolean getIsGizmoEnabled() {
		return false;
	}

	@Override
	public WorldObject getSelectedEditorObject() {
		return pef.prop;
	}
}
