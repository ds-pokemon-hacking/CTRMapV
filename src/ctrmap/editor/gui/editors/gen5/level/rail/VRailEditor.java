package ctrmap.editor.gui.editors.gen5.level.rail;

import ctrmap.formats.pokemon.WorldObject;
import ctrmap.editor.gui.editors.gen5.level.VLevelEditor;
import ctrmap.editor.gui.editors.gen5.level.tools.VRailTool;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import xstandard.util.ArraysEx;
import xstandard.util.ListenableList;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import ctrmap.formats.pokemon.gen5.rail.RailCurve;
import ctrmap.formats.pokemon.gen5.rail.RailData;
import ctrmap.formats.pokemon.gen5.rail.RailLine;
import ctrmap.formats.pokemon.gen5.rail.RailPoint;
import ctrmap.formats.pokemon.gen5.rail.RailTilemaps;
import ctrmap.missioncontrol_ntr.field.debug.VMapDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VMap;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.util.gui.CMGUI;

public class VRailEditor extends javax.swing.JPanel implements AbstractToolbarEditor, VMapDebugger {

	private VRailTool tool;
	public VLevelEditor editors;
	public VRailRenderer renderer;

	public RailData rails;

	private List<RailLine> lines = new ArrayList<>();
	private List<RailPoint> points = new ArrayList<>();

	private RailLine line;
	private RailPoint point;

	private boolean loaded = false;

	private DefaultListModel<String> elemListModel = new DefaultListModel<>();

	public final ListenableList<WorldObject> lineEditorObjects = new ListenableList<>();

	private static final int LINEEDITOR_OBJLIST_IDX_POINT1 = 0;
	private static final int LINEEDITOR_OBJLIST_IDX_POINT2 = 1;
	private static final int LINEEDITOR_OBJLIST_IDX_CURVE = 2;

	public VRailEditor(VLevelEditor editors) {
		initComponents();

		for (int i = 0; i < 3; i++) {
			lineEditorObjects.add(null);
		}

		this.editors = editors;
		elementList.setModel(elemListModel);

		renderer = new VRailRenderer();
		tool = new VRailTool(this);

		editorTabs.addChangeListener((ChangeEvent e) -> {
			Component comp = editorTabs.getSelectedComponent();
			if (comp == lineEditor) {
				loadLineEditor();
				showLine(line);
			} else if (comp == pointEditor) {
				loadPointEditor();
				showPoint(point);
			}
		});

		elementList.addListSelectionListener((ListSelectionEvent e) -> {
			if (loaded && elementList.getSelectedIndex() != -1) {
				Component comp = editorTabs.getSelectedComponent();
				if (comp == lineEditor) {
					showLine(lines.get(elementList.getSelectedIndex()));
				} else {
					showPoint(points.get(elementList.getSelectedIndex()));
				}
			}
		});

		loaded = true;
	}

	public void loadLineEditor() {
		loaded = false;
		editorTabs.setSelectedComponent(lineEditor);
		elemListModel.removeAllElements();

		for (RailLine l : lines) {
			elemListModel.addElement(l.name);
		}

		loaded = true;
		elementList.setSelectedIndex(lines.indexOf(line));
	}

	public void loadPointEditor() {
		loaded = false;
		editorTabs.setSelectedComponent(pointEditor);
		elemListModel.removeAllElements();

		for (RailPoint p : points) {
			elemListModel.addElement(p.name);
		}

		loaded = true;

		elementList.setSelectedIndex(points.indexOf(point));
	}

	public void showLine(int index) {
		if (index >= 0 && index < lines.size()) {
			showLine(lines.get(index));
		}
	}

	public void showLine(RailLine line) {
		this.line = line;
		lineEditorObjects.set(LINEEDITOR_OBJLIST_IDX_POINT1, null);
		lineEditorObjects.set(LINEEDITOR_OBJLIST_IDX_POINT2, null);
		lineEditorObjects.set(LINEEDITOR_OBJLIST_IDX_CURVE, null);
		if (line != null) {
			if (editorTabs.getSelectedComponent() != lineEditor) {
				loadLineEditor();
			}

			point1Name.setText(line.getP1().name);
			point2Name.setText(line.getP2().name);

			RailCurve c = line.getCurve();
			curveX.setValue(c.position.x);
			curveY.setValue(c.position.y);
			curveZ.setValue(c.position.z);

			lineAngle.setValue(line.angle);
			lineLen.setText(String.valueOf(line.lineTileLength));

			RailTilemaps.Block tilemap = line.getTilemapBlock();
			lineTilemapDim.setText(tilemap.tiles.getWidth() + "x" + tilemap.tiles.getHeight());

			lineEditorObjects.set(LINEEDITOR_OBJLIST_IDX_POINT1, line.getP1());
			lineEditorObjects.set(LINEEDITOR_OBJLIST_IDX_POINT2, line.getP2());
			lineEditorObjects.set(LINEEDITOR_OBJLIST_IDX_CURVE, line.getCurve());
		}
		renderer.setSelectedPoint(null);
		renderer.setSelectedLine(line);
	}

	public void showPoint(RailPoint point) {
		this.point = point;
		if (point != null) {
			if (editorTabs.getSelectedComponent() != pointEditor) {
				loadPointEditor();
			}

			RailLine t1 = point.attachments[0].getLine();
			RailLine t2 = point.attachments[1].getLine();

			track1Name.setText(t1 == null ? null : t1.name);
			track2Name.setText(t2 == null ? null : t2.name);

			trk1Dir.setValue(point.attachments[0].direction);
			trk2Dir.setValue(point.attachments[1].direction);
			trk1Width.setValue(point.attachments[0].width);
			trk2Width.setValue(point.attachments[1].width);
		}
		renderer.setSelectedLine(null);
		renderer.setSelectedPoint(point);
	}

	@Override
	public boolean store(boolean dialog){
		if (rails != null) {
			return CMGUI.commonSaveDataSequence(editors.getCTRMap(), rails.hash, dialog, "Rail system data", false, (() -> {
				rails.write();
			}));
		}
		return true;
	}
	
	@Override
	public boolean isDebugOnly(){
		return true;
	}
	
	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        elementListSP = new javax.swing.JScrollPane();
        elementList = new javax.swing.JList<>();
        editorTabs = new javax.swing.JTabbedPane();
        lineEditor = new javax.swing.JPanel();
        point1Label = new javax.swing.JLabel();
        point2Label = new javax.swing.JLabel();
        point1Name = new javax.swing.JLabel();
        point2Name = new javax.swing.JLabel();
        btnGoPoint2 = new javax.swing.JButton();
        btnGoPoint1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        curveX = new javax.swing.JFormattedTextField();
        curveY = new javax.swing.JFormattedTextField();
        curveZ = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        lineAngle = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        lineTilemapDim = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lineLen = new javax.swing.JLabel();
        pointEditor = new javax.swing.JPanel();
        track1Name = new javax.swing.JLabel();
        track2Name = new javax.swing.JLabel();
        btnGoTrack1 = new javax.swing.JButton();
        btnGoTrack2 = new javax.swing.JButton();
        track1Label = new javax.swing.JLabel();
        track2Label = new javax.swing.JLabel();
        trk1DirLabel = new javax.swing.JLabel();
        trk1Dir = new javax.swing.JSpinner();
        trk1WidthLabel = new javax.swing.JLabel();
        trk1Width = new javax.swing.JFormattedTextField();
        trk2DirLabel = new javax.swing.JLabel();
        trk2Dir = new javax.swing.JSpinner();
        trk2WidthLabel = new javax.swing.JLabel();
        trk2Width = new javax.swing.JFormattedTextField();

        elementList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        elementListSP.setViewportView(elementList);

        point1Label.setText("Point 1:");

        point2Label.setText("Point 2:");

        point1Name.setText("-");

        point2Name.setText("-");

        btnGoPoint2.setText("Go");
        btnGoPoint2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoPoint2ActionPerformed(evt);
            }
        });

        btnGoPoint1.setText("Go");
        btnGoPoint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoPoint1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Curve");

        curveX.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        curveY.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        curveZ.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        jLabel2.setText("Angle");

        lineAngle.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        jLabel3.setText("Tilemap dimensions:");

        lineTilemapDim.setText("-");

        jLabel4.setText("Line length:");

        lineLen.setText("-");

        javax.swing.GroupLayout lineEditorLayout = new javax.swing.GroupLayout(lineEditor);
        lineEditor.setLayout(lineEditorLayout);
        lineEditorLayout.setHorizontalGroup(
            lineEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lineEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lineEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(lineEditorLayout.createSequentialGroup()
                        .addComponent(point1Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(point1Name, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGoPoint1))
                    .addGroup(lineEditorLayout.createSequentialGroup()
                        .addComponent(point2Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(point2Name, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGoPoint2))
                    .addGroup(lineEditorLayout.createSequentialGroup()
                        .addGroup(lineEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(curveX, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(curveY, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(curveZ, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(lineAngle, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(lineEditorLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lineTilemapDim, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(lineEditorLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lineLen, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        lineEditorLayout.setVerticalGroup(
            lineEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lineEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lineEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(point1Label)
                    .addComponent(point1Name)
                    .addComponent(btnGoPoint1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(point2Label)
                    .addComponent(point2Name)
                    .addComponent(btnGoPoint2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(curveX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(curveY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(curveZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lineAngle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(lineEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lineLen))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lineTilemapDim))
                .addContainerGap(91, Short.MAX_VALUE))
        );

        editorTabs.addTab("Lines", lineEditor);

        track1Name.setText("-");

        track2Name.setText("-");

        btnGoTrack1.setText("Go");
        btnGoTrack1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoTrack1ActionPerformed(evt);
            }
        });

        btnGoTrack2.setText("Go");
        btnGoTrack2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoTrack2ActionPerformed(evt);
            }
        });

        track1Label.setText("Track 1:");

        track2Label.setText("Track 2:");

        trk1DirLabel.setText("Direction");

        trk1Dir.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        trk1WidthLabel.setText("Width");

        trk1Width.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        trk2DirLabel.setText("Direction");

        trk2Dir.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        trk2WidthLabel.setText("Width");

        trk2Width.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        javax.swing.GroupLayout pointEditorLayout = new javax.swing.GroupLayout(pointEditor);
        pointEditor.setLayout(pointEditorLayout);
        pointEditorLayout.setHorizontalGroup(
            pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pointEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pointEditorLayout.createSequentialGroup()
                        .addComponent(track1Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pointEditorLayout.createSequentialGroup()
                                .addComponent(track1Name, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnGoTrack1))
                            .addGroup(pointEditorLayout.createSequentialGroup()
                                .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pointEditorLayout.createSequentialGroup()
                                        .addComponent(trk1DirLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(trk1Dir, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(pointEditorLayout.createSequentialGroup()
                                        .addComponent(trk1WidthLabel)
                                        .addGap(18, 18, 18)
                                        .addComponent(trk1Width, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(pointEditorLayout.createSequentialGroup()
                        .addComponent(track2Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pointEditorLayout.createSequentialGroup()
                                .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pointEditorLayout.createSequentialGroup()
                                        .addComponent(trk2DirLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(trk2Dir, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(pointEditorLayout.createSequentialGroup()
                                        .addComponent(trk2WidthLabel)
                                        .addGap(18, 18, 18)
                                        .addComponent(trk2Width, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(pointEditorLayout.createSequentialGroup()
                                .addComponent(track2Name, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnGoTrack2)))))
                .addContainerGap())
        );
        pointEditorLayout.setVerticalGroup(
            pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pointEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(track1Label)
                    .addComponent(track1Name)
                    .addComponent(btnGoTrack1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trk1DirLabel)
                    .addComponent(trk1Dir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trk1Width, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(trk1WidthLabel))
                .addGap(18, 18, 18)
                .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(track2Label)
                        .addComponent(track2Name))
                    .addComponent(btnGoTrack2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trk2DirLabel)
                    .addComponent(trk2Dir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pointEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trk2Width, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(trk2WidthLabel))
                .addContainerGap(199, Short.MAX_VALUE))
        );

        editorTabs.addTab("Points", pointEditor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(elementListSP)
                    .addComponent(editorTabs))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(editorTabs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(elementListSP, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGoTrack1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoTrack1ActionPerformed
		if (point != null) {
			RailLine track = point.attachments[0].getLine();
			showLine(track);
		}
    }//GEN-LAST:event_btnGoTrack1ActionPerformed

    private void btnGoTrack2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoTrack2ActionPerformed
		if (point != null) {
			RailLine track = point.attachments[1].getLine();
			showLine(track);
		}
    }//GEN-LAST:event_btnGoTrack2ActionPerformed

    private void btnGoPoint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoPoint1ActionPerformed
		if (line != null) {
			RailPoint pnt = line.getP1();
			if (pnt != null) {
				showPoint(pnt);
			}
		}
    }//GEN-LAST:event_btnGoPoint1ActionPerformed

    private void btnGoPoint2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoPoint2ActionPerformed
		if (line != null) {
			RailPoint pnt = line.getP2();
			if (pnt != null) {
				showPoint(pnt);
			}
		}
    }//GEN-LAST:event_btnGoPoint2ActionPerformed

	@Override
	public List<AbstractTool> getTools() {
		return ArraysEx.asList(tool);
	}

	@Override
	public void loadMapMatrix(VMap chunks) {
		this.rails = chunks == null ? null : chunks.rails;
		renderer.loadMap(chunks);
		if (chunks != null && chunks.rails != null) {
			lines = chunks.rails.lines;
			points = chunks.rails.points;
		} else {
			lines = new ArrayList<>();
			points = new ArrayList<>();
		}
		
		tool.fullyRebuildScene();
		
		loadLineEditor();
		if (!lines.isEmpty()) {
			showLine(lines.get(0));
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGoPoint1;
    private javax.swing.JButton btnGoPoint2;
    private javax.swing.JButton btnGoTrack1;
    private javax.swing.JButton btnGoTrack2;
    private javax.swing.JFormattedTextField curveX;
    private javax.swing.JFormattedTextField curveY;
    private javax.swing.JFormattedTextField curveZ;
    private javax.swing.JTabbedPane editorTabs;
    private javax.swing.JList<String> elementList;
    private javax.swing.JScrollPane elementListSP;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSpinner lineAngle;
    private javax.swing.JPanel lineEditor;
    private javax.swing.JLabel lineLen;
    private javax.swing.JLabel lineTilemapDim;
    private javax.swing.JLabel point1Label;
    private javax.swing.JLabel point1Name;
    private javax.swing.JLabel point2Label;
    private javax.swing.JLabel point2Name;
    private javax.swing.JPanel pointEditor;
    private javax.swing.JLabel track1Label;
    private javax.swing.JLabel track1Name;
    private javax.swing.JLabel track2Label;
    private javax.swing.JLabel track2Name;
    private javax.swing.JSpinner trk1Dir;
    private javax.swing.JLabel trk1DirLabel;
    private javax.swing.JFormattedTextField trk1Width;
    private javax.swing.JLabel trk1WidthLabel;
    private javax.swing.JSpinner trk2Dir;
    private javax.swing.JLabel trk2DirLabel;
    private javax.swing.JFormattedTextField trk2Width;
    private javax.swing.JLabel trk2WidthLabel;
    // End of variables declaration//GEN-END:variables
}
