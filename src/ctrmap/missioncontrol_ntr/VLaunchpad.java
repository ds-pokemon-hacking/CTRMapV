package ctrmap.missioncontrol_ntr;

import ctrmap.missioncontrol_base.IMissionControl;
import ctrmap.missioncontrol_ntr.field.VFieldController;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.util.List;

public class VLaunchpad extends IMissionControl {

	public boolean residentMode = true;

	public NTRGameFS fs;

	public VMcConfig config;

	public VFieldController field;
	
	public VRTC.Season season = VRTC.Season.getRTC();
	
	@Override
	protected void onInit() {
		fs = new NTRGameFS(fsManager, game);

		fs.initAccurateArcFileSupport();

		log.out("Mission control has succesfully initialized.", VMCModuleLogSource.BASE);
	}
	
	@Override
	public List<String> updateVideoBackend() {
		List<String> res = super.updateVideoBackend();
		if (res.isEmpty()) {
//			backend.getProgramManager().getUserShManager().addIncludeDirectory(ResourceAccess.getResourceFile("missioncontrol_v"));
		}
		return res;
	}

	public void start(VMcConfig config) {
		this.config = config;
		field = new VFieldController(this);
	}
}
