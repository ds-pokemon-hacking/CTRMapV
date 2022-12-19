package ctrmap.util.tools.ovl;

import xstandard.cli.ArgumentBuilder;
import xstandard.cli.ArgumentPattern;
import xstandard.cli.ArgumentType;
import xstandard.formats.yaml.Yaml;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.DialogUtils;
import xstandard.gui.DnDHelper;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.file.XFileDialog;
import java.awt.Color;
import java.io.File;
import java.util.List;
import ctrmap.formats.ntr.rom.OverlayTable;
import xstandard.fs.FSFile;

public class OvlUtil extends javax.swing.JFrame {

	private OverlayTable ovlTable;
	private File ovlDir;

	public OvlUtil() {
		initComponents();

		DnDHelper.addFileDropTarget(ovlTblDrop, new DnDHelper.FileDropListener() {
			@Override
			public void acceptDrop(List<File> files) {
				if (!files.isEmpty()) {
					File f = files.get(0);
					if (f.exists() && !f.isDirectory()) {
						try {
							ovlTable = new OverlayTable(new DiskFile(f));
						} catch (Exception e) {
							ovlTable = null;
						}
						setLoadedStuff();
					}
				}
			}
		});

		DnDHelper.addFileDropTarget(ovlDirDrop, new DnDHelper.FileDropListener() {
			@Override
			public void acceptDrop(List<File> files) {
				if (!files.isEmpty()) {
					File f = files.get(0);
					ovlDir = f;
					setLoadedStuff();
				}
			}
		});

		setLoadedStuff();
	}

	private static final ArgumentPattern[] CLI_ARGS = new ArgumentPattern[]{
		new ArgumentPattern("mode", "Mode of operation (rebuild/toyml/fromyml).", ArgumentType.STRING, "rebuild", "-m", "--mode"),
		new ArgumentPattern("table", "An overlay table", ArgumentType.STRING, null, "-y9", "--ovl"),
		new ArgumentPattern("dir", "A directory containing overlays in \"overlay_####.bin\" format.", ArgumentType.STRING, null, "-d", "--dir"),
		new ArgumentPattern("yml", "An input/output YAML file.", ArgumentType.STRING, null, "-y", "--yml"),};

	public static void main(String[] args) {
		if (args.length == 0) {
			ComponentUtils.setSystemNativeLookAndFeel();
			new OvlUtil().setVisible(true);
		} else {
			ArgumentBuilder bld = new ArgumentBuilder(CLI_ARGS);
			bld.parse(args);
			try {
				String mode;
				if (bld.getContent("mode", true) != null) {
					mode = bld.getContent("mode").stringValue();
				} else {
					mode = bld.defaultContent.exists() ? bld.defaultContent.stringValue() : "rebuild";
				}

				switch (mode) {
					case "rebuild":
						cliRebuild(bld);
						break;
					case "toyml":
						cliToYml(bld);
						break;
					case "fromyml":
						cliFromYml(bld);
						break;
				}

			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println();
				System.out.println("OvlUtil Command Line Interface 1.0.1");
				System.out.println();
				bld.print();
			}
		}
	}

	private static boolean ymlChecks(File ovTblFile, File ymlFile) {
		if (!ovTblFile.exists()) {
			System.out.println("Supplied overlay table does not exist.");
			return false;
		}
		if (!ymlFile.exists()) {
			System.out.println("Supplied YAML file does not exist.");
			return false;
		}
		if (ovTblFile.isDirectory()) {
			System.out.println("The overlay table should not be a directory.");
			return false;
		}
		if (ymlFile.isDirectory()) {
			System.out.println("The YML file should not be a directory.");
			return false;
		}
		return true;
	}

	private static void cliFromYml(ArgumentBuilder bld) {
		String ovTbl = bld.getContent("table").stringValue();
		String dir = bld.getContent("yml").stringValue();

		File ovTblFile = new File(ovTbl);
		File ymlFile = new File(dir);

		if (ymlChecks(ovTblFile, ymlFile)) {
			OverlayTable tbl = new OverlayTable(new DiskFile(ovTblFile));
			if (tbl.setYML(new Yaml(new DiskFile(ymlFile)))) {
				tbl.write();
			} else {
				System.out.println("Malformed YML.");
			}
		}
	}

	private static void cliToYml(ArgumentBuilder bld) {
		String ovTbl = bld.getContent("table").stringValue();
		String dir = bld.getContent("yml").stringValue();

		File ovTblFile = new File(ovTbl);
		File ymlFile = new File(dir);

		if (ymlChecks(ovTblFile, ymlFile)) {
			OverlayTable tbl = new OverlayTable(new DiskFile(ovTblFile));
			tbl.getYML().writeToFile(new DiskFile(ymlFile));
		}
	}

	private static void cliRebuild(ArgumentBuilder bld) {
		String ovTbl = bld.getContent("table").stringValue();
		String dir = bld.getContent("dir").stringValue();

		File ovTblFile = new File(ovTbl);
		File dirFile = new File(dir);

		if (!ovTblFile.exists()) {
			System.out.println("Supplied overlay table does not exist.");
			return;
		}
		if (!dirFile.exists()) {
			System.out.println("Supplied overlay directory does not exist.");
			return;
		}
		if (ovTblFile.isDirectory()) {
			System.out.println("The overlay table should not be a directory.");
			return;
		}
		if (!dirFile.isDirectory()) {
			System.out.println("The overlay directory is not a directory.");
			return;
		}

		OverlayTable tbl = new OverlayTable(new DiskFile(ovTblFile));
		tbl.updateByDir(new DiskFile(dirFile));
		tbl.write();
	}

	public static final Color NO_LOADED_COLOR = new Color(240, 240, 240);
	public static final Color LOADED_COLOR = new Color(0, 204, 0);

	private boolean ovlValid() {
		return ovlTable != null;
	}

	private boolean dirValid() {
		return ovlDir != null && ovlDir.isDirectory();
	}

	private boolean valid() {
		return ovlValid() && dirValid();
	}

	public void setLoadedStuff() {
		ovlTblDrop.setBackground(ovlValid() ? LOADED_COLOR : NO_LOADED_COLOR);
		ovlDirDrop.setBackground(dirValid() ? LOADED_COLOR : NO_LOADED_COLOR);

		ComponentUtils.setComponentsEnabled(valid(), btnRebuildTable);
		ComponentUtils.setComponentsEnabled(ovlValid(), btnExportYml, btnImportYml);
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ovlDirDrop = new javax.swing.JLabel();
        ovlTblDrop = new javax.swing.JLabel();
        btnRebuildTable = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnImportYml = new javax.swing.JButton();
        btnExportYml = new javax.swing.JButton();
        yamlLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("OvlUtil");
        setLocationByPlatform(true);

        ovlDirDrop.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ovlDirDrop.setText("Drop an overlay directory");
        ovlDirDrop.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        ovlTblDrop.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ovlTblDrop.setText("Drop an overlay table");
        ovlTblDrop.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnRebuildTable.setText("Rebuild overlay table from directory");
        btnRebuildTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRebuildTableActionPerformed(evt);
            }
        });

        btnImportYml.setText("Import overlay table");
        btnImportYml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportYmlActionPerformed(evt);
            }
        });

        btnExportYml.setText("Export overlay table");
        btnExportYml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportYmlActionPerformed(evt);
            }
        });

        yamlLabel.setText("YAML");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(ovlTblDrop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ovlDirDrop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnRebuildTable))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(yamlLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 115, Short.MAX_VALUE)
                        .addComponent(btnExportYml)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImportYml)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ovlTblDrop, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                    .addComponent(ovlDirDrop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRebuildTable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExportYml)
                    .addComponent(btnImportYml)
                    .addComponent(yamlLabel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRebuildTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRebuildTableActionPerformed
		if (valid()) {
			ovlTable.updateByDir(new DiskFile(ovlDir));
			ovlTable.write();
		}
    }//GEN-LAST:event_btnRebuildTableActionPerformed

    private void btnExportYmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportYmlActionPerformed
		if (ovlValid()) {
			FSFile target = XFileDialog.openSaveFileDialog(Yaml.EXTENSION_FILTER);

			if (target != null) {
				ovlTable.getYML().writeToFile(target);
			}
		}
    }//GEN-LAST:event_btnExportYmlActionPerformed

    private void btnImportYmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportYmlActionPerformed
		if (ovlValid()) {
			FSFile source = XFileDialog.openFileDialog(Yaml.EXTENSION_FILTER);

			if (source != null) {
				Yaml yml = new Yaml(source);
				if (ovlTable.setYML(yml)) {
					ovlTable.write();
				} else {
					DialogUtils.showErrorMessage(this, "Import error", "The YML file is malformed.");
				}
			}
		}
    }//GEN-LAST:event_btnImportYmlActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExportYml;
    private javax.swing.JButton btnImportYml;
    private javax.swing.JButton btnRebuildTable;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel ovlDirDrop;
    private javax.swing.JLabel ovlTblDrop;
    private javax.swing.JLabel yamlLabel;
    // End of variables declaration//GEN-END:variables
}
