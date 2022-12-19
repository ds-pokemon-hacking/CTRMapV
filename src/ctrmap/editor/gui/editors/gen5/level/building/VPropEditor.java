package ctrmap.editor.gui.editors.gen5.level.building;

import ctrmap.editor.gui.editors.gen5.level.VLevelEditor;
import ctrmap.editor.gui.editors.gen5.level.tools.VPropTool;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.PlusMinusButtonSet;
import xstandard.util.ArraysEx;
import xstandard.util.ReflectionHash;
import ctrmap.util.gui.CMGUI;
import java.util.ArrayList;
import java.util.List;
import ctrmap.formats.pokemon.gen5.buildings.ChunkBuilding;
import ctrmap.formats.pokemon.gen5.buildings.ChunkBuildings;
import ctrmap.missioncontrol_ntr.field.debug.VAreaDebugger;
import ctrmap.missioncontrol_ntr.field.debug.VMapDebugger;
import ctrmap.missioncontrol_ntr.field.map.BuildingInstance;
import ctrmap.missioncontrol_ntr.field.structs.VArea;
import ctrmap.missioncontrol_ntr.field.structs.VMap;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import xstandard.util.ListenableList;

public class VPropEditor extends javax.swing.JPanel implements AbstractToolbarEditor, VMapDebugger, VAreaDebugger {

	private VPropTool tool;

	public VMap map;
	private VArea area;
	public VLevelEditor editors;

	private boolean loaded = false;
	public BuildingInstance prop;

	private List<ChunkBuilding> hashArr = new ArrayList<>();
	private ReflectionHash hash = new ReflectionHash(hashArr);

	public VPropEditor(VLevelEditor editors) {
		initComponents();
		this.editors = editors;

		tool = new VPropTool(this);

		ComponentUtils.setNFValueClass(Float.class, tx, ty, tz, ry);

		bmSelect.addBuildingSelectionListener(new VBuildingSelectJList.BuildingSelectionListener() {
			@Override
			public void onBuildingRscSelected(int selectedBmResID) {
				if (prop != null && selectedBmResID != -1 && selectedBmResID != prop.getBld().modelUID) {
					prop.setResourceID(selectedBmResID);
				}
			}
		});

		addRemove.addListener(new PlusMinusButtonSet.PMButtonListener() {
			@Override
			public void plusClicked() {
				if (map != null) {
					ChunkBuilding bld = new ChunkBuilding();
					if (prop != null) {
						bld.modelUID = prop.getBld().modelUID;
						bld.position.y = prop.getPosition().y;
					}
					CMGUI.addToComboBoxAndListWorldObjSimple(new BuildingInstance(bld, area.bmReg), map.buildings, bldComboBox, editors, "Building ");
				}
			}

			@Override
			public void minusClicked() {
				if (map != null) {
					prop.freeRsc();
					CMGUI.removeFromComboBoxAndList(prop, map.buildings, bldComboBox);
				}
			}
		});
		
		bmPreview.bindSelectPanel(bmSelect);
	}

	public void setProp(int index) {
		ComponentUtils.setSelectedIndexSafe(bldComboBox, index);
	}

	private void showProp(int index) {
		if (index != -1) {
			if (map != null) {
				prop = map.buildings.get(index);

				tx.setValue(prop.getPosition().x);
				ty.setValue(prop.getPosition().y);
				tz.setValue(prop.getPosition().z);

				ry.setValue(prop.getBld().rotation);

				bmSelect.setSelectedBuilding(prop.getBldRes());
			}
		} else {
			prop = null;
			tx.setValue(0f);
			ty.setValue(0f);
			tz.setValue(0f);
			ry.setValue(0f);
			bmSelect.setSelectedBuilding(null);
		}
		tool.setSelectedObject(prop);
	}

	public void saveProp() {
		if (this.prop != null) {
			ChunkBuilding b = prop.getBld();

			b.position.x = (Float) tx.getValue();
			b.position.y = (Float) ty.getValue();
			b.position.z = (Float) tz.getValue();

			b.rotation = (Float) ry.getValue();
		}
	}

	@Override
	public boolean store(boolean dialog) {
		if (map != null) {
			saveProp();
			loadBldArr(false);
			return CMGUI.commonSaveDataSequence(editors.getCTRMap(), hash, dialog, "Prop placement", false, (() -> {
				ChunkBuildings infoComb = new ChunkBuildings();
				for (BuildingInstance bld : map.buildings) {
					infoComb.buildings.add(bld.getBld());
				}
				infoComb.write(map);
			}));
		}
		return true;
	}

	public void refreshNoSave() {
		showProp(bldComboBox.getSelectedIndex());
	}

	@Override
	public void loadMapMatrix(VMap chunks) {
		loaded = false;
		map = chunks;
		prop = null;

		bldComboBox.removeAllItems();
		if (map != null) {
			int i = 0;
			for (BuildingInstance bld : chunks.buildings) {
				bldComboBox.addItem("Building " + i);
				i++;
			}
			loadBldArr(true);
		}

		tool.fullyRebuildScene();

		loaded = true;

		setProp(0);
	}

	private void loadBldArr(boolean resetHash) {
		hashArr.clear();
		if (map != null) {
			for (BuildingInstance bi : map.buildings) {
				hashArr.add(bi.getBld());
			}
			if (resetHash) {
				hash.reset();
			}
		}
	}

	@Override
	public void loadArea(VArea a) {
		this.area = a;
		if (a != null && a.bmReg != null) {
			bmSelect.loadBuildingList(a.bmReg.buildings);
			bmPreview.loadBuildingList(a.bmReg, a.propTextures);
		} else {
			bmSelect.loadBuildingList(new ListenableList<>());
			bmPreview.loadBuildingList(null, null);
		}
	}

	@Override
	public List<AbstractTool> getTools() {
		return ArraysEx.asList(tool);
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bldComboBox = new javax.swing.JComboBox<>();
        mainSep = new javax.swing.JSeparator();
        btnSave = new javax.swing.JButton();
        locLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tx = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        ty = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        tz = new javax.swing.JFormattedTextField();
        rotLabel = new javax.swing.JLabel();
        rotYLabel = new javax.swing.JLabel();
        ry = new javax.swing.JFormattedTextField();
        locRotSeparator = new javax.swing.JSeparator();
        addRemove = new xstandard.gui.components.PlusMinusButtonSet();
        previewSep = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        bmSelect = new ctrmap.editor.gui.editors.gen5.level.building.VBuildingSelectJList();
        bmPreview = new ctrmap.editor.gui.editors.gen5.level.building.VBuildingPreviewPanel();
        btnLaunchPRE = new javax.swing.JButton();

        bldComboBox.setMaximumRowCount(25);
        bldComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bldComboBoxActionPerformed(evt);
            }
        });

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        locLabel.setText("Location");

        jLabel2.setForeground(new java.awt.Color(255, 0, 0));
        jLabel2.setText("X:");

        tx.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        jLabel3.setForeground(new java.awt.Color(0, 153, 0));
        jLabel3.setText("Y:");

        ty.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        jLabel4.setForeground(new java.awt.Color(0, 0, 204));
        jLabel4.setText("Z:");

        tz.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        rotLabel.setText("Rotation");

        rotYLabel.setForeground(new java.awt.Color(0, 153, 0));
        rotYLabel.setText("Y:");

        ry.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        locRotSeparator.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jScrollPane1.setViewportView(bmSelect);

        javax.swing.GroupLayout bmPreviewLayout = new javax.swing.GroupLayout(bmPreview);
        bmPreview.setLayout(bmPreviewLayout);
        bmPreviewLayout.setHorizontalGroup(
            bmPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 246, Short.MAX_VALUE)
        );
        bmPreviewLayout.setVerticalGroup(
            bmPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        btnLaunchPRE.setText("Edit resource bundle");
        btnLaunchPRE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLaunchPREActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainSep)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(bldComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addRemove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(previewSep)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnSave))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(locLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tx, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tz, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ty, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addComponent(locRotSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rotLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(rotYLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ry, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                            .addComponent(btnLaunchPRE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bmPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bldComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLaunchPRE))
                    .addComponent(bmPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewSep, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(locLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(tx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(ty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(tz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(locRotSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rotLabel)
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rotYLabel))
                        .addGap(26, 26, 26)))
                .addComponent(btnSave)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bldComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bldComboBoxActionPerformed
		if (this.loaded) {
			saveProp();
			showProp(bldComboBox.getSelectedIndex());
		}
    }//GEN-LAST:event_bldComboBoxActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		saveProp();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnLaunchPREActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLaunchPREActionPerformed
		if (loaded && area != null) {
			VPropRegistryEditor pre = new VPropRegistryEditor();
			pre.load(area.bmReg, area.propTextures);
			pre.setSelectedBm(bmSelect.getSelectedBuildingUID());
			pre.setLocationRelativeTo(editors.getCTRMap());
			pre.setVisible(true);
		}
    }//GEN-LAST:event_btnLaunchPREActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private xstandard.gui.components.PlusMinusButtonSet addRemove;
    private javax.swing.JComboBox<String> bldComboBox;
    private ctrmap.editor.gui.editors.gen5.level.building.VBuildingPreviewPanel bmPreview;
    private ctrmap.editor.gui.editors.gen5.level.building.VBuildingSelectJList bmSelect;
    private javax.swing.JButton btnLaunchPRE;
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel locLabel;
    private javax.swing.JSeparator locRotSeparator;
    private javax.swing.JSeparator mainSep;
    private javax.swing.JSeparator previewSep;
    private javax.swing.JLabel rotLabel;
    private javax.swing.JLabel rotYLabel;
    private javax.swing.JFormattedTextField ry;
    private javax.swing.JFormattedTextField tx;
    private javax.swing.JFormattedTextField ty;
    private javax.swing.JFormattedTextField tz;
    // End of variables declaration//GEN-END:variables
}
