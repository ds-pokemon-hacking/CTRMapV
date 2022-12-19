package ctrmap.missioncontrol_ntr.field.debug;

import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VArea;

public interface VAreaDebugger extends IMCDebugger {
	public void loadArea(VArea a);
}
