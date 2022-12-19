package ctrmap.missioncontrol_ntr.field.debug;

import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_ntr.field.VFieldController;

public interface VFieldDebugger extends IMCDebugger {
	public void attachField(VFieldController ctrl);
}
