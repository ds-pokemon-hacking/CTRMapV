package ctrmap.editor.gui.editors.gen5.level.tools;

import xstandard.math.vec.RGBA;
import ctrmap.formats.pokemon.WorldObject;
import xstandard.util.ListenableList;
import ctrmap.editor.gui.editors.gen5.level.entities.VProxyEditor;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;

public class VProxyTool extends VGridObjToolBase {
	
	private VProxyEditor editor;
	
	public VProxyTool(VProxyEditor editor){
		super(editor.editors);
		this.editor = editor;
	}

	@Override
	public AbstractToolbarEditor getEditor() {
		return editor;
	}

	@Override
	public JComponent getGUI() {
		return editor;
	}

	@Override
	public String getFriendlyName() {
		return "Proxy";
	}

	@Override
	public boolean getNaviEnabled() {
		return true;
	}

	@Override
	public ListenableList getGridObjects() {
		if (editor.zone == null || editor.zone.entities == null){
			return new ListenableList();
		}
		return editor.zone.entities.furniture;
	}

	@Override
	public void showWorldObjInEditor(int index) {
		editor.setFurniture(index);
	}

	@Override
	public String getResGroup() {
		return "proxy";
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
		editor.refreshNoSave();
	}

	@Override
	public boolean getIsGizmoEnabled() {
		return false;
	}

	@Override
	public WorldObject getSelectedEditorObject() {
		return editor.furniture;
	}
}
