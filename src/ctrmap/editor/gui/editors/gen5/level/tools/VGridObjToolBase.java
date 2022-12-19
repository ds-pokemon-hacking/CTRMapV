package ctrmap.editor.gui.editors.gen5.level.tools;

import xstandard.util.ListenableList;
import ctrmap.formats.pokemon.WorldObject;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.editor.gui.editors.common.tools.worldobj.WorldObjToolBase;
import ctrmap.formats.pokemon.gen5.zone.entities.VGridObject;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;

public abstract class VGridObjToolBase extends WorldObjToolBase {

	public VGridObjToolBase(AbstractPerspective edt){
		super(edt, VFieldConstants.TILE_REAL_SIZE);
	}
	
	protected abstract ListenableList<? extends VGridObject> getGridObjects();
	
	@Override
	public float getFixedDimGizmoSize(){
		return 0.65f;
	}
	
	@Override
	public ListenableList<? extends WorldObject> getWorldObjects(){
		return getGridObjects();
	}
}
