package ctrmap.editor.gui.editors.gen5.level.tools;

import xstandard.math.vec.RGBA;
import ctrmap.formats.pokemon.WorldObject;
import xstandard.util.ListenableList;
import ctrmap.editor.gui.editors.gen5.level.entities.VTriggerEditor;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;

public class VTriggerTool extends VGridObjToolBase {
	
	private VTriggerEditor tef;
	
	public VTriggerTool(VTriggerEditor nef){
		super(nef.editors);
		this.tef = nef;
	}

	@Override
	public AbstractToolbarEditor getEditor() {
		return tef;
	}

	@Override
	public JComponent getGUI() {
		return tef;
	}

	@Override
	public String getFriendlyName() {
		return "Trigger";
	}

	@Override
	public boolean getNaviEnabled() {
		return true;
	}

	@Override
	public ListenableList getGridObjects() {
		if (tef.zone == null){
			return new ListenableList();
		}
		return tef.zone.entities.triggers;
	}

	@Override
	public void showWorldObjInEditor(int index) {
		tef.setTrigger(index);
	}

	@Override
	public String getResGroup() {
		return "trigger";
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
	public void updateComponents(){
		tef.refreshNoSave();
	}

	@Override
	public boolean getIsGizmoEnabled() {
		return true;
	}

	@Override
	public WorldObject getSelectedEditorObject() {
		return tef.trigger;
	}
}
