package ctrmap.editor.gui.editors.gen5.level.building;

import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.creativestudio.ngcs.io.CSG3DIOContentType;
import ctrmap.creativestudio.ngcs.io.G3DIO;
import ctrmap.creativestudio.ngcs.rtldr.NGCSIOManager;
import ctrmap.formats.generic.collada.DAE;
import ctrmap.formats.generic.interchange.AnimeUtil;
import ctrmap.formats.generic.source.SMD;
import ctrmap.formats.ntr.nitrowriter.nsbtx.NSBTXWriter;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildingResource;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildings;
import ctrmap.missioncontrol_ntr.field.map.BuildingTexturePack;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.gui.ComboSelectDialog;
import xstandard.gui.DialogUtils;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.PlusMinusButtonSet;
import xstandard.gui.components.listeners.AbstractToggleableListener;
import xstandard.gui.components.listeners.ToggleableActionListener;
import xstandard.gui.components.listeners.ToggleableChangeListener;
import xstandard.gui.file.XFileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;

public class VPropRegistryEditor extends javax.swing.JFrame {

	private final DefaultListModel<String> anmListModel = new DefaultListModel<>();

	private AreaBuildings ab;
	private BuildingTexturePack bmTextures;

	private final ToggleableChangeListener doorPosChgListener = new ToggleableChangeListener() {
		@Override
		public void onApprovedStateChange(ChangeEvent e) {
			AreaBuildingResource r = getSelectedBm();
			if (r != null) {
				r.doorX = (Short) doorX.getValue();
				r.doorY = (Short) doorY.getValue();
				r.doorZ = (Short) doorZ.getValue();
			}
		}
	};

	private final ToggleableChangeListener uidChgListener = new ToggleableChangeListener() {
		@Override
		public void onApprovedStateChange(ChangeEvent e) {
			AreaBuildingResource r = getSelectedBm();
			if (r != null) {
				int newUID = (Integer) bmUID.getValue();
				if (ab.getResourceByUniqueID(newUID) != null) {
					DialogUtils.showErrorMessage(VPropRegistryEditor.this, "UID not unique", "This UID is already in use!");
					setAllowEvents(false);
					bmUID.setValue(r.uid);
					setAllowEvents(true);
				} else {
					r.setUID((Integer) bmUID.getValue());
				}
			}
		}
	};

	private final ToggleableChangeListener bmTypeChgListener = new ToggleableChangeListener() {
		@Override
		public void onApprovedStateChange(ChangeEvent e) {
			AreaBuildingResource r = getSelectedBm();
			if (r != null) {
				r.type = (Integer) bmType.getValue();
			}
		}
	};

	private final ToggleableActionListener anmCntTypeListener = new ToggleableActionListener() {
		@Override
		public void actionPerformedImpl(ActionEvent e) {
			AreaBuildingResource r = getSelectedBm();
			if (r != null) {
				r.setAnmCntType(AreaBuildingResource.ABAnimCntType.VALUES[bmAnmCnt.getSelectedIndex()]);
			}
		}
	};

	private final ToggleableActionListener anmSetLayoutListener = new ToggleableActionListener() {
		@Override
		public void actionPerformedImpl(ActionEvent e) {
			AreaBuildingResource r = getSelectedBm();
			if (r != null) {
				switch (AnmSetLayout.values()[anmSetMode.getSelectedIndex()]) {
					case _1XN:
						r.condenseAnimations();
						r.anmSetEntryCount = r.getPresentAnimationCount();
						break;
					case _NX1:
						r.condenseAnimations();
						r.anmSetEntryCount = 1;
						break;
					case _2X1:
						r.condenseAnimations();
						r.anmSetEntryCount = 1;
						break;
					case _2X2:
						r.spreadAnimations2x2();
						r.anmSetEntryCount = 2;
						break;
				}
				loadAnmSet();
			}
		}
	};

	/**
	 * Creates new form VPropRegistryEditor
	 */
	public VPropRegistryEditor() {
		initComponents();
		anmList.setModel(anmListModel);

		mainBMList.addBuildingSelectionListener(((selectedBmResID) -> {
			loadBuilding();
		}));

		bmPreview.bindSelectPanel(mainBMList);

		bmPreview.getScene().addSceneAnimationCallback((frameAdvance) -> {
			bmAnmControl.update();
		});

		doorBMList.addBuildingSelectionListener(new VBuildingSelectJList.BuildingSelectionListener() {
			@Override
			public void onBuildingRscSelected(int selectedBmResID) {
				AreaBuildingResource r = getSelectedBm();
				if (r != null) {
					r.setDoorUID((short) doorBMList.getSelectedBuildingUID());
				}
			}
		});

		btnAddRemoveBM.addListener(new PlusMinusButtonSet.PMButtonListener() {
			@Override
			public void plusClicked() {
				if (ab != null) {
					NewBMDialog dlg = new NewBMDialog(ab, bmTextures, VPropRegistryEditor.this, true);
					dlg.setVisible(true);
					AreaBuildingResource r = dlg.getResult();
					if (r != null) {
						mainBMList.setSelectedBuilding(r);
					}
				}
			}

			@Override
			public void minusClicked() {
				AreaBuildingResource r = getSelectedBm();
				if (r != null) {
					if (DialogUtils.showYesNoWarningDialog(VPropRegistryEditor.this, "Are you sure?", "This resource will be permanently removed. Prepare for unforeseen consequences.")) {
						int idx = mainBMList.getSelectedIndex();
						ab.buildings.remove(r);
						ComponentUtils.setSelectedIndexSafe(mainBMList, idx);
						r.sendDiscardToListeners(0);

						//Remove unused textures
						for (Material mat : r.materials()) {
							for (TextureMapper m : mat.textures) {
								Texture tex = Scene.getNamedObject(m.textureName, bmTextures.textures);
								if (tex != null) {
									boolean used = false;
									for (G3DResource bm2 : ab.buildings) {
										if (bm2.isTextureUsed(m.textureName)) {
											used = true;
											break;
										}
									}
									if (!used) {
										bmTextures.textures.remove(tex);
									}
								}
							}
						}
					}
				}
			}
		});

		bmUID.addChangeListener(uidChgListener);
		bmType.addChangeListener(bmTypeChgListener);
		bmAnmCnt.addActionListener(anmCntTypeListener);
		anmSetMode.addActionListener(anmSetLayoutListener);

		ComponentUtils.addChangeListener(doorPosChgListener, doorX, doorY, doorZ);
	}
	
	private int getAnmSetEntryCountForAnmSetCount(int anmSetCount) {
		if (anmSetCount == 0) {
			return 4;
		}
		return 4 / anmSetCount;
	}

	public void load(AreaBuildings ab, BuildingTexturePack textures) {
		this.ab = ab;
		mainBMList.loadBuildingList(ab.buildings);
		doorBMList.loadBuildingList(ab.buildings);
		bmPreview.loadBuildingList(ab, textures);
		this.bmTextures = textures;
	}

	private final AbstractToggleableListener[] listeners = new AbstractToggleableListener[]{
		doorPosChgListener, bmTypeChgListener, uidChgListener, anmCntTypeListener, anmSetLayoutListener
	};

	private void loadBuilding() {
		AreaBuildingResource b = getSelectedBm();

		AbstractToggleableListener.setAllowEventsMulti(false, listeners);
		if (b != null) {
			bmUID.setValue(b.uid);
			bmType.setValue(b.type);
			doorX.setValue(b.doorX);
			doorY.setValue(b.doorY);
			doorZ.setValue(b.doorZ);
			doorBMList.setSelectedBuilding(ab.getResourceByUniqueID(b.doorUID));
			anmSetMode.setSelectedIndex(AnmSetLayout.detect(b).ordinal());
			bmAnmCnt.setSelectedIndex(b.anmCntType.ordinal());
		} else {
			ComponentUtils.clearComponents(bmUID, bmType, doorX, doorY, doorZ);
			bmAnmCnt.setSelectedIndex(0);
			bmPreview.loadResource(null);
			anmSetMode.setSelectedIndex(0);
		}
		buildAnmSetBox();
		loadAnmSet();
		AbstractToggleableListener.setAllowEventsMulti(true, listeners);
	}

	private boolean canRemoveAnmSet() {
		AreaBuildingResource res = getSelectedBm();
		return res != null && res.getAnimationSetCount() > 1;
	}

	private boolean canAddNewAnmSet() {
		AreaBuildingResource res = getSelectedBm();
		if (res != null) {
			int ac = res.getPresentAnimationCount();
			int acps = res.anmSetEntryCount;
			if (acps == 1) {
				return ac < 4;
			} else {
				return ac <= 2;
			}
		}
		return false;
	}

	private boolean canAddNewAnm() {
		AreaBuildingResource ab = getSelectedBm();
		if (ab != null) {
			int asIdx = anmSetBox.getSelectedIndex();
			if (asIdx != -1) {
				if (asIdx == 0) {
					return ((ab.anmSetEntryCount + 1) * ab.getAnimationSetCount()) < 4; //only allow adding to the first set
				} else {
					return ab.getPresentAnimationCountInSet(asIdx) < ab.anmSetEntryCount;
				}
			}
		}
		return false;
	}

	private void registAnmSetDropdown(int index) {
		anmSetBox.addItem("Animation set " + index);
	}

	private void buildAnmSetBox() {
		AreaBuildingResource r = getSelectedBm();
		if (r != null) {
			AnmSetLayout lyt = AnmSetLayout.detect(r);
			int count = lyt.getSetCount(r);
			while (anmSetBox.getItemCount() > count) {
				anmSetBox.removeItemAt(anmSetBox.getItemCount() - 1);
			}
			for (int i = anmSetBox.getItemCount(); i < count; i++) {
				registAnmSetDropdown(i);
			}
		} else {
			anmSetBox.removeAllItems();
		}
	}

	private void loadAnmSet() {
		int asIdx = anmSetBox.getSelectedIndex();
		AreaBuildingResource r = getSelectedBm();
		anmListModel.removeAllElements();
		if (r != null) {
			int asc = anmSetBox.getItemCount();
			if (asIdx >= 0 && asIdx < asc) {
				int elemCount = AnmSetLayout.values()[anmSetMode.getSelectedIndex()].getEntryCount(r);

				for (int i = 0; i < elemCount; i++) {
					String text = "❌";

					AbstractAnimation a = r.getConvAnm(asIdx * r.anmSetEntryCount + i);
					if (a != null) {
						text = a.name;
					}

					anmListModel.addElement(text);
				}
			}
		}
	}

	private AreaBuildingResource getSelectedBm() {
		return mainBMList.getSelectedBuilding();
	}

	public void setSelectedBm(int uid) {
		mainBMList.setSelectedBuilding(ab.getResourceByUniqueID(uid));
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainListSP = new javax.swing.JScrollPane();
        mainBMList = new ctrmap.editor.gui.editors.gen5.level.building.VBuildingSelectJList();
        bmPreview = new ctrmap.editor.gui.editors.gen5.level.building.VBuildingPreviewPanel();
        bmAnmControl = new ctrmap.util.gui.AnimationControl();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        doorBMList = new ctrmap.editor.gui.editors.gen5.level.building.VBuildingSelectJList();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        doorX = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        doorY = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        doorZ = new javax.swing.JSpinner();
        btnUnbindDoor = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        bmUIDLabel = new javax.swing.JLabel();
        bmUID = new javax.swing.JSpinner();
        bmTypeLabel = new javax.swing.JLabel();
        bmType = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        bmAnmCnt = new javax.swing.JComboBox<>();
        anmListSep = new javax.swing.JSeparator();
        anmSetBox = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        anmList = new javax.swing.JList<>();
        btnPlaySelectAnm = new javax.swing.JButton();
        btnStopPlayAnm = new javax.swing.JButton();
        btnImportAnm = new javax.swing.JButton();
        btnOpenAnmCS = new javax.swing.JButton();
        anmSetMode = new javax.swing.JComboBox<>();
        btnClearAnm = new javax.swing.JButton();
        btnAddRemoveBM = new xstandard.gui.components.PlusMinusButtonSet();
        btnOpenMdlCS = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        btnCommitMenu = new javax.swing.JMenu();
        btnCommitAB = new javax.swing.JMenuItem();
        btnCommitBMTex = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AB Editor");

        mainListSP.setViewportView(mainBMList);

        bmPreview.setAnmEnable(false);

        javax.swing.GroupLayout bmPreviewLayout = new javax.swing.GroupLayout(bmPreview);
        bmPreview.setLayout(bmPreviewLayout);
        bmPreviewLayout.setHorizontalGroup(
            bmPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        bmPreviewLayout.setVerticalGroup(
            bmPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("AB Resource"));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Door"));

        jScrollPane2.setViewportView(doorBMList);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Position"));

        jLabel1.setForeground(new java.awt.Color(255, 0, 0));
        jLabel1.setText("X");

        doorX.setModel(new javax.swing.SpinnerNumberModel(Short.valueOf((short)0), Short.valueOf((short)-32768), Short.valueOf((short)32767), Short.valueOf((short)1)));

        jLabel2.setForeground(new java.awt.Color(51, 153, 0));
        jLabel2.setText("Y");

        doorY.setModel(new javax.swing.SpinnerNumberModel(Short.valueOf((short)0), Short.valueOf((short)-32768), Short.valueOf((short)32767), Short.valueOf((short)1)));

        jLabel4.setForeground(new java.awt.Color(0, 0, 255));
        jLabel4.setText("Z");

        doorZ.setModel(new javax.swing.SpinnerNumberModel(Short.valueOf((short)0), Short.valueOf((short)-32768), Short.valueOf((short)32767), Short.valueOf((short)1)));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(doorX, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(doorY, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(doorZ, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(doorX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(doorY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(doorZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnUnbindDoor.setText("Unbind door");
        btnUnbindDoor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUnbindDoorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnUnbindDoor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUnbindDoor)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));

        bmUIDLabel.setText("UID");

        bmUID.setModel(new javax.swing.SpinnerNumberModel(0, 0, 512, 1));

        bmTypeLabel.setText("Type");

        bmType.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bmUIDLabel)
                .addGap(24, 24, 24)
                .addComponent(bmUID, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(bmTypeLabel)
                .addGap(18, 18, 18)
                .addComponent(bmType, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(bmTypeLabel)
                        .addComponent(bmType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(bmUIDLabel)
                        .addComponent(bmUID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Animations"));

        jLabel3.setText("Animation controller");

        bmAnmCnt.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Not animated", "Ambient generic", "Dynamic", "Ambient RTC" }));

        anmSetBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anmSetBoxActionPerformed(evt);
            }
        });

        anmList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(anmList);

        btnPlaySelectAnm.setForeground(new java.awt.Color(51, 204, 0));
        btnPlaySelectAnm.setText("▶");
        btnPlaySelectAnm.setToolTipText("Play");
        btnPlaySelectAnm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlaySelectAnmActionPerformed(evt);
            }
        });

        btnStopPlayAnm.setForeground(new java.awt.Color(255, 0, 0));
        btnStopPlayAnm.setText("⬛");
        btnStopPlayAnm.setToolTipText("Stop");
        btnStopPlayAnm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopPlayAnmActionPerformed(evt);
            }
        });

        btnImportAnm.setForeground(new java.awt.Color(0, 204, 255));
        btnImportAnm.setText("▼");
        btnImportAnm.setToolTipText("Import");
        btnImportAnm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportAnmActionPerformed(evt);
            }
        });

        btnOpenAnmCS.setText("Open in CS");
        btnOpenAnmCS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenAnmCSActionPerformed(evt);
            }
        });

        anmSetMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1 set / 4 animations", "2 sets / 1 + 1 animation", "2 sets / 2 + 2 animations", "4 sets / 1 + 1 + 1 + 1 animation" }));

        btnClearAnm.setText("❌");
        btnClearAnm.setToolTipText("Import");
        btnClearAnm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearAnmActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addComponent(anmListSep)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btnImportAnm)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClearAnm)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOpenAnmCS)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addComponent(btnPlaySelectAnm)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnStopPlayAnm))
                    .addComponent(anmSetBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bmAnmCnt, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(anmSetMode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(bmAnmCnt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(anmListSep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(anmSetMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(anmSetBox, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPlaySelectAnm)
                    .addComponent(btnStopPlayAnm)
                    .addComponent(btnImportAnm)
                    .addComponent(btnOpenAnmCS)
                    .addComponent(btnClearAnm))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnOpenMdlCS.setText("Open in CS");
        btnOpenMdlCS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenMdlCSActionPerformed(evt);
            }
        });

        btnCommitMenu.setText("Commit changes");

        btnCommitAB.setText("Resource bundle");
        btnCommitAB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCommitABActionPerformed(evt);
            }
        });
        btnCommitMenu.add(btnCommitAB);

        btnCommitBMTex.setText("Static texture pack");
        btnCommitBMTex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCommitBMTexActionPerformed(evt);
            }
        });
        btnCommitMenu.add(btnCommitBMTex);

        menuBar.add(btnCommitMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mainListSP, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAddRemoveBM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnOpenMdlCS)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bmPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bmAnmControl, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(bmPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bmAnmControl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mainListSP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAddRemoveBM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnOpenMdlCS, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void anmSetBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anmSetBoxActionPerformed
		loadAnmSet();
    }//GEN-LAST:event_anmSetBoxActionPerformed

    private void btnStopPlayAnmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopPlayAnmActionPerformed
		stopAnmPreview();
    }//GEN-LAST:event_btnStopPlayAnmActionPerformed

    private void btnPlaySelectAnmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlaySelectAnmActionPerformed
		AreaBuildingResource r = getSelectedBm();
		if (r != null) {
			int anmSet = anmSetBox.getSelectedIndex();
			int anmIdx = anmList.getSelectedIndex();
			if (anmSet != -1 && anmIdx != -1) {
				AbstractAnimation a = r.getConvAnm(anmSet * r.anmSetEntryCount + anmIdx);
				bmAnmControl.changeController(bmPreview.loadAnime(a));
			}
		}
    }//GEN-LAST:event_btnPlaySelectAnmActionPerformed

    private void btnUnbindDoorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUnbindDoorActionPerformed
		AreaBuildingResource r = getSelectedBm();
		if (r != null) {
			r.setDoorUID(-1);
			doorBMList.clearSelection();
		}
    }//GEN-LAST:event_btnUnbindDoorActionPerformed

    private void btnOpenMdlCSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenMdlCSActionPerformed
		if (ab != null) {
			G3DResource combRsc = new G3DResource();

			final AreaBuildingResource bmRsc = getSelectedBm();
			if (bmRsc != null) {
				combRsc.merge(bmRsc);
			}
			combRsc.merge(bmTextures);
			NGCS ngcs = new NGCS(combRsc, (cs) -> {
				bmTextures.textures.clear();
				bmTextures.addTextures(cs.getTextures());
				List<Model> ngcsModels = cs.getModels();
				if (!ngcsModels.isEmpty() && bmRsc != null) {
					System.out.println("repl model");
					bmRsc.models.clear();
					bmRsc.models.add(ngcsModels.get(0));
					System.out.println(ngcsModels.get(0).meshes.size());
				}
				return true;
			}, true);

			ngcs.setVisible(true);
			ngcs.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					int idx = ab.buildings.indexOf(bmRsc);
					if (idx != -1) {
						ab.buildings.setModify(ab.buildings.indexOf(bmRsc), bmRsc);
						if (bmRsc != null) {
							if (DialogUtils.showYesNoDialog(VPropRegistryEditor.this, "Commit changes?", "Would you like to convert the CreativeStudio model back into the resource bundle?")) {
								System.out.println("conv model");
								G3DResource cvtSrc = new G3DResource();
								cvtSrc.merge(bmRsc);
								cvtSrc.merge(bmTextures); //only for correct material linking
								try {
									byte[] model = BMG3DIO.convertModel(VPropRegistryEditor.this, cvtSrc);
									ab.queueModel(bmRsc, model);
								} catch (Exception ex) {
									DialogUtils.showExceptionTraceDialog(ex);
								}
							}
						}
					}
				}
			});
		}
    }//GEN-LAST:event_btnOpenMdlCSActionPerformed

    private void btnCommitABActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitABActionPerformed
		if (ab != null) {
			ab.write();
		}
    }//GEN-LAST:event_btnCommitABActionPerformed

    private void btnCommitBMTexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitBMTexActionPerformed
		if (bmTextures != null) {
			NSBTXWriter w = new NSBTXWriter(bmTextures.textures, null);
			bmTextures.setBaseBytes(w.writeToMemory());
		}
    }//GEN-LAST:event_btnCommitBMTexActionPerformed


    private void btnOpenAnmCSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenAnmCSActionPerformed
		int anmIdx = getSelectedImExAnmIdx();
		if (anmIdx != -1) {
			final AreaBuildingResource bmRsc = getSelectedBm();
			AbstractAnimation a = bmRsc.getConvAnm(anmIdx);
			if (a != null) {
				G3DResource combRsc = new G3DResource();

				combRsc.merge(getSelectedBm());
				combRsc.merge(bmTextures);
				combRsc.addAnime(a);

				NGCS ngcs = new NGCS(combRsc, (cs) -> {
					bmTextures.textures.clear();
					bmTextures.addTextures(cs.getTextures());
					List<Model> ngcsModels = cs.getModels();
					if (!ngcsModels.isEmpty()) {
						bmRsc.models.clear();
						bmRsc.models.add(ngcsModels.get(0));
					}
					List<AbstractAnimation> ngcsAnimations = cs.getAllAnimations();
					if (!ngcsAnimations.isEmpty()) {
						AbstractAnimation src = ngcsAnimations.get(anmIdx);
						if (src != a) {
							Skeleton skl = BMG3DIO.getBmRscSkel(bmRsc);

							byte[] conv = BMG3DIO.convertNNSAnm(skl, src);
							if (conv != null) {
								bmRsc.setAnm(anmIdx, src, conv);
								reloadAnmSet();
							}
						}
					}
					return true;
				});
				ngcs.setVisible(true);
			}
		}
    }//GEN-LAST:event_btnOpenAnmCSActionPerformed

	private void stopAnmPreview() {
		bmPreview.clearAnime();
		bmAnmControl.changeController(null);
	}

	private void reloadAnmSet() {
		int idx = anmList.getSelectedIndex();
		loadAnmSet();
		anmList.setSelectedIndex(idx);
		stopAnmPreview();
	}

    private void btnImportAnmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportAnmActionPerformed
		int idx = getSelectedImExAnmIdx();
		if (idx != -1) {
			FSFile fsf = XFileDialog.openFileDialog(DAE.EXTENSION_FILTER, SMD.EXTENSION_FILTER, AnimeUtil.SA_EXTENSION_FILTER, AnimeUtil.MA_EXTENSION_FILTER);

			if (fsf != null) {
				System.out.println("file");
				G3DResource res = G3DIO.readFile(fsf, null, NGCSIOManager.getInstance().getFormatHandlers(CSG3DIOContentType.ANIMATION_ANY_RESERVED));

				if (res != null) {
					System.out.println("got res");
					AbstractAnimation src = null;

					List<AbstractAnimation> importAnmList = res.getAnimations();
					System.out.println("anm count " + importAnmList.size());
					switch (importAnmList.size()) {
						case 0:
							break;
						case 1:
							src = importAnmList.get(0);
							break;
						default:
							ComboSelectDialog dlg = new ComboSelectDialog(this, true, "Select one of the animations", importAnmList);
							dlg.setVisible(true);
							src = (AbstractAnimation) dlg.getSelectedUserObj();
							break;
					}

					if (src != null) {
						AreaBuildingResource bm = getSelectedBm();
						byte[] conv = BMG3DIO.convertNNSAnm(BMG3DIO.getBmRscSkel(bm), src);
						if (conv != null) {
							bm.setAnm(idx, src, conv);
							bm.anmSetEntryCount = Math.max(1, bm.anmSetEntryCount);
							reloadAnmSet();
						}
					}
				}
			}
		}
    }//GEN-LAST:event_btnImportAnmActionPerformed

    private void btnClearAnmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearAnmActionPerformed
		int idx = getSelectedImExAnmIdx();
		if (idx != -1) {
			AreaBuildingResource bm = getSelectedBm();
			bm.setAnm(idx, null, null);
			reloadAnmSet();
			if (bm.getPresentAnimationCount() == 0) {
				bm.anmSetEntryCount = 0;
			}
		}
    }//GEN-LAST:event_btnClearAnmActionPerformed

	private int getSelectedImExAnmIdx() {
		AreaBuildingResource r = getSelectedBm();
		if (r != null) {
			int anmSetIdx = anmSetBox.getSelectedIndex();
			if (anmSetIdx != -1) {
				int anmIdx = anmList.getSelectedIndex();
				if (anmIdx != -1) {
					return anmSetIdx * r.anmSetEntryCount + anmIdx;
				}
			}
		}
		return -1;
	}

	private static enum AnmSetLayout {
		_1XN(1, -1),
		_2X1(2, 1),
		_2X2(2, 2),
		_NX1(-1, 1);

		public final int setCount;
		public final int setEntryCount;

		private AnmSetLayout(int setCount, int setEntryCount) {
			this.setCount = setCount;
			this.setEntryCount = setEntryCount;
		}

		public int getSetCount(AreaBuildingResource rsc) {
			if (setCount != -1) {
				return setCount;
			}
			return 4;
		}

		public int getEntryCount(AreaBuildingResource rsc) {
			if (setEntryCount != -1) {
				return setEntryCount;
			}
			return 4;
		}

		public static AnmSetLayout detect(AreaBuildingResource rsc) {
			switch (rsc.anmSetEntryCount) {
				case 2:
					if (rsc.getPresentAnimationCount() > 2) {
						return _2X2;
					}
					break;
				case 1:
					if (rsc.getPresentAnimationCount() == 2) {
						return _2X1;
					} else {
						return _1XN;
					}
				case 0:
					return _1XN;
				default:
					break;
			}
			return _NX1;
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> anmList;
    private javax.swing.JSeparator anmListSep;
    private javax.swing.JComboBox<String> anmSetBox;
    private javax.swing.JComboBox<String> anmSetMode;
    private javax.swing.JComboBox<String> bmAnmCnt;
    private ctrmap.util.gui.AnimationControl bmAnmControl;
    private ctrmap.editor.gui.editors.gen5.level.building.VBuildingPreviewPanel bmPreview;
    private javax.swing.JSpinner bmType;
    private javax.swing.JLabel bmTypeLabel;
    private javax.swing.JSpinner bmUID;
    private javax.swing.JLabel bmUIDLabel;
    private xstandard.gui.components.PlusMinusButtonSet btnAddRemoveBM;
    private javax.swing.JButton btnClearAnm;
    private javax.swing.JMenuItem btnCommitAB;
    private javax.swing.JMenuItem btnCommitBMTex;
    private javax.swing.JMenu btnCommitMenu;
    private javax.swing.JButton btnImportAnm;
    private javax.swing.JButton btnOpenAnmCS;
    private javax.swing.JButton btnOpenMdlCS;
    private javax.swing.JButton btnPlaySelectAnm;
    private javax.swing.JButton btnStopPlayAnm;
    private javax.swing.JButton btnUnbindDoor;
    private ctrmap.editor.gui.editors.gen5.level.building.VBuildingSelectJList doorBMList;
    private javax.swing.JSpinner doorX;
    private javax.swing.JSpinner doorY;
    private javax.swing.JSpinner doorZ;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private ctrmap.editor.gui.editors.gen5.level.building.VBuildingSelectJList mainBMList;
    private javax.swing.JScrollPane mainListSP;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables
}
