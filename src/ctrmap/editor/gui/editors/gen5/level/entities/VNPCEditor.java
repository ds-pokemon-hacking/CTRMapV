package ctrmap.editor.gui.editors.gen5.level.entities;

import ctrmap.editor.gui.editors.gen5.level.VLevelEditor;
import ctrmap.editor.gui.editors.gen5.level.tools.VNPCTool;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import xstandard.gui.DialogUtils;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.PlusMinusButtonSet;
import xstandard.util.ArraysEx;
import ctrmap.util.gui.CMGUI;
import java.awt.Point;
import java.util.List;
import ctrmap.formats.pokemon.gen5.zone.entities.VNPC;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;
import ctrmap.missioncontrol_ntr.field.VFieldController;
import ctrmap.missioncontrol_ntr.field.VPlayerController;
import ctrmap.missioncontrol_ntr.field.debug.VPlayerDebugger;
import ctrmap.missioncontrol_ntr.field.debug.VZoneDebugger;
import ctrmap.missioncontrol_ntr.field.mmodel.VMoveModel;
import ctrmap.missioncontrol_ntr.field.structs.VZone;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.missioncontrol_ntr.field.debug.VFieldDebugger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import xstandard.math.vec.Vec3f;

public class VNPCEditor extends javax.swing.JPanel implements AbstractToolbarEditor, VFieldDebugger, VZoneDebugger, VPlayerDebugger {

	private VNPCTool tool;
	private VFieldController ctrl;
	public VZone zone;
	public VLevelEditor editors;

	private boolean loaded = false;
	public VMoveModel npc;

	public VNPCEditor(VLevelEditor editors) {
		initComponents();
		this.editors = editors;

		tool = new VNPCTool(this);

		ComponentUtils.setNFValueClass(Integer.class, moveCode, bhv, areaH, areaW);
		
		model.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (loaded && npc != null) {
					npc.setModelID((Integer)model.getValue());
				}
			}
		});

		addRemove.addListener(new PlusMinusButtonSet.PMButtonListener() {
			@Override
			public void plusClicked() {
				if (zone != null) {
					int uid = -1;
					for (VMoveModel mmdl : zone.NPCs) {
						int cmpUid = mmdl.NPCData.uid;
						if (cmpUid < 254) {
							uid = Math.max(uid, mmdl.NPCData.uid);
						}
					}
					uid++;
					if (uid >= 254) {
						DialogUtils.showErrorMessage(editors.getCTRMap(), "Too many NPCs.", "Can not allocate more than 254 user NPCs.");
					} else {
						VNPC npcData = new VNPC();
						npcData.uid = uid;
						npcData.objCode = (npc != null) ? npc.getModelUID() : 0;
						VMoveModel n = new VMoveModel(ctrl, npcData);
						n.setWPos(editors.getIdealCenterCameraPosByZeroPlane());
						zone.NPCs.add(n);
						zone.entities.NPCs.add(n.NPCData);
						npcComboBox.addItem(String.valueOf(n.getEntityUID()));
						setNPC(zone.NPCs.size() - 1);
					}
				}
			}

			@Override
			public void minusClicked() {
				if (zone != null && npc != null) {
					zone.entities.NPCs.remove(npc.NPCData);
					CMGUI.removeFromComboBoxAndList(npc, zone.NPCs, npcComboBox);
				}
			}
		});

		locPanel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (npc != null) {
					VRailOrGridLocationPanel.PositionSet ps = locPanel.save();
					npc.NPCData.isPositionRail = ps.isPositionRail;
					npc.NPCData.railLineNo = ps.railLineID;
					npc.NPCData.railFrontPos = ps.railPosFront;
					npc.NPCData.railSidePos = ps.railPosSide;
					int x = ps.gridX;
					int z = ps.gridZ;
					float y = ps.worldY;
					Point nowGPos = npc.getGPos();
					if (y != npc.getAltitude() || x != nowGPos.x || z != nowGPos.y) {
						npc.setLocation(x, z, y);
					}
				}
			}
		});
	}

	public void setNPC(int index) {
		ComponentUtils.setSelectedIndexSafe(npcComboBox, index);
	}

	private void loadLocationToPanel() {
		if (npc != null) {
			VNPC n = npc.NPCData;
			VRailOrGridLocationPanel.PositionSet set = new VRailOrGridLocationPanel.PositionSet(n.isPositionRail, n.gposX, n.wposY, n.gposZ, n.railLineNo, n.railFrontPos, n.railSidePos);
			locPanel.load(set);
		} else {
			locPanel.load(null);
		}
	}

	private void showNPC(int index) {
		if (zone != null && index >= 0 && index < zone.NPCs.size()) {
			npc = zone.NPCs.get(index);

			VNPC n = npc.NPCData;
			model.setValue(n.objCode);
			spawnFlag.setValue(n.spawnFlag);
			scrid.setValue(n.script);

			ori.setSelectedIndex(n.faceDirection);

			moveCode.setValue(n.moveCode);
			bhv.setValue(n.eventType);
			param0.setValue(n.params[0]);
			param1.setValue(n.params[1]);
			param2.setValue(n.params[2]);

			areaW.setValue(n.areaWidth);
			areaH.setValue(n.areaHeight);

			worldLoc.setText(npc.getWPos().toString());
		} else {
			npc = null;
			ComponentUtils.clearComponents(model, spawnFlag, scrid, ori, moveCode, bhv, param0, areaW, areaH, worldLoc);
		}
		loadLocationToPanel();
		tool.setSelectedObject(npc);
	}

	public void saveNPC() {
		if (this.npc != null) {
			VNPC n = this.npc.NPCData;
			npc.setModelID((Integer) model.getValue());
			n.spawnFlag = (Integer) spawnFlag.getValue();
			n.script = (Integer) scrid.getValue();

			//npc.setLocation((Integer) x.getValue(), (Integer) y.getValue(), (Float) alt.getValue());
			npc.setOrientation(ori.getSelectedIndex());

			n.moveCode = (Integer) moveCode.getValue();
			n.eventType = (Integer) bhv.getValue();
			n.params[0] = (Integer) param0.getValue();
			n.params[1] = (Integer) param1.getValue();
			n.params[2] = (Integer) param2.getValue();

			n.areaWidth = (Integer) areaW.getValue();
			n.areaHeight = (Integer) areaH.getValue();

			tool.fullyRebuildScene();
		}
	}

	public void refreshNoSave() {
		showNPC(npcComboBox.getSelectedIndex());
	}

	@Override
	public List<AbstractTool> getTools() {
		return ArraysEx.asList(tool);
	}

	@Override
	public void attachField(VFieldController ctrl) {
		this.ctrl = ctrl;
	}

	@Override
	public void loadZone(VZone z) {
		this.zone = z;

		loaded = false;
		npc = null;
		npcComboBox.removeAllItems();
		if (z != null) {
			for (VMoveModel mmdl : z.NPCs) {
				npcComboBox.addItem(String.valueOf(mmdl.NPCData.uid));
			}
		}
		tool.fullyRebuildScene();
		loaded = true;
		setNPC(0);
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        npcComboBox = new javax.swing.JComboBox<>();
        mainSep = new javax.swing.JSeparator();
        entityPanel = new javax.swing.JPanel();
        modelLabel = new javax.swing.JLabel();
        model = new javax.swing.JSpinner();
        spawnFlagLabel = new javax.swing.JLabel();
        spawnFlag = new javax.swing.JSpinner();
        scridLabel = new javax.swing.JLabel();
        scrid = new javax.swing.JSpinner();
        entSep = new javax.swing.JSeparator();
        locLabel = new javax.swing.JLabel();
        oriLabel = new javax.swing.JLabel();
        ori = new javax.swing.JComboBox<>();
        worldLocLabel = new javax.swing.JLabel();
        worldLoc = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        locPanel = new ctrmap.editor.gui.editors.gen5.level.entities.VRailOrGridLocationPanel();
        actorPanel = new javax.swing.JPanel();
        moveCodeLabel = new javax.swing.JLabel();
        moveCode = new javax.swing.JFormattedTextField();
        bhvLabel = new javax.swing.JLabel();
        bhv = new javax.swing.JFormattedTextField();
        actorSep = new javax.swing.JSeparator();
        areaLabel = new javax.swing.JLabel();
        areaW = new javax.swing.JFormattedTextField();
        areaWLabel = new javax.swing.JLabel();
        areaHLabel = new javax.swing.JLabel();
        areaH = new javax.swing.JFormattedTextField();
        areaDimLabel = new javax.swing.JLabel();
        miscPanel = new javax.swing.JPanel();
        param0Label = new javax.swing.JLabel();
        param0 = new javax.swing.JSpinner();
        param1 = new javax.swing.JSpinner();
        param1Label = new javax.swing.JLabel();
        param2 = new javax.swing.JSpinner();
        param2Label = new javax.swing.JLabel();
        npcParam0Hint = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        addRemove = new xstandard.gui.components.PlusMinusButtonSet();

        npcComboBox.setMaximumRowCount(25);
        npcComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                npcComboBoxActionPerformed(evt);
            }
        });

        entityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Entity"));

        modelLabel.setText("Model No.");

        model.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        spawnFlagLabel.setText("Spawn flag");

        spawnFlag.setModel(new javax.swing.SpinnerNumberModel());

        scridLabel.setText("Script");

        scrid.setModel(new javax.swing.SpinnerNumberModel());

        locLabel.setText("Location");

        oriLabel.setText("Orientation");

        ori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "North", "South", "West", "East" }));

        worldLocLabel.setText("World location:");

        worldLoc.setText("--");

        locPanel.setYIsFloat(true);

        javax.swing.GroupLayout entityPanelLayout = new javax.swing.GroupLayout(entityPanel);
        entityPanel.setLayout(entityPanelLayout);
        entityPanelLayout.setHorizontalGroup(
            entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(entityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(entityPanelLayout.createSequentialGroup()
                        .addGroup(entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addComponent(entSep)
                            .addComponent(locPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(entityPanelLayout.createSequentialGroup()
                        .addGroup(entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(entityPanelLayout.createSequentialGroup()
                                    .addComponent(modelLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(model, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(entityPanelLayout.createSequentialGroup()
                                    .addComponent(spawnFlagLabel)
                                    .addGap(18, 18, 18)
                                    .addComponent(spawnFlag, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(entityPanelLayout.createSequentialGroup()
                                    .addComponent(scridLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(scrid, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(locLabel)
                            .addComponent(oriLabel)
                            .addComponent(ori, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(entityPanelLayout.createSequentialGroup()
                        .addComponent(worldLocLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(worldLoc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        entityPanelLayout.setVerticalGroup(
            entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(entityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modelLabel)
                    .addComponent(model, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spawnFlagLabel)
                    .addComponent(spawnFlag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scridLabel)
                    .addComponent(scrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(entSep, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(worldLocLabel)
                    .addComponent(worldLoc))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(oriLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        actorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Actor"));

        moveCodeLabel.setText("MoveCode");

        moveCode.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        bhvLabel.setText("Behavior");

        bhv.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        areaLabel.setText("Area");

        areaW.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        areaWLabel.setForeground(new java.awt.Color(255, 0, 0));
        areaWLabel.setText("Width");

        areaHLabel.setForeground(new java.awt.Color(0, 153, 0));
        areaHLabel.setText("Height");

        areaH.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        areaDimLabel.setText("Dimensions");

        javax.swing.GroupLayout actorPanelLayout = new javax.swing.GroupLayout(actorPanel);
        actorPanel.setLayout(actorPanelLayout);
        actorPanelLayout.setHorizontalGroup(
            actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(actorSep)
                    .addGroup(actorPanelLayout.createSequentialGroup()
                        .addGroup(actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(areaLabel)
                            .addComponent(areaDimLabel)
                            .addGroup(actorPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(areaWLabel)
                                    .addComponent(areaHLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(areaW, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(areaH, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(actorPanelLayout.createSequentialGroup()
                                .addComponent(moveCodeLabel)
                                .addGap(18, 18, 18)
                                .addComponent(moveCode, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(bhvLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bhv, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        actorPanelLayout.setVerticalGroup(
            actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(bhvLabel)
                        .addComponent(bhv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(moveCodeLabel)
                        .addComponent(moveCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(actorSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(areaLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(actorPanelLayout.createSequentialGroup()
                        .addComponent(areaWLabel)
                        .addGap(29, 29, 29))
                    .addGroup(actorPanelLayout.createSequentialGroup()
                        .addComponent(areaDimLabel)
                        .addGroup(actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(actorPanelLayout.createSequentialGroup()
                                .addGap(35, 35, 35)
                                .addComponent(areaHLabel))
                            .addGroup(actorPanelLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(actorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(actorPanelLayout.createSequentialGroup()
                                        .addComponent(areaW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(26, 26, 26))
                                    .addComponent(areaH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap())
        );

        miscPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("User parameters"));

        param0Label.setText("Param 0");

        param0.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        param1.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        param1Label.setText("Param 1");

        param2.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        param2Label.setText("Param 2");

        npcParam0Hint.setText("(Trainer line of sight, Foongus level...)");

        javax.swing.GroupLayout miscPanelLayout = new javax.swing.GroupLayout(miscPanel);
        miscPanel.setLayout(miscPanelLayout);
        miscPanelLayout.setHorizontalGroup(
            miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(miscPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(miscPanelLayout.createSequentialGroup()
                        .addComponent(param0Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(param0, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(npcParam0Hint))
                    .addGroup(miscPanelLayout.createSequentialGroup()
                        .addComponent(param1Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(param1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(miscPanelLayout.createSequentialGroup()
                        .addComponent(param2Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(param2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        miscPanelLayout.setVerticalGroup(
            miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(miscPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(param0Label)
                    .addComponent(param0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(npcParam0Hint))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(param1Label)
                    .addComponent(param1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(param2Label)
                    .addComponent(param2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
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
                    .addComponent(actorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(miscPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnSave))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(npcComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addRemove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(entityPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(npcComboBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(entityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(actorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(miscPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void npcComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_npcComboBoxActionPerformed
		if (loaded) {
			saveNPC();
			showNPC(npcComboBox.getSelectedIndex());
		}
    }//GEN-LAST:event_npcComboBoxActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		saveNPC();
    }//GEN-LAST:event_btnSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actorPanel;
    private javax.swing.JSeparator actorSep;
    private xstandard.gui.components.PlusMinusButtonSet addRemove;
    private javax.swing.JLabel areaDimLabel;
    private javax.swing.JFormattedTextField areaH;
    private javax.swing.JLabel areaHLabel;
    private javax.swing.JLabel areaLabel;
    private javax.swing.JFormattedTextField areaW;
    private javax.swing.JLabel areaWLabel;
    private javax.swing.JFormattedTextField bhv;
    private javax.swing.JLabel bhvLabel;
    private javax.swing.JButton btnSave;
    private javax.swing.JSeparator entSep;
    private javax.swing.JPanel entityPanel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel locLabel;
    private ctrmap.editor.gui.editors.gen5.level.entities.VRailOrGridLocationPanel locPanel;
    private javax.swing.JSeparator mainSep;
    private javax.swing.JPanel miscPanel;
    private javax.swing.JSpinner model;
    private javax.swing.JLabel modelLabel;
    private javax.swing.JFormattedTextField moveCode;
    private javax.swing.JLabel moveCodeLabel;
    private javax.swing.JComboBox<String> npcComboBox;
    private javax.swing.JLabel npcParam0Hint;
    private javax.swing.JComboBox<String> ori;
    private javax.swing.JLabel oriLabel;
    private javax.swing.JSpinner param0;
    private javax.swing.JLabel param0Label;
    private javax.swing.JSpinner param1;
    private javax.swing.JLabel param1Label;
    private javax.swing.JSpinner param2;
    private javax.swing.JLabel param2Label;
    private javax.swing.JSpinner scrid;
    private javax.swing.JLabel scridLabel;
    private javax.swing.JSpinner spawnFlag;
    private javax.swing.JLabel spawnFlagLabel;
    private javax.swing.JLabel worldLoc;
    private javax.swing.JLabel worldLocLabel;
    // End of variables declaration//GEN-END:variables

	private VPlayerController plr;

	@Override
	public void bindPlayerController(VPlayerController ctrl) {
		this.plr = ctrl;
	}

	@Override
	public void update() {

	}

	@Override
	public void onPlayerMove() {
		if (npc != null && npc.getEntityUID() == VFieldConstants.PLAYERCTRL_MMDL_UID) {
			loadLocationToPanel();
			Vec3f wpos = npc.getWPos();
			worldLoc.setText(wpos.toString());
		}
	}

	@Override
	public boolean getIsWalkThroughWallsEnabled() {
		return false;
	}
}
