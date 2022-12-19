package ctrmap.editor.gui.editors.gen5.level.camera;

import ctrmap.formats.pokemon.gen5.camera.VAbstractCameraData;
import xstandard.gui.components.listeners.IKeyAdapter;
import xstandard.gui.components.listeners.IMouseAdapter;
import xstandard.gui.components.listeners.IMouseMotionAdapter;
import xstandard.math.vec.Vec3f;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import ctrmap.formats.pokemon.gen5.camera.VCameraCoordinates;
import ctrmap.formats.pokemon.gen5.camera.VCameraDataRect;
import ctrmap.missioncontrol_ntr.field.VFieldCameraController;

public class VCameraEditorVisualInputManager implements IKeyAdapter, IMouseAdapter, IMouseMotionAdapter {

	private int originMouseX;
	private int originMouseY;
	private ArrayList<Integer> keycodes = new ArrayList<>();

	private VCameraEditor mCamEditForm;

	public VCameraEditorVisualInputManager(VCameraEditor mCamEditForm) {
		this.mCamEditForm = mCamEditForm;
		startInputThread();
	}

	public void attachComponent(JComponent comp) {
		comp.addMouseListener(this);
		comp.addMouseMotionListener(this);
		comp.addKeyListener(this);
	}

	public void detachComponent(JComponent comp) {
		comp.removeMouseListener(this);
		comp.removeMouseMotionListener(this);
		comp.removeKeyListener(this);
		keycodes.clear();
	}

	private void startInputThread() {
		Thread inputThread = new Thread() {
			public boolean finishUpAndExit = false;

			public void run() {
				try {
					while (!finishUpAndExit) {
						if ((keycodes.contains(KeyEvent.VK_LEFT)
							|| keycodes.contains(KeyEvent.VK_RIGHT)
							|| keycodes.contains(KeyEvent.VK_UP)
							|| keycodes.contains(KeyEvent.VK_DOWN))) {

							CameraCoordinatesHandle co = getCamCoordsToAlterShowInEditor();
							VCameraCoordinateEditPanel panel1 = mCamEditForm.coords1Pnl;
							VCameraCoordinateEditPanel panel2 = mCamEditForm.coords2Pnl;

							if (co != null) {
								if (keycodes.contains(KeyEvent.VK_LEFT)) {
									co.FOV = -0.5f;
									co.applyAddToCoords();
									panel1.reloadFOV();
									panel2.reloadFOV();
								}
								if (keycodes.contains(KeyEvent.VK_RIGHT)) {
									co.FOV = 0.5f;
									co.applyAddToCoords();
									panel1.reloadFOV();
									panel2.reloadFOV();
								}

								if (keycodes.contains(KeyEvent.VK_UP)) {
									co.tz -= 2f;
									co.applyAddToCoords();
									panel1.reloadFOV();
									panel2.reloadFOV();
								}

								if (keycodes.contains(KeyEvent.VK_DOWN)) {
									co.tz += 2f;
									co.applyAddToCoords();
									panel1.reloadDist();
									panel2.reloadDist();
								}
							}
						}

						Thread.sleep(10);
					}
				} catch (InterruptedException e) {
				}
			}

			@Override
			public void interrupt() {
				finishUpAndExit = true;
			}
		};
		inputThread.start();
	}

	public void setOrigins(MouseEvent e) {
		originMouseX = e.getX();
		originMouseY = e.getY();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mCamEditForm.save();
		setOrigins(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mCamEditForm.forceReloadNoSave();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		CameraCoordinatesHandle co = getCamCoordsToAlterShowInEditor();
		VCameraCoordinateEditPanel panel1 = mCamEditForm.coords1Pnl;
		VCameraCoordinateEditPanel panel2 = mCamEditForm.coords2Pnl;
		if (co != null) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				//Rotations on camera areas are unsigned. Thus, we won't allow turning to negatives
				co.yaw = -(e.getX() - originMouseX) / 3f;
				co.pitch = (e.getY() - originMouseY) / 3f;

				co.applyAddToCoords();

				panel1.reloadPitchYaw();
				panel2.reloadPitchYaw();
			} else if (SwingUtilities.isRightMouseButton(e)) {
				float yaw = getRealYaw(co.lerped.yaw, co.lerped.tz, co.lerped.targetOffset.x, co.lerped.targetOffset.z);
				//System.out.println(yaw);

				float xAdd = (float) Math.cos(Math.toRadians(yaw));
				float zAdd = -(float) Math.sin(Math.toRadians(yaw));

				co.targetOffset.x += (e.getX() - originMouseX) * xAdd * 2f;
				co.targetOffset.z += (e.getX() - originMouseX) * zAdd * 2f;

				float sinPitch = (float) Math.sin(Math.toRadians(co.pitch));
				float cosPitch = (float) Math.cos(Math.toRadians(co.pitch));

				co.targetOffset.y -= (e.getY() - originMouseY) * cosPitch * 2f;

				co.applyAddToCoords();

				panel1.reloadTargetOffset();
				panel2.reloadTargetOffset();
			}
		}
		setOrigins(e);
	}

	public static float getRealYaw(float yaw, float zZoom, float offx, float offz) {
		Vec3f pos = new Vec3f(0f, 0f, 0f);
		double yawRad = Math.toRadians(yaw);
		pos.x += Math.sin(yawRad) * zZoom;
		pos.z += Math.cos(yawRad) * zZoom;
		return (float) Math.toDegrees(Math.atan2(pos.x - offx, pos.z - offz));
	}

	private CameraCoordinatesHandle getCamCoordsToAlterShowInEditor() {
		if (mCamEditForm.editors.dcc.getDebugCameraEnabled()) {
			return null;
		}
		if (mCamEditForm.editors.getVMC().field.map != null && mCamEditForm.editors.getVMC().field.map.cameras != null) {
			float pX = mCamEditForm.editors.getVMC().field.player.getX();
			float pZ = mCamEditForm.editors.getVMC().field.player.getZ();

			VAbstractCameraData c = mCamEditForm.editors.getVMC().field.map.getCamAtWorldLoc(pX, pZ);
			int index = mCamEditForm.cameras.entries.indexOf(c);
			if (mCamEditForm.getSelectedCamera() != c) {
				mCamEditForm.setCamera(index);
			}

			if (c != null && c instanceof VCameraDataRect) {
				VCameraDataRect rect = (VCameraDataRect) c;
				CameraCoordinatesHandle handle = new CameraCoordinatesHandle();
				handle.left = rect.coords1;
				handle.right = rect.coords2;
				handle.weight = VFieldCameraController.calcProgression(pZ, rect.gridZ, rect.gridH);
				if (handle.weight > 0.9f) {
					handle.weight = 1f;
				} else if (handle.weight < 0.1f) {
					handle.weight = 0f;
				}
				if (keycodes.contains(KeyEvent.VK_SHIFT)) {
					if (handle.weight < 0.5f) {
						handle.weight = 0f;
					} else {
						handle.weight = 1f;
					}
				}
				handle.lerped = new VCameraCoordinates();
				handle.left.lerp(handle.right, handle.lerped, handle.weight);

				return handle;
			}
		}
		return null;
	}

	public static class CameraCoordinatesHandle extends VCameraCoordinates {

		public VCameraCoordinates left;
		public VCameraCoordinates right;

		public VCameraCoordinates lerped;

		public float weight;

		public CameraCoordinatesHandle() {
			super();
			setNull();
		}

		public void applyAddToCoords() {
			left.addWeighted(this, 1f - weight);
			right.addWeighted(this, weight);

			left.normalizeAngles();
			right.normalizeAngles();
			left.restrictFxAnglesUnsigned();
			right.restrictFxAnglesUnsigned();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!keycodes.contains(e.getKeyCode())) {
			keycodes.add(e.getKeyCode());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keycodes.remove((Integer) e.getKeyCode());
	}

	@Override
	public void mouseExited(MouseEvent e) {
		keycodes.clear();
	}
}
