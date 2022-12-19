package ctrmap.editor.gui.editors.gen5.level;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.renderer.scene.Scene;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.common.collision.ICollisionProvider;
import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.field.debug.VMapDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VMap;
import ctrmap.util.gui.cameras.FPSCameraInputManager;
import ctrmap.util.gui.cameras.OrthoCameraInputManager;
import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import xstandard.util.ArraysEx;

public class VLevelEditor extends AbstractPerspective {

	public VLevelEditor(CTRMap cm) {
		super(cm);
	}

	public VLaunchpad getVMC() {
		return (VLaunchpad) ctrmap.mcInUse;
	}

	private final VMapDebugger mapLoadCamAdjustDebugger = new VMapDebugger() {
		@Override
		public void loadMapMatrix(VMap map) {
			if (map != null) {
				Point mtxStartPnt = map.matrix.getFirstMatrixPoint(map.matrixZoneId);
				Dimension activeMtxDim = map.matrix.getActiveDimensions(map.matrixZoneId);
				FPSCameraInputManager fpsCamera = m3DInput.fpsCamera;
				boolean oldAllowMotion = fpsCamera.getAllowMotion();
				fpsCamera.setAllowMotion(true);
				fpsCamera.setTX((mtxStartPnt.x + activeMtxDim.width / 2f) * map.chunkSpan);
				fpsCamera.setTY(activeMtxDim.height * map.chunkSpan);
				fpsCamera.setTZ((mtxStartPnt.y + activeMtxDim.height * 2f) * map.chunkSpan);
				fpsCamera.setPitch(-35f);
				fpsCamera.setYaw(0);
				fpsCamera.setAllowMotion(oldAllowMotion);
				OrthoCameraInputManager orthoCam = tilemapInput.orthoCam;
				oldAllowMotion = orthoCam.getAllowMotion();
				orthoCam.setAllowMotion(true);
				orthoCam.setZoom(Math.max(activeMtxDim.width, activeMtxDim.height) * map.chunkSpan);
				orthoCam.setCenter(
					(mtxStartPnt.x + activeMtxDim.width / 2f) * map.chunkSpan,
					(mtxStartPnt.y + activeMtxDim.height / 2f) * map.chunkSpan
				);
				orthoCam.setAllowMotion(oldAllowMotion);
			}
		}
	};

	@Override
	public List<? extends IMCDebugger> getExtraDebuggers() {
		return ArraysEx.asList(mapLoadCamAdjustDebugger);
	}

	@Override
	public String getName() {
		return "Level Editor (Gen V)";
	}

	@Override
	public void onEditorActivated() {
		super.onEditorActivated();
		getVMC().field.fieldOpen();
	}

	@Override
	public void onEditorDeactivated() {
		super.onEditorDeactivated();
		getVMC().field.fieldClose();
	}

	@Override
	public void onDCCCameraChanged() {
		if (getVMC().field != null) {
			getVMC().field.camera.attach(dcc);
		}
	}

	@Override
	public Scene getInjectionScene() {
		if (getVMC().field != null) {
			return getVMC().field.fieldScene;
		}
		return null;
	}

	@Override
	public ICollisionProvider getWorldCollisionProvider() {
		return getVMC().field.map;
	}

	@Override
	public boolean isGameSupported(GameInfo game) {
		return game.isGenV();
	}
}
