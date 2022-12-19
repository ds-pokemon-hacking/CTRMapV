package ctrmap.util.tools.ovl;

import ctrmap.CTRMapVResources;
import rpm.util.AutoRelGenerator;
import ctrmap.editor.system.workspace.GameDetector;
import ctrmap.editor.system.workspace.wildcards.FSWildCardManagerNTR;
import rpm.elfconv.ELF2RPM;
import rpm.elfconv.ExternalSymbolDB;
import rpm.format.rpm.*;
import xstandard.formats.yaml.Yaml;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.DnDHelper;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.file.XFileDialog;
import xstandard.gui.file.CommonExtensionFilters;
import java.io.File;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import xstandard.fs.FSFile;
import xstandard.gui.DialogUtils;
import xstandard.io.base.impl.ext.data.DataIOStream;
import ctrmap.util.DirectFSManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import rpm.cli.RPMTool;

public class CodeInjectionSystemGUI extends javax.swing.JFrame {

	public static final String CISYS_OVLINJECT_RSC = "codeinjection/OvlLoader.rpm";
	public static final String CISYS_OVLINJECT_NAME = "OvlLoader";
	public static final String CISYS_OVLINJECT_HEAPSTART_SYMNAME = "ResizeMemoryForOvl344";

	public static final String CISYS_ARM9_DECMPOFF_RSC = "codeinjection/arm9_decmp_off_HK_REL.rpm";

	private OvlPatchSystem psys;

	private CodeInjectionSystem ciSys;

	private DefaultListModel<String> patchModel = new DefaultListModel<>();

	public CodeInjectionSystemGUI() {
		initComponents();
		patchList.setModel(patchModel);

		patchList.addListSelectionListener((e) -> {
			loadPatch();
		});

		setLocationRelativeTo(null);

		DnDHelper.addFileDropTarget(getRootPane(), new DnDHelper.FileDropListener() {
			@Override
			public void acceptDrop(List<File> files) {
				if (!files.isEmpty()) {
					loadGameDirectoryImpl(new DiskFile(files.get(0)));
				}
			}
		});
	}

	private void loadPSys() {
		patchModel.removeAllElements();
		if (psys != null) {
			for (OvlCodeEntry e : psys.codeInfo) {
				patchModel.addElement(e.name);
			}
		}
		loadPatch();
	}

	private OvlCodeEntry getSelectedPatch() {
		if (psys != null) {
			int idx = patchList.getSelectedIndex();
			if (idx != -1) {
				return psys.codeInfo.get(idx);
			}
		}
		return null;
	}

	private void loadPatch() {
		OvlCodeEntry p = getSelectedPatch();
		if (p != null) {
			selPatch.setText(p.name);
			selPatchSize.setText("0x" + Integer.toHexString(p.length));
			baseOfs.setText("0x" + Integer.toHexString(p.offset + psys.getBaseOffset()));

			//ComponentUtils.setComponentsEnabled(!p.readOnly, btnImportHooks, btnRemovePatch);
		} else {
			selPatch.setText("-");
			selPatchSize.setText("-");
			baseOfs.setText("-");
			ComponentUtils.setComponentsEnabled(true, btnImportHooks, btnRemovePatch);
		}
	}

	private void loadGameDirectoryImpl(FSFile f) {
		if (f != null && f.exists() && f.isDirectory()) {
			FSFile esdbFile = XFileDialog.openFileDialog("Open an ESDB symbol database", Yaml.EXTENSION_FILTER);

			if (esdbFile != null) {
				NTRGameFS fs = new NTRGameFS(new DirectFSManager(f, FSWildCardManagerNTR.INSTANCE), GameDetector.createGameInfo(f));
				ExternalSymbolDB esdb = new ExternalSymbolDB(esdbFile);
				boolean isNew = !CodeInjectionSystem.isCISInitialized(fs);
				ciSys = new CodeInjectionSystem(fs, esdb);
				if (isNew) {
					tryInstallDefaultOvlLoader();
				}
				psys = ciSys.getPSys();
				loadPSys();
			}
		}
	}

	private void tryInstallDefaultOvlLoader() {
		if (DialogUtils.showYesNoDialog(this, "Install overlay loader?", "Would you like to install the built-in overlay loader?")) {
			queueBasePatchesToNewCIS(ciSys);
			save();
		}
	}

	private void updateBuiltinLoaderOvlSize() {
		if (ciSys != null && psys != null) {
			OvlCodeEntry entry = psys.getPatchByName(CISYS_OVLINJECT_NAME);

			if (entry != null) {
				if (!setOverlaySizeToOvlLoader(entry.getFullRPM(), ciSys.getMaxOvlAddr())) {
					DialogUtils.showErrorMessage(this, "Error", "Setting the overlay size failed.");
				}
			}
		}
	}

	private void queueBasePatchesToNewCIS(CodeInjectionSystem cisys) {
		RPM ovlLoaderRPM = new RPM(CTRMapVResources.ACCESSOR.getByteArray(CISYS_OVLINJECT_RSC));
		RPM arm9UndecompRPM = new RPM(CTRMapVResources.ACCESSOR.getByteArray(CISYS_ARM9_DECMPOFF_RSC));

		setOverlaySizeToOvlLoader(ovlLoaderRPM, cisys.getMaxOvlAddr());

		psys.queueRPMFilePatch(ovlLoaderRPM, CISYS_OVLINJECT_NAME).readOnly = true;
		psys.queueRPMFilePatch(arm9UndecompRPM, "ARM9-NoDecompress").readOnly = true;
	}

	private boolean setOverlaySizeToOvlLoader(RPM ovlLoaderRPM, int maxOvlAddr) {
		RPMSymbol sym = ovlLoaderRPM.getSymbol(CISYS_OVLINJECT_HEAPSTART_SYMNAME);
		if (sym != null) {
			try {
				System.out.println("Setting heap size " + Integer.toHexString(maxOvlAddr));
				DataIOStream code = ovlLoaderRPM.getCodeStream();
				code.seek(sym.address);
				code.writeInt(maxOvlAddr);
				return true;
			} catch (IOException ex) {
				Logger.getLogger(CodeInjectionSystemGUI.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			System.err.println("Could not find CISYS_OVLINJECT_HEAPSTART_SYMNAME! Expect undefined behavior.");
		}
		return false;
	}

	private void save() {
		if (ciSys != null) {
			updateBuiltinLoaderOvlSize();
			ciSys.savePSysData();
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        patchListSP = new javax.swing.JScrollPane();
        patchList = new javax.swing.JList<>();
        selPatchLabel = new javax.swing.JLabel();
        selPatch = new javax.swing.JLabel();
        selPatchSizeLabel = new javax.swing.JLabel();
        selPatchSize = new javax.swing.JLabel();
        baseOfsLabel = new javax.swing.JLabel();
        baseOfs = new javax.swing.JLabel();
        btnExportSymMap = new javax.swing.JButton();
        btnAddPatch = new javax.swing.JButton();
        btnRemovePatch = new javax.swing.JButton();
        btnExportHooks = new javax.swing.JButton();
        btnImportHooks = new javax.swing.JButton();
        btnReapplyHooks = new javax.swing.JButton();
        btnExportRpm = new javax.swing.JButton();
        btnStripSymbolNames = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        controlSep = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        btnOpenGameDir = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        btnELF2RPM = new javax.swing.JMenuItem();
        btnMultiRpmConv = new javax.swing.JMenuItem();
        btnRPM2DLL = new javax.swing.JMenuItem();
        btnBranchCalc = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("OvlPatchSystemGUI");

        patchList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        patchListSP.setViewportView(patchList);

        selPatchLabel.setText("Selected patch:");

        selPatch.setText("-");

        selPatchSizeLabel.setText("Size:");

        selPatchSize.setText("-");

        baseOfsLabel.setText("Base offset");

        baseOfs.setText("-");

        btnExportSymMap.setText("Export");
        btnExportSymMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportSymMapActionPerformed(evt);
            }
        });

        btnAddPatch.setText("+");
        btnAddPatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPatchActionPerformed(evt);
            }
        });

        btnRemovePatch.setText("-");
        btnRemovePatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemovePatchActionPerformed(evt);
            }
        });

        btnExportHooks.setText("Export");
        btnExportHooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportHooksActionPerformed(evt);
            }
        });

        btnImportHooks.setText("Import");
        btnImportHooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportHooksActionPerformed(evt);
            }
        });

        btnReapplyHooks.setText("Re-apply");
        btnReapplyHooks.setMargin(new java.awt.Insets(2, 5, 2, 5));
        btnReapplyHooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReapplyHooksActionPerformed(evt);
            }
        });

        btnExportRpm.setText("Export");
        btnExportRpm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportRpmActionPerformed(evt);
            }
        });

        btnStripSymbolNames.setText("Strip names");
        btnStripSymbolNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStripSymbolNamesActionPerformed(evt);
            }
        });

        jLabel1.setText("Relocations:");

        jLabel2.setText("Symbols:");

        fileMenu.setText("File");

        btnOpenGameDir.setText("Open game directory");
        btnOpenGameDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenGameDirActionPerformed(evt);
            }
        });
        fileMenu.add(btnOpenGameDir);

        menuBar.add(fileMenu);

        toolsMenu.setText("Tools");

        btnELF2RPM.setText("Convert ELF to RPM");
        btnELF2RPM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnELF2RPMActionPerformed(evt);
            }
        });
        toolsMenu.add(btnELF2RPM);

        btnMultiRpmConv.setText("MultiRPM Converter");
        btnMultiRpmConv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMultiRpmConvActionPerformed(evt);
            }
        });
        toolsMenu.add(btnMultiRpmConv);

        btnRPM2DLL.setText("Convert RPM to DLL");
        btnRPM2DLL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRPM2DLLActionPerformed(evt);
            }
        });
        toolsMenu.add(btnRPM2DLL);

        btnBranchCalc.setText("Branch calculator");
        btnBranchCalc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBranchCalcActionPerformed(evt);
            }
        });
        toolsMenu.add(btnBranchCalc);

        menuBar.add(toolsMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAddPatch, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemovePatch, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnExportRpm))
                    .addComponent(patchListSP, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnImportHooks, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnExportHooks, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(controlSep, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(selPatchSize)
                                    .addComponent(baseOfs)
                                    .addComponent(selPatch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(selPatchLabel)
                                    .addComponent(selPatchSizeLabel)
                                    .addComponent(baseOfsLabel)
                                    .addComponent(jLabel2)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btnExportSymMap, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnStripSymbolNames))
                                    .addComponent(btnReapplyHooks, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 42, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(patchListSP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddPatch)
                            .addComponent(btnRemovePatch)
                            .addComponent(btnExportRpm)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selPatchLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selPatch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selPatchSizeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selPatchSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(baseOfsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(baseOfs)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(controlSep, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnExportSymMap)
                            .addComponent(btnStripSymbolNames))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnImportHooks)
                            .addComponent(btnExportHooks))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReapplyHooks)
                        .addGap(41, 41, 41)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOpenGameDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenGameDirActionPerformed
		FSFile f = XFileDialog.openDirectoryDialog();

		loadGameDirectoryImpl(f);
    }//GEN-LAST:event_btnOpenGameDirActionPerformed

    private void btnAddPatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPatchActionPerformed
		if (ciSys != null) {
			FSFile patchFile = XFileDialog.openFileDialog(RPM.EXTENSION_FILTER);

			if (patchFile != null) {
				ciSys.queueFilePatch(patchFile);
				save();

				loadPSys();
				patchList.setSelectedIndex(psys.codeInfo.size() - 1);
			}
		}
    }//GEN-LAST:event_btnAddPatchActionPerformed

    private void btnRemovePatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemovePatchActionPerformed
		if (psys != null) {
			psys.removePatch(getSelectedPatch());
			save();
			loadPSys();
		}
    }//GEN-LAST:event_btnRemovePatchActionPerformed

    private void btnExportSymMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportSymMapActionPerformed
		OvlCodeEntry p = getSelectedPatch();

		if (p != null) {
			FSFile targetFile = XFileDialog.openSaveFileDialog(CommonExtensionFilters.LINKER_MAP);

			if (targetFile != null) {
				p.getFullRPM().writeMAPToFile(targetFile);
			}
		}
    }//GEN-LAST:event_btnExportSymMapActionPerformed

    private void btnExportHooksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportHooksActionPerformed
		OvlCodeEntry p = getSelectedPatch();

		if (p != null) {
			FSFile targetFile = XFileDialog.openSaveFileDialog(Yaml.EXTENSION_FILTER);

			if (targetFile != null) {
				RPMTool.writeRelocationsAsYml(p.getFullRPM(), targetFile);
			}
		}
    }//GEN-LAST:event_btnExportHooksActionPerformed

    private void btnImportHooksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportHooksActionPerformed
		OvlCodeEntry p = getSelectedPatch();

		if (p != null) {
			FSFile sourceFile = XFileDialog.openFileDialog(Yaml.EXTENSION_FILTER);

			if (sourceFile != null) {
				RPM rpm = p.getFullRPM();
				
				RPMTool.readRelocationsFromYml(rpm, sourceFile);

				p.updated = true;
				save();
			}
		}
    }//GEN-LAST:event_btnImportHooksActionPerformed

    private void btnELF2RPMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnELF2RPMActionPerformed
		if (ciSys != null) {
			FSFile elf = XFileDialog.openFileDialog("Select a source ELF file", CommonExtensionFilters.ELF);

			if (elf != null) {
				FSFile target = XFileDialog.openSaveFileDialog("Select a destination RPM file", elf.getNameWithoutExtension(), RPM.EXTENSION_FILTER);

				if (target != null) {
					RPM rpm = ELF2RPM.getRPM(elf, ciSys.getESDB());
					AutoRelGenerator.makeHooksAuto(rpm, ciSys.getESDB());
					target.setBytes(rpm.getBytes());
				}
			}
		}
    }//GEN-LAST:event_btnELF2RPMActionPerformed

    private void btnBranchCalcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBranchCalcActionPerformed
		new BranchCalculator().setVisible(true);
    }//GEN-LAST:event_btnBranchCalcActionPerformed

    private void btnMultiRpmConvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMultiRpmConvActionPerformed
		if (ciSys != null) {
			new MultiElfToRpmDialog(this, true, ciSys.getESDB()).setVisible(true);
		}
    }//GEN-LAST:event_btnMultiRpmConvActionPerformed

    private void btnReapplyHooksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReapplyHooksActionPerformed
		if (getSelectedPatch() != null) {
			getSelectedPatch().getFullRPM().doExternalRelocations(ciSys);
		}
    }//GEN-LAST:event_btnReapplyHooksActionPerformed

    private void btnExportRpmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportRpmActionPerformed
		OvlCodeEntry e = getSelectedPatch();
		if (e != null) {
			FSFile target = XFileDialog.openSaveFileDialog(RPM.EXTENSION_FILTER);
			if (target != null) {
				target.setBytes(e.getFullRPM().getBytesForBaseOfs(0));
			}
		}
    }//GEN-LAST:event_btnExportRpmActionPerformed

    private void btnStripSymbolNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStripSymbolNamesActionPerformed
		OvlCodeEntry e = getSelectedPatch();
		if (e != null) {
			e.getFullRPM().strip();
			e.updated = true;
			save();
		}
    }//GEN-LAST:event_btnStripSymbolNamesActionPerformed

	private static enum PMCModulePriority {
		SYSTEM_CRUCIAL,
		COMMON_DEPENDENCY_1,
		COMMON_DEPENDENCY_2,
		COMMON_DEPENDENCY_3,
		PATCH
	}

    private void btnRPM2DLLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRPM2DLLActionPerformed
		FSFile src = XFileDialog.openFileDialog(RPM.EXTENSION_FILTER);
		if (src != null) {
			RPM rpm = new RPM(src);
			rpm.strip();
			if (rpm.getBaseAddress() != 0) {
				rpm.updateBytesForBaseAddr(0);
			}
			PMCModulePriority type = (PMCModulePriority) JOptionPane.showInputDialog(this, "What priority should this module be handled with?", "Module priority", JOptionPane.QUESTION_MESSAGE, null, PMCModulePriority.values(), PMCModulePriority.PATCH);
			if (type != null) {
				rpm.metaData.putValue(new RPMMetaData.RPMMetaValue("PMCModulePriority", type));
				FSFile dst = XFileDialog.openSaveFileDialog(null, src.getNameWithoutExtension(), CommonExtensionFilters.DLL);
				if (dst != null) {
					dst.setBytes(rpm.getBytes("DLXF"));
				}
			}
		}
    }//GEN-LAST:event_btnRPM2DLLActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		ComponentUtils.setSystemNativeLookAndFeel();

		java.awt.EventQueue.invokeLater(() -> {
			CodeInjectionSystemGUI form = new CodeInjectionSystemGUI();
			form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			form.setVisible(true);
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel baseOfs;
    private javax.swing.JLabel baseOfsLabel;
    private javax.swing.JButton btnAddPatch;
    private javax.swing.JMenuItem btnBranchCalc;
    private javax.swing.JMenuItem btnELF2RPM;
    private javax.swing.JButton btnExportHooks;
    private javax.swing.JButton btnExportRpm;
    private javax.swing.JButton btnExportSymMap;
    private javax.swing.JButton btnImportHooks;
    private javax.swing.JMenuItem btnMultiRpmConv;
    private javax.swing.JMenuItem btnOpenGameDir;
    private javax.swing.JMenuItem btnRPM2DLL;
    private javax.swing.JButton btnReapplyHooks;
    private javax.swing.JButton btnRemovePatch;
    private javax.swing.JButton btnStripSymbolNames;
    private javax.swing.JSeparator controlSep;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JList<String> patchList;
    private javax.swing.JScrollPane patchListSP;
    private javax.swing.JLabel selPatch;
    private javax.swing.JLabel selPatchLabel;
    private javax.swing.JLabel selPatchSize;
    private javax.swing.JLabel selPatchSizeLabel;
    private javax.swing.JMenu toolsMenu;
    // End of variables declaration//GEN-END:variables
}
