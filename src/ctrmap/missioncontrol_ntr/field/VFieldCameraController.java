package ctrmap.missioncontrol_ntr.field;

import ctrmap.formats.pokemon.gen5.camera.VAbstractCameraData;
import ctrmap.renderer.scene.Camera;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import ctrmap.formats.pokemon.gen5.camera.VCameraCoordinates;
import ctrmap.formats.pokemon.gen5.camera.VCameraDataRect;
import ctrmap.formats.pokemon.gen5.camera.VDefaultCameraData;
import ctrmap.formats.pokemon.gen5.camera.VDefaultCameraDataFile;
import ctrmap.missioncontrol_base.DebugCameraController;
import ctrmap.missioncontrol_base.debug.IMCDebuggable;
import ctrmap.missioncontrol_ntr.field.structs.VZone;
import ctrmap.missioncontrol_ntr.fs.NARCRef;

public class VFieldCameraController implements IMCDebuggable<DebugCameraController> {

	public Camera fixedCamera = new Camera();

	private VFieldController controller;

	private VDefaultCameraDataFile defaultCameras;
	
	private DebugCameraController freeCamSrc;

	public VFieldCameraController(VFieldController cnt) {
		controller = cnt;
		defaultCameras = new VDefaultCameraDataFile(cnt.mc.fs.NARCGet(NARCRef.FIELD_CAMERA_DEFAULTS, 0));
		defaultCam = defaultCameras.entries.get(0);
		fixedCamera.name = "GameCamera";
		fixedCamera.viewMode = Camera.ViewMode.LOOK_AT;

		cnt.mc.getDebuggerManager().registDebuggable(this);
	}

	@Override
	public void attach(DebugCameraController dcc) {
		if (freeCamSrc == dcc) {
			return;
		}
		if (freeCamSrc != null) {
			freeCamSrc.deactivateCamera();
		}
		if (dcc != null) {
			freeCamSrc = dcc;
		} else {
			freeCamSrc = null;
		}
		if (freeCamSrc != null) {
			freeCamSrc.addSceneTarget(controller.fieldScene);
			freeCamSrc.onControllerActivated();
		}
	}
	
	@Override
	public void detach(DebugCameraController dcc) {
		dcc.deactivateCamera();
	}
	
	@Override
	public void destroy(DebugCameraController dcc) {
		
	}

	private final Matrix4 tempCamPosMtx = new Matrix4();

	public void updateLocal() {
		if (!isUsingDebugCamera()) {
			VCameraCoordinates c = calcFixedCameraCoordinates();

			fixedCamera.FOV = c.FOV;
			fixedCamera.zNear = defaultCam.zNear;
			fixedCamera.zFar = defaultCam.zFar;

			if (defaultCam.enablePlayerTarget) {
				fixedCamera.lookAtTarget.set(controller.player.getPosition());
			} else {
				fixedCamera.lookAtTarget.set(defaultCam.targetTranslation);
			}

			tempCamPosMtx.identity();
			tempCamPosMtx.translation(fixedCamera.lookAtTarget);

			fixedCamera.lookAtTarget.add(c.targetOffset);

			tempCamPosMtx.rotateY(MathEx.toRadiansf(c.yaw));
			tempCamPosMtx.rotateX(MathEx.toRadiansf(-c.pitch));
			tempCamPosMtx.translate(0, 0, c.tz);

			tempCamPosMtx.getTranslation(fixedCamera.translation);

			/*System.out.println("trans " + fixedCamera.translation);
			System.out.println("target " + fixedCamera.lookAtTarget);
			System.out.println("fov " + fixedCamera.FOV);
			System.out.println("orig rot " + c.pitch + "/" + c.yaw);*/
			freeCamSrc.deactivateCamera();
			controller.fieldScene.instantiateCamera(fixedCamera);
		} else {
			controller.fieldScene.deinstantiateCamera(fixedCamera);
			freeCamSrc.activateCamera();
		}
	}
	
	public boolean isUsingDebugCamera() {
		return freeCamSrc != null && freeCamSrc.getDebugCameraEnabled();
	}

	public void onZoneLoad(VZone zone) {
		defaultCam = defaultCameras.entries.get(zone.header.cameraIndex);

		setCoordsToDefault();
	}

	private void setCoordsToDefault() {
		currentCoords.FOV = defaultCam.FOV;
		currentCoords.pitch = defaultCam.rotation.x;
		currentCoords.yaw = defaultCam.rotation.y;
		currentCoords.targetOffset.set(defaultCam.targetTranslation);
		currentCoords.tz = defaultCam.tzDist;
	}

	public boolean motionLocked = false;

	private VDefaultCameraData defaultCam;
	private VCameraCoordinates currentCoords = new VCameraCoordinates();

	private VCameraCoordinates lerpTemp = new VCameraCoordinates();

	public static float calcProgression(float playerCoord, int camStart, int camDim) {
		return (playerCoord - camStart * VFieldConstants.TILE_REAL_SIZE) / (camDim * VFieldConstants.TILE_REAL_SIZE);
	}

	public VCameraCoordinates calcFixedCameraCoordinates() {
		float playerX = controller.player.getX();
		float playerZ = controller.player.getZ();
		VAbstractCameraData cam = controller.map.getCamAtWorldLoc(playerX, playerZ);
		if (cam != null) {
			switch (cam.getType()) {
				case RECTANGLE:
					VCameraDataRect rect = (VCameraDataRect) cam;

					float weight = rect.horizontal
						? calcProgression(playerX, rect.gridX, rect.gridW)
						: calcProgression(playerZ, rect.gridZ, rect.gridH);

					rect.coords1.lerp(rect.coords2, lerpTemp, weight);

					if (cam.stayCalcFunc >= 5) {
						currentCoords.FOV = lerpTemp.FOV;
						currentCoords.targetOffset.set(lerpTemp.targetOffset);
					}

					currentCoords.pitch = lerpTemp.pitch;
					currentCoords.yaw = lerpTemp.yaw;
					currentCoords.tz = lerpTemp.tz;
					break;
				case CIRCLE:
					//Not supported yet
					break;
			}

		} else {
			if (defaultCam != null) {
				setCoordsToDefault();
			}
		}

		return currentCoords;
	}

	@Override
	public Class<DebugCameraController> getDebuggerClass() {
		return DebugCameraController.class;
	}
}
