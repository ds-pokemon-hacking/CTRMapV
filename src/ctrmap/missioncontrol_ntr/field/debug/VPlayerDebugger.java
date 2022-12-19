package ctrmap.missioncontrol_ntr.field.debug;

import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_ntr.field.VPlayerController;

public interface VPlayerDebugger extends IMCDebugger {
	public void bindPlayerController(VPlayerController ctrl);
	public void update();
	public void onPlayerMove();
	public boolean getIsWalkThroughWallsEnabled();
}
