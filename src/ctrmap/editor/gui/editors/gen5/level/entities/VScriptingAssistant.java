package ctrmap.editor.gui.editors.gen5.level.entities;

import ctrmap.editor.gui.editors.gen5.level.VLevelEditor;
import ctrmap.editor.gui.editors.gen5.level.tools.VScriptAssistantTool;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import ctrmap.renderer.scene.Camera;
import xstandard.util.ArraysEx;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.formats.pokemon.gen5.camera.VCameraCoordinates;
import ctrmap.missioncontrol_ntr.field.VFieldCameraController;
import xstandard.math.MathEx;

public class VScriptingAssistant extends javax.swing.JPanel implements AbstractToolbarEditor {

	private VLevelEditor editors;

	private VScriptAssistantTool tool;

	public VScriptingAssistant(VLevelEditor editors) {
		initComponents();
		this.editors = editors;
		tool = new VScriptAssistantTool(this);
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        evCameraMoveToLabel = new javax.swing.JLabel();
        btnMakeMoveTo = new javax.swing.JButton();
        evCameraInterval = new javax.swing.JSpinner();
        evCameraIntervalLabel = new javax.swing.JLabel();
        evCameraMoveToSep = new javax.swing.JSeparator();

        evCameraMoveToLabel.setText("EVCamera.MoveTo");

        btnMakeMoveTo.setText("Copy");
        btnMakeMoveTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMakeMoveToActionPerformed(evt);
            }
        });

        evCameraInterval.setModel(new javax.swing.SpinnerNumberModel(40, 0, null, 1));

        evCameraIntervalLabel.setText("Interval");

        evCameraMoveToSep.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(evCameraMoveToLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(evCameraMoveToSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(evCameraIntervalLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(evCameraInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMakeMoveTo)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(evCameraMoveToLabel)
                        .addComponent(btnMakeMoveTo)
                        .addComponent(evCameraInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(evCameraIntervalLabel))
                    .addComponent(evCameraMoveToSep, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(269, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnMakeMoveToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMakeMoveToActionPerformed
		copyToClipboard(buildMakeCameraFunc());
    }//GEN-LAST:event_btnMakeMoveToActionPerformed

	public String buildMakeCameraFunc() {
		float[] coords = createCameraCoordinates();
		StringBuilder sb = new StringBuilder();
		sb.append("EVCamera.MoveTo(");
		for (int i = 0; i < coords.length; i++) {
			sb.append(coords[i]);
			sb.append("f, ");
		}
		sb.append((Integer) evCameraInterval.getValue());
		sb.append(");");
		return sb.toString();
	}

	public static final float _FX16_PI_FULLRES = 8f / MathEx.PI;

	public float[] createCameraCoordinates() {
		float[] coords = new float[]{
			0, //pitch
			0, //yaw
			0, //dist
			0, //x
			0, //y
			0 //z
		};

		if (editors.dcc.getDebugCameraEnabled()) {
			Camera fpsCamera = editors.m3DInput.fpsCamera.cam;

			float pitchR = MathEx.toRadiansf(-fpsCamera.rotation.x);
			float yawR = MathEx.toRadiansf(fpsCamera.rotation.y);

			//Camera rotations are 90 degrees offset compared to the unit circle
			//In turn, goniometric operations can be swapped instead of offsetting the values manually
			float sy = (float) Math.sin(yawR);
			float cy = (float) Math.cos(yawR);
			float sp = (float) Math.sin(pitchR);
			float cp = (float) Math.cos(pitchR);

			coords[0] = pitchR * _FX16_PI_FULLRES;
			coords[1] = yawR * _FX16_PI_FULLRES;
			coords[2] = 50f;
			coords[3] = fpsCamera.translation.x - coords[2] * sy * cp;
			coords[4] = fpsCamera.translation.y - coords[2] * sp;
			coords[5] = fpsCamera.translation.z - coords[2] * cy * cp;
		} else {
			VFieldCameraController fldCam = editors.getVMC().field.camera;
			VCameraCoordinates c = fldCam.calcFixedCameraCoordinates();
			coords[0] = MathEx.toRadiansf(c.pitch) * _FX16_PI_FULLRES;
			coords[1] = MathEx.toRadiansf(c.yaw) * _FX16_PI_FULLRES;
			coords[2] = c.tz;
			coords[3] = fldCam.fixedCamera.lookAtTarget.x;
			coords[4] = fldCam.fixedCamera.lookAtTarget.y;
			coords[5] = fldCam.fixedCamera.lookAtTarget.z;
		}
		return coords;
	}

	private void copyToClipboard(String data) {
		StringSelection sel = new StringSelection(data);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
	}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMakeMoveTo;
    private javax.swing.JSpinner evCameraInterval;
    private javax.swing.JLabel evCameraIntervalLabel;
    private javax.swing.JLabel evCameraMoveToLabel;
    private javax.swing.JSeparator evCameraMoveToSep;
    // End of variables declaration//GEN-END:variables

	@Override
	public List<AbstractTool> getTools() {
		return ArraysEx.asList(tool);
	}
}
