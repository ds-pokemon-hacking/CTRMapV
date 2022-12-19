package ctrmap.missioncontrol_ntr;

import java.util.ArrayList;
import java.util.List;
import ctrmap.missioncontrol_ntr.field.VPlayerController;

public class VMcConfig {

	private List<ConfigListener> listeners = new ArrayList<>();

	public boolean enableIKPlus = true;
	public boolean isDefaultRun = true;
	public VPlayerController.PlayerGender playerGender = VPlayerController.PlayerGender.MALE;

	public void addListener(ConfigListener l) {
		if (l != null && !listeners.contains(l)) {
			listeners.add(l);
		}
	}

	public void callListeners() {
		for (ConfigListener l : listeners) {
			l.onConfigUpdate();
		}
	}

	public static interface ConfigListener {

		public void onConfigUpdate();
	}
}
