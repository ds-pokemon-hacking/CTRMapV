package ctrmap.missioncontrol_ntr.field;

import ctrmap.missioncontrol_base.InputManager;

public class VFieldInput implements InputManager.McInput {

	private final VFieldController controller;
	private final InputManager man;

	public VFieldInput(InputManager man, VFieldController cnt) {
		controller = cnt;
		this.man = man;
	}

	protected long input_lastUpdateTimestamp;

	@Override
	public void init() {
		input_lastUpdateTimestamp = System.currentTimeMillis();
	}

	@Override
	public void update() {
		if (!(controller.camera.isUsingDebugCamera())) {
			if (input_lastUpdateTimestamp == 0) {
				init();
			} else {
				//if the above is true, we don't want to move the player character as the buttons are used to move the camera
				long input_timestamp = System.currentTimeMillis();

				long diff = input_timestamp - input_lastUpdateTimestamp;

				controller.player.doInputLoop(diff, man);

				input_lastUpdateTimestamp = input_timestamp;
			}
		}
	}
}
