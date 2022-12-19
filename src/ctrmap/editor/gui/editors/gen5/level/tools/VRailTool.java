package ctrmap.editor.gui.editors.gen5.level.tools;

import ctrmap.editor.gui.editors.common.tools.worldobj.WorldObjToolBase;
import ctrmap.editor.gui.editors.gen5.level.rail.VRailSelector;
import ctrmap.formats.pokemon.WorldObject;
import ctrmap.editor.gui.editors.gen5.level.rail.VRailEditor;
import ctrmap.editor.gui.editors.common.tools.worldobj.WorldObjInstanceAdapter;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.util.ObjectSelection;
import xstandard.math.vec.RGBA;
import xstandard.util.ListenableList;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;

public class VRailTool extends WorldObjToolBase {

	public final VRailEditor ref;
	protected VRailSelector selector;

	private Scene scene = new Scene("VRailToolG3DEx");

	public VRailTool(VRailEditor ref) {
		super(ref.editors, VFieldConstants.TILE_REAL_SIZE);
		this.ref = ref;
		this.selector = new VRailSelector(this);
		scene.addChild(ref.renderer);
		scene.addChild(selector);
		scene.addChild(worldObjScene);
	}

	@Override
	public JComponent getGUI() {
		return ref;
	}

	@Override
	public String getFriendlyName() {
		return "Rail";
	}

	@Override
	public AbstractToolbarEditor getEditor() {
		return ref;
	}

	@Override
	public void onTileMouseMoved(MouseEvent e) {
		selectRailPosByEvent(e);
	}
	
	@Override
	public void onTileMouseDown(MouseEvent e) {
		super.onTileMouseDown(e);
		ref.showLine(selector.selectedRail);
	}

	@Override
	public String getResGroup() {
		return "rail";
	}

	@Override
	public Scene getG3DEx() {
		return scene;
	}
	
	@Override
	protected boolean isEnforceShaderlessSelection(){
		return true;
	}

	public void selectRailPosByEvent(MouseEvent e) {
		int selObjId = ObjectSelection.getSelectedObjectIDSHA(e, ref.editors.getRenderer());

		if (selObjId != -1) {
			int lineNo = selObjId & 0xFF;
			if (lineNo != -1) {
				selector.selPosY = (selObjId >> 8) & 0xFF;
				selector.selPosX = ((selObjId >> 16) & 0xFF) - 64;
			}
			selector.selectedRail = lineNo;
			if (selector.getCurrentLine() != null) {
				//System.out.println("pos " + selector.selectedRail + " xy " + selector.selPosX + "x" + selector.selPosY);
				//System.out.println(selector.getCurrentLine().getAbsPositionOnRail(selector.selPosX, selector.selPosY, true));
			}
		} else {
			selector.selectedRail = -1;
		}
	}

	@Override
	public boolean getNaviEnabled() {
		return false;
	}

	@Override
	public RGBA getSelectionColor() {
		return RGBA.RED;
	}

	@Override
	public RGBA getRegLineColor() {
		return RGBA.GREEN;
	}

	@Override
	public RGBA getRegFillColor() {
		return new RGBA(0, 220, 20, 50);
	}
	
	@Override
	protected WorldObjInstanceAdapter getCreateInstanceAdapter(WorldObject obj){
		WorldObjInstanceAdapter a  = super.getCreateInstanceAdapter(obj);
		for (Model mdl : a.resource.models){
			for (Material mat : mdl.materials){
				//This shader forces the object selection value to be 0, so that moving the
				//point handles around does not select the rails under them
				mat.addShaderExtension("RailRendererVWorldObjOverlayShader.fsh_ext");
			}
		}
		return a;
	}

	@Override
	public boolean getIsGizmoEnabled() {
		return false;
	}

	@Override
	public ListenableList<? extends WorldObject> getWorldObjects() {
		return ref.lineEditorObjects;
	}

	@Override
	public void showWorldObjInEditor(int index) {
		
	}

	@Override
	public WorldObject getSelectedEditorObject() {
		return null;
	}

	@Override
	public void updateComponents() {
		
	}
}
