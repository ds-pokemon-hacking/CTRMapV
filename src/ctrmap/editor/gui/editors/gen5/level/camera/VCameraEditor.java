package ctrmap.editor.gui.editors.gen5.level.camera;

import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.editor.gui.editors.gen5.level.VLevelEditor;
import ctrmap.editor.gui.editors.gen5.level.tools.VCameraTool;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import ctrmap.formats.pokemon.gen5.camera.VAbstractCameraData;
import ctrmap.formats.pokemon.gen5.camera.VCameraAreaType;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.PlusMinusButtonSet;
import xstandard.util.ArraysEx;
import ctrmap.util.gui.CMGUI;
import java.util.List;
import ctrmap.formats.pokemon.gen5.camera.VCameraDataRect;
import ctrmap.formats.pokemon.gen5.camera.VCameraDataFile;
import ctrmap.missioncontrol_ntr.field.debug.VMapDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VCameraEditor extends javax.swing.JPanel implements AbstractToolbarEditor, VMapDebugger {

	public VLevelEditor editors;

	private VCameraTool tool;

	public VCameraDataFile cameras;

	public VCameraEditorVisualInputManager input;

	private VAbstractCameraData cam;

	public VCameraEditor(VLevelEditor editors) {
		initComponents();
		this.editors = editors;
		tool = new VCameraTool(this);
		input = new VCameraEditorVisualInputManager(this);

		addRemBtns.addListener(new PlusMinusButtonSet.PMButtonListener() {
			@Override
			public void plusClicked() {
				if (cameras != null) {
					VCameraDataRect newCamData = new VCameraDataRect();

					CMGUI.addToComboBoxAndListWorldObjSimple(newCamData, cameras.entries, camEntryBox, editors, "Camera ");
				}
			}

			@Override
			public void minusClicked() {
				if (cameras != null) {
					VAbstractCameraData cam = cameras.entries.getOrDefault(camEntryBox.getSelectedIndex(), null);

					if (cam != null) {
						VCameraEditor.this.cam = null;
						CMGUI.removeFromComboBoxAndList(cam, cameras.entries, camEntryBox);
					}
				}
			}
		});
		
		ActionListener transitionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cam != null) {
					if (cam.getType() == VCameraAreaType.RECTANGLE) {
						VCameraDataRect rect = (VCameraDataRect) cam;
						if (btnTransitionHorizontal.isSelected()) {
							rect.horizontal = true;
						}
						else if (btnTransitionVertical.isSelected()) {
							rect.horizontal = false;
						}
					}
				}
			}
		};
		btnTransitionHorizontal.addActionListener(transitionListener);
		btnTransitionVertical.addActionListener(transitionListener);

		ComponentUtils.setNFValueClass(Integer.class, xPos, zPos, width, height);
	}

	private void showCamera() {
		int idx = camEntryBox.getSelectedIndex();
		if (cameras != null && idx != -1) {
			cam = cameras.entries.get(idx);

			if (cam.getType() == VCameraAreaType.RECTANGLE) {
				VCameraDataRect rect = (VCameraDataRect) cam;

				xPos.setValue(rect.gridX);
				zPos.setValue(rect.gridZ);
				width.setValue(rect.gridW);
				height.setValue(rect.gridH);
				if (rect.horizontal) {
					btnTransitionHorizontal.setSelected(rect.horizontal);
				}
				else {
					btnTransitionHorizontal.setSelected(!rect.horizontal);
				}
				unk2.setValue(rect.unk2);
				btnIsMainCaps1.setSelected(rect.stayCalcFunc >= 2);
				btnIsMainCaps2.setSelected(rect.stayCalcFunc >= 5);
				btnIsEntryCaps1.setSelected(rect.enterCalcFunc >= 2);
				btnIsEntryCaps2.setSelected(rect.enterCalcFunc >= 5);
				btnIsExitCaps1.setSelected(rect.exitCalcFunc >= 2);
				btnIsExitCaps2.setSelected(rect.exitCalcFunc >= 5);

				coords1Pnl.showCoords(rect.coords1);
				coords2Pnl.showCoords(rect.coords2);
			}
		} else {
			cam = null;
			coords1Pnl.showCoords(null);
			coords2Pnl.showCoords(null);
			ComponentUtils.clearComponents(btnIsCamEnabled, btnIsEntryCaps1, btnIsEntryCaps2, btnIsExitCaps1, btnIsExitCaps2, btnIsMainCaps1, btnIsMainCaps2,
				xPos, zPos, width, height, unk2, btnTransitionHorizontal, btnTransitionVertical);
		}
		tool.setSelectedObject(cam);
	}

	public void setCamera(int index) {
		ComponentUtils.setSelectedIndexSafe(camEntryBox, index);
	}

	public VAbstractCameraData getSelectedCamera() {
		if (cameras != null) {
			return cameras.entries.getOrDefault(camEntryBox.getSelectedIndex(), null);
		}
		return null;
	}

	public void forceReloadNoSave() {
		showCamera();
	}

	public void save() {
		if (cam != null) {
			coords1Pnl.save();
			coords2Pnl.save();

			if (cam.getType() == VCameraAreaType.RECTANGLE) {
				VCameraDataRect rect = (VCameraDataRect) cam;

				rect.gridX = (Integer) xPos.getValue();
				rect.gridZ = (Integer) zPos.getValue();
				rect.gridW = (Integer) width.getValue();
				rect.gridH = (Integer) height.getValue();
				rect.unk2 = ((Number) unk2.getValue()).intValue();

				rect.stayCalcFunc = btnIsMainCaps2.isSelected() ? 5 : btnIsMainCaps1.isSelected() ? 2 : 0;
				rect.enterCalcFunc = btnIsEntryCaps2.isSelected() ? 6 : btnIsEntryCaps1.isSelected() ? 3 : 0;
				rect.exitCalcFunc = btnIsExitCaps2.isSelected() ? 7 : btnIsExitCaps1.isSelected() ? 4 : 0;
			}
		}
	}

	@Override
	public boolean store(boolean dialog) {
		if (cameras != null) {
			save();
			return CMGUI.commonSaveDataSequence(editors.getCTRMap(), cameras.hash, dialog, "Camera data", false, (() -> {
				cameras.write();
			}));
		}
		return true;
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        transitionBtnGroup = new javax.swing.ButtonGroup();
        camEntryLabel = new javax.swing.JLabel();
        camEntryBox = new javax.swing.JComboBox<>();
        addRemBtns = new xstandard.gui.components.PlusMinusButtonSet();
        generalPanel = new javax.swing.JPanel();
        btnIsCamEnabled = new javax.swing.JCheckBox();
        camPosLabel = new javax.swing.JLabel();
        posXLabel = new javax.swing.JLabel();
        xPos = new javax.swing.JFormattedTextField();
        posZLabel = new javax.swing.JLabel();
        zPos = new javax.swing.JFormattedTextField();
        height = new javax.swing.JFormattedTextField();
        heightLabel = new javax.swing.JLabel();
        width = new javax.swing.JFormattedTextField();
        widthLabel = new javax.swing.JLabel();
        dimLabel = new javax.swing.JLabel();
        hFlagLabel = new javax.swing.JLabel();
        unk2 = new javax.swing.JSpinner();
        dimLabel1 = new javax.swing.JLabel();
        btnTransitionVertical = new javax.swing.JRadioButton();
        btnTransitionHorizontal = new javax.swing.JRadioButton();
        btnSave = new javax.swing.JButton();
        capsPanel = new javax.swing.JPanel();
        mainCapsLabel = new javax.swing.JLabel();
        btnIsMainCaps1 = new javax.swing.JCheckBox();
        btnIsMainCaps2 = new javax.swing.JCheckBox();
        entryCapsLabel = new javax.swing.JLabel();
        btnIsEntryCaps1 = new javax.swing.JCheckBox();
        btnIsEntryCaps2 = new javax.swing.JCheckBox();
        exitCapsLabel = new javax.swing.JLabel();
        btnIsExitCaps1 = new javax.swing.JCheckBox();
        btnIsExitCaps2 = new javax.swing.JCheckBox();

        camEntryLabel.setText("Camera entry:");

        camEntryBox.setMaximumRowCount(20);
        camEntryBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                camEntryBoxActionPerformed(evt);
            }
        });

        generalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));

        btnIsCamEnabled.setText("Enabled");

        camPosLabel.setText("Position:");

        posXLabel.setForeground(new java.awt.Color(255, 0, 0));
        posXLabel.setText("X");

        xPos.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        posZLabel.setForeground(new java.awt.Color(0, 0, 255));
        posZLabel.setText("Z");

        zPos.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        height.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        heightLabel.setForeground(new java.awt.Color(0, 0, 255));
        heightLabel.setText("Height");

        width.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        widthLabel.setForeground(new java.awt.Color(255, 0, 0));
        widthLabel.setText("Width");

        dimLabel.setText("Dimensions:");

        hFlagLabel.setText("Unk2");

        unk2.setModel(new javax.swing.SpinnerNumberModel());

        dimLabel1.setText("Transition:");

        transitionBtnGroup.add(btnTransitionVertical);
        btnTransitionVertical.setText("Vertical");

        transitionBtnGroup.add(btnTransitionHorizontal);
        btnTransitionHorizontal.setText("Horizontal");

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dimLabel1)
                            .addGroup(generalPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(generalPanelLayout.createSequentialGroup()
                                        .addComponent(hFlagLabel)
                                        .addGap(6, 6, 6)
                                        .addComponent(unk2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(generalPanelLayout.createSequentialGroup()
                                        .addComponent(btnTransitionVertical)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnTransitionHorizontal)))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnIsCamEnabled)
                            .addComponent(camPosLabel)
                            .addComponent(dimLabel)
                            .addGroup(generalPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(generalPanelLayout.createSequentialGroup()
                                        .addComponent(posXLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(xPos, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(generalPanelLayout.createSequentialGroup()
                                        .addComponent(widthLabel)
                                        .addGap(6, 6, 6)
                                        .addComponent(width)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(generalPanelLayout.createSequentialGroup()
                                        .addComponent(posZLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(zPos, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(generalPanelLayout.createSequentialGroup()
                                        .addComponent(heightLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(height)))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnIsCamEnabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(camPosLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(posXLabel)
                    .addComponent(xPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posZLabel)
                    .addComponent(zPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dimLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(widthLabel)
                    .addComponent(width, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightLabel)
                    .addComponent(height, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dimLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTransitionVertical)
                    .addComponent(btnTransitionHorizontal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(unk2)
                    .addComponent(hFlagLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        coords1Pnl.setBorder(javax.swing.BorderFactory.createTitledBorder("Coordinates 1"));
        coords1Pnl.setToolTipText("");

        coords2Pnl.setBorder(javax.swing.BorderFactory.createTitledBorder("Coordinates 2"));

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        capsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Capabilities"));

        mainCapsLabel.setText("Main");

        btnIsMainCaps1.setText("Pitch/Yaw/Dist");
        btnIsMainCaps1.setToolTipText("");

        btnIsMainCaps2.setText("FOV/Target");

        entryCapsLabel.setText("Entry");

        btnIsEntryCaps1.setText("Pitch/Yaw/Dist");
        btnIsEntryCaps1.setToolTipText("");

        btnIsEntryCaps2.setText("FOV/Target");

        exitCapsLabel.setText("Exit");

        btnIsExitCaps1.setText("Pitch/Yaw/Dist");
        btnIsExitCaps1.setToolTipText("");

        btnIsExitCaps2.setText("FOV/Target");

        javax.swing.GroupLayout capsPanelLayout = new javax.swing.GroupLayout(capsPanel);
        capsPanel.setLayout(capsPanelLayout);
        capsPanelLayout.setHorizontalGroup(
            capsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(capsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(capsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(entryCapsLabel)
                    .addComponent(exitCapsLabel)
                    .addComponent(mainCapsLabel))
                .addGap(18, 18, 18)
                .addGroup(capsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(capsPanelLayout.createSequentialGroup()
                        .addComponent(btnIsMainCaps1)
                        .addGap(18, 18, 18)
                        .addComponent(btnIsMainCaps2))
                    .addGroup(capsPanelLayout.createSequentialGroup()
                        .addComponent(btnIsExitCaps1)
                        .addGap(18, 18, 18)
                        .addComponent(btnIsExitCaps2))
                    .addGroup(capsPanelLayout.createSequentialGroup()
                        .addComponent(btnIsEntryCaps1)
                        .addGap(18, 18, 18)
                        .addComponent(btnIsEntryCaps2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        capsPanelLayout.setVerticalGroup(
            capsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(capsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(capsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mainCapsLabel)
                    .addComponent(btnIsMainCaps1)
                    .addComponent(btnIsMainCaps2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(capsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(entryCapsLabel)
                    .addComponent(btnIsEntryCaps1)
                    .addComponent(btnIsEntryCaps2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(capsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exitCapsLabel)
                    .addComponent(btnIsExitCaps1)
                    .addComponent(btnIsExitCaps2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(generalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(camEntryLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(camEntryBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addRemBtns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(coords1Pnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(coords2Pnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnSave))
                    .addComponent(capsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addRemBtns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(camEntryLabel)
                        .addComponent(camEntryBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(coords1Pnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(coords2Pnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(capsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		save();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void camEntryBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_camEntryBoxActionPerformed
		save();
		showCamera();
    }//GEN-LAST:event_camEntryBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private xstandard.gui.components.PlusMinusButtonSet addRemBtns;
    private javax.swing.JCheckBox btnIsCamEnabled;
    private javax.swing.JCheckBox btnIsEntryCaps1;
    private javax.swing.JCheckBox btnIsEntryCaps2;
    private javax.swing.JCheckBox btnIsExitCaps1;
    private javax.swing.JCheckBox btnIsExitCaps2;
    private javax.swing.JCheckBox btnIsMainCaps1;
    private javax.swing.JCheckBox btnIsMainCaps2;
    private javax.swing.JButton btnSave;
    private javax.swing.JRadioButton btnTransitionHorizontal;
    private javax.swing.JRadioButton btnTransitionVertical;
    private javax.swing.JComboBox<String> camEntryBox;
    private javax.swing.JLabel camEntryLabel;
    private javax.swing.JLabel camPosLabel;
    private javax.swing.JPanel capsPanel;
    public final ctrmap.editor.gui.editors.gen5.level.camera.VCameraCoordinateEditPanel coords1Pnl = new ctrmap.editor.gui.editors.gen5.level.camera.VCameraCoordinateEditPanel();
    public final ctrmap.editor.gui.editors.gen5.level.camera.VCameraCoordinateEditPanel coords2Pnl = new ctrmap.editor.gui.editors.gen5.level.camera.VCameraCoordinateEditPanel();
    private javax.swing.JLabel dimLabel;
    private javax.swing.JLabel dimLabel1;
    private javax.swing.JLabel entryCapsLabel;
    private javax.swing.JLabel exitCapsLabel;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JLabel hFlagLabel;
    private javax.swing.JFormattedTextField height;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JLabel mainCapsLabel;
    private javax.swing.JLabel posXLabel;
    private javax.swing.JLabel posZLabel;
    private javax.swing.ButtonGroup transitionBtnGroup;
    private javax.swing.JSpinner unk2;
    private javax.swing.JFormattedTextField width;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JFormattedTextField xPos;
    private javax.swing.JFormattedTextField zPos;
    // End of variables declaration//GEN-END:variables

	@Override
	public List<AbstractTool> getTools() {
		return ArraysEx.asList(tool);
	}

	@Override
	public void loadMapMatrix(VMap map) {
		cameras = map == null ? null : map.cameras;

		camEntryBox.removeAllItems();
		if (cameras != null) {
			for (int i = 0; i < cameras.entries.size(); i++) {
				camEntryBox.addItem("Camera " + i);
			}
		}
		tool.fullyRebuildScene();

		setCamera(0);
	}
}
