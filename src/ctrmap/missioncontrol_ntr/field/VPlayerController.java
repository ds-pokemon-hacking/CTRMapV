package ctrmap.missioncontrol_ntr.field;

import ctrmap.missioncontrol_base.InputManager;
import java.util.ArrayList;
import java.util.List;
import ctrmap.formats.common.GameInfo;
import xstandard.math.vec.Vec3f;
import java.awt.Point;
import ctrmap.formats.pokemon.gen5.zone.entities.VNPC;
import ctrmap.formats.pokemon.gen5.zone.entities.VWarp;
import ctrmap.missioncontrol_base.debug.IMCDebuggable;
import ctrmap.missioncontrol_ntr.VMcConfig;
import ctrmap.missioncontrol_ntr.field.debug.VFieldDebugger;
import ctrmap.missioncontrol_ntr.field.debug.VPlayerDebugger;
import ctrmap.missioncontrol_ntr.field.mmodel.VMoveModel;
import ctrmap.missioncontrol_ntr.field.structs.VZone;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;

public class VPlayerController implements VMcConfig.ConfigListener, IMCDebuggable<VPlayerDebugger> {

	public VFieldController parent;

	public VMoveModel playerMModel;

	private NTRGameFS modelFS;

	private GameInfo game;
	private VMcConfig config;

	public boolean isPlayerMotionEnabled = true;
	public float interpMotionLength = 5f;

	public VPlayerController(VFieldController parent, NTRGameFS fs, GameInfo gm, VMcConfig config) {
		this.parent = parent;
		this.config = config;
		playerMModel = new VMoveModel(parent, new VNPC(255, config.playerGender == PlayerGender.FEMALE ? 4 : 1));
		game = gm;
		modelFS = fs;
		onConfigUpdate();
		config.addListener(this);
		isInvertRun = config.isDefaultRun; //only set this on first config load, can be adjusted with CTRL

		parent.mc.getDebuggerManager().registDebuggable(this);
	}

	private int currentMMdl;

	public final void setModel(int mmIdx) {
		currentMMdl = mmIdx;
		parent.mc.getDebuggerManager().reattachDebuggers(this);
	}

	public void onZoneLoadSpawn(VZone zone) {
		zone.NPCs.add(playerMModel);
	}

	public void callDebuggersUpdate() {
		parent.mc.getDebuggerManager().callDebuggers(this, (debugger) -> {
			debugger.update();
		});
	}

	@Override
	public final void onConfigUpdate() {
		if (config.playerGender == PlayerGender.MALE) {
			setModel(1);
		} else {
			setModel(2);
		}
	}

	private boolean locked = false;

	public void lock() {
		locked = true;
	}

	public void unlock() {
		locked = false;
	}

	public boolean getIsLocked() {
		return locked;
	}

	private float motionAngle8wayModifier = 0;
	private boolean walkThroughWallsButton = false;

	public void doInputLoop(long updateDiff, InputManager manager) {
		if (isPlayerMotionEnabled) {
			if (manager.isButtonDown(InputManager.Button.LB)) {
				if (allowInvertRunChange) {
					isInvertRun = !isInvertRun;
					allowInvertRunChange = false;
				}
			} else {
				allowInvertRunChange = true;
			}
			walkThroughWallsButton = manager.isButtonDown(InputManager.Button.DEBUG0);
			isRun = manager.isButtonDown(InputManager.Button.B) ^ isInvertRun;
			float diff = (updateDiff) / 14f;
			if (isRun) {
				diff *= 2f;
			}

			if (manager.isStickActive(InputManager.Joystick.LEFT)) {
				double motionAngle8way = manager.getStickAngle(InputManager.Joystick.LEFT);
				motionAngle8way -= Math.toRadians(motionAngle8wayModifier);
				double playerMoveXFinal = roundIfClose(Math.cos(motionAngle8way)) * diff;
				double playerMoveYFinal = roundIfClose(-Math.sin(motionAngle8way)) * diff;

				if (!movePlayer((float) playerMoveXFinal, (float) playerMoveYFinal)) {
					//setAnimIdle();
					//reverse updown signum setting to allow yawshifting when a wall is hit (movePlayer() returns false)
				} else {
					//setAnimRunWalk();
				}
			} else if (!locked) {
				/*setAnimIdle();
				if (playerInput_idleStart == -1) {
					if (!specialState_stretching) {
						playerInput_idle_resetAndRandomize();
					}
				} else if (System.currentTimeMillis() - playerInput_idleStart > playerInput_idleLength) {
					playerInput_idleStart = -1;
					MotionIndex stretchMotionIdx = MotionIndex.TR_ACT_STRETCH_1;

					if (specialState_sitting) {
						stretchMotionIdx = MotionIndex.TR_ACT_STRETCH_SIT;
					} else {
						int decide12 = playerInput_idleLengthRandomizer.nextInt(2);
						if (decide12 == 1) {
							stretchMotionIdx = MotionIndex.TR_ACT_STRETCH_2;
						}
					}

					specialState_stretching = true;
					Runnable stretchDisableCallback = () -> {
						specialState_stretching = false;
						setAnimIdle(false);
					};
					setAnimationWithCallback(stretchMotionIdx, stretchDisableCallback);
				}*/
			}
		}
		callDebuggersUpdate();
	}

	private boolean allowInvertRunChange = false;
	private boolean isInvertRun = true;
	private boolean isRun = false;

	private static double roundIfClose(double d) {
		if (Math.abs(Math.round(d) - d) < 0.001) {
			return Math.round(d);
		} else {
			return d;
		}
	}

	public void setLocation(float x, float z) {
		playerMModel.setLocationPrecise(x, z);
	}

	public void setLocation(int x, int z, float y) {
		playerMModel.setLocation(x, z, y);
	}
	
	public void forceUpdateHeight(){
		playerMModel.p.y = parent.map.getHeightAtWorldLoc(playerMModel.p.x, playerMModel.p.y, playerMModel.p.z);
		playerMModel.NPCData.wposY = playerMModel.p.y;
	}

	public Vec3f getPosition() {
		return playerMModel.p;
	}

	public float getX() {
		return playerMModel.p.x;
	}

	public float getZ() {
		return playerMModel.p.z;
	}

	public void setFromWarp(VWarp warpSource, VWarp warpTarget) {
		float warpPercentage = 0f;
		Point p = playerMModel.getGPos();
		/*if (warpSource.faceDirection == VWarp.ContactDirection.UP || warpSource.faceDirection == VWarp.ContactDirection.DOWN) {
			warpPercentage = (p.x - warpSource.x) / warpSource.w;
			p.x = (int)(warpTarget.x + warpPercentage * warpTarget.w);
			p.y = (int)warpTarget.y;
		} else {
			warpPercentage = (p.y - warpSource.y) / warpSource.h;
			p.x = (int)warpTarget.x;
			p.y = (int)(warpTarget.y + warpPercentage * warpTarget.h);
		}*/
		setLocation(p.x, 0, p.y);
	}

	public boolean movePlayer(float xadd, float zadd) {
		return movePlayer(xadd, zadd, false);
	}

	public boolean movePlayer(float xadd, float zadd, boolean override) {
		override |= walkThroughWallsButton;
		if (!override) {
			override = parent.mc.getDebuggerManager().getBoolPermFromDebuggers(this, (debugger) -> {
				return debugger.getIsWalkThroughWallsEnabled();
			});
		}
		boolean success = override ? true : true;
		if (success) {
			setOrientation(xadd, zadd);
			setLocation(getPosition().x + xadd, getPosition().z + zadd);
			parent.mc.getDebuggerManager().callDebuggers(this, (debugger) -> {
				debugger.onPlayerMove();
			});
		}
		return success;
	}

	public boolean movePlayerAbs(float x, float z, boolean override) {
		return movePlayer(x - getPosition().x, z - getPosition().z, override);
	}

	public void setOrientation(float xAngComp, float yAngComp) {
		int dir = -1;
		if (xAngComp != 0){
			if (xAngComp > 0){
				dir = 3;
			}
			else {
				dir = 2;
			}
		}
		else if (yAngComp != 0){
			if (yAngComp > 0){
				dir = 1;
			}
			else {
				dir = 0;
			}
		}
		if (dir != -1){
			playerMModel.setOrientation(dir);
		}
	}

	@Override
	public Class<VPlayerDebugger> getDebuggerClass() {
		return VPlayerDebugger.class;
	}

	@Override
	public void attach(VPlayerDebugger debugger) {
		debugger.bindPlayerController(this);
	}

	@Override
	public void detach(VPlayerDebugger debugger) {
		debugger.bindPlayerController(null);
	}

	@Override
	public void destroy(VPlayerDebugger debugger) {
		debugger.bindPlayerController(null);
	}

	public static enum PlayerGender {
		MALE,
		FEMALE
	}
}
