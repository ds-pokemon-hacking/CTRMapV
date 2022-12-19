package ctrmap.editor.gui.editors.gen5.level.tools;

import xstandard.math.vec.RGBA;
import ctrmap.formats.pokemon.WorldObject;
import xstandard.util.ListenableList;
import ctrmap.editor.gui.editors.gen5.level.entities.VNPCEditor;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.missioncontrol_ntr.field.mmodel.VMoveModel;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;

public class VNPCTool extends VGridObjToolBase {

	private VNPCEditor nef;

	public VNPCTool(VNPCEditor nef) {
		super(nef.editors);
		this.nef = nef;
	}

	/*@Override
	public void setSelectedObject(WorldObject obj) {
		super.setSelectedObject(obj);
		if (nef.zone != null) {
			for (VMoveModel mmdl : nef.zone.NPCs) {
				for (Material mat : mmdl.resource.materials()) {
					//Make the selected NPC have depth test priority
					mat.depthColorMask.depthFunction = (obj == mmdl) ? MaterialParams.TestFunction.ALWAYS : MaterialParams.TestFunction.LEQ;
				}
			}
		}
	}*/

	@Override
	public AbstractToolbarEditor getEditor() {
		return nef;
	}

	@Override
	public JComponent getGUI() {
		return nef;
	}

	@Override
	public String getFriendlyName() {
		return "NPC";
	}

	@Override
	public boolean getNaviEnabled() {
		return true;
	}

	@Override
	public ListenableList getGridObjects() {
		if (nef.zone == null) {
			return new ListenableList();
		}
		return nef.zone.NPCs;
	}

	@Override
	public void showWorldObjInEditor(int index) {
		nef.setNPC(index);
	}

	@Override
	public String getResGroup() {
		return "npc";
	}

	@Override
	public RGBA getSelectionColor() {
		return RGBA.RED;
	}

	@Override
	public RGBA getRegLineColor() {
		return new RGBA(0, 0, 210, 255);
	}

	@Override
	public RGBA getRegFillColor() {
		return new RGBA(0, 0, 255, 50);
	}

	@Override
	public void updateComponents() {
		nef.refreshNoSave();
	}

	@Override
	public boolean getIsGizmoEnabled() {
		return false;
	}

	@Override
	public WorldObject getSelectedEditorObject() {
		return nef.npc;
	}
}
