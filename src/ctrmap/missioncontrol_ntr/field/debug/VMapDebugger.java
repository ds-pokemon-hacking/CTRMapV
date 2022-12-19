
package ctrmap.missioncontrol_ntr.field.debug;

import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VMap;

public interface VMapDebugger extends IMCDebugger {
	public void loadMapMatrix(VMap chunks);
}
