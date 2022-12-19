package ctrmap.editor.gui.editors.gen5.level.tools;

import xstandard.math.vec.RGBA;
import ctrmap.formats.pokemon.WorldObject;
import xstandard.util.ListenableList;
import ctrmap.editor.gui.editors.common.tools.worldobj.DynamicHeightInstanceAdapter;
import ctrmap.editor.gui.editors.common.tools.worldobj.WorldObjInstanceAdapter;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.editor.gui.editors.gen5.level.camera.VCameraEditor;
import ctrmap.formats.pokemon.gen5.camera.VCameraDataRect;

public class VCameraTool extends VGridObjToolBase {

	private VCameraEditor cef;

	public VCameraTool(VCameraEditor cef) {
		super(cef.editors);
		this.cef = cef;
	}

	@Override
	public void onToolInit() {
		cef.input.attachComponent(cef.editors.getRenderer().getGUI());
		super.onToolInit();
	}

	@Override
	public void onToolShutdown() {
		cef.input.detachComponent(cef.editors.getRenderer().getGUI());
		super.onToolShutdown();
	}

	@Override
	public AbstractToolbarEditor getEditor() {
		return cef;
	}

	@Override
	public JComponent getGUI() {
		return cef;
	}

	@Override
	public String getFriendlyName() {
		return "Camera";
	}

	@Override
	public boolean getNaviEnabled() {
		return false;
	}

	@Override
	protected WorldObjInstanceAdapter createInstanceAdapter(WorldObject obj) {
		if (obj instanceof VCameraDataRect) {
			return new DynamicHeightInstanceAdapter(obj, unitSize, this, cef.editors.getVMC().field.map);
		} else {
			return super.createInstanceAdapter(obj);
		}
	}

	@Override
	public ListenableList getGridObjects() {
		if (cef.cameras != null) {
			return cef.cameras.entries;
		}
		return new ListenableList();
	}

	@Override
	public void showWorldObjInEditor(int index) {
		cef.setCamera(index);
	}

	@Override
	public String getResGroup() {
		return "cam";
	}

	@Override
	public RGBA getSelectionColor() {
		return RGBA.RED;
	}

	@Override
	public RGBA getRegLineColor() {
		return new RGBA(0, 128, 255, 255);
	}

	@Override
	public RGBA getRegFillColor() {
		return new RGBA(0, 128, 128, 50);
	}

	@Override
	public void updateComponents() {
		cef.forceReloadNoSave();
	}

	@Override
	public boolean getIsGizmoEnabled() {
		return true;
	}

	@Override
	public WorldObject getSelectedEditorObject() {
		return cef.getSelectedCamera();
	}
}
