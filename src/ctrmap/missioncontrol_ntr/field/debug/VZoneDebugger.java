package ctrmap.missioncontrol_ntr.field.debug;

import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VZone;

public interface VZoneDebugger extends IMCDebugger {
	public void loadZone(VZone z);
}
