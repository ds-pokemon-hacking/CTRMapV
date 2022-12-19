package ctrmap.editor.gui.editors.gen5.level.entities;

import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.listeners.ToggleableActionListener;
import xstandard.gui.components.listeners.ToggleableDocumentAdapter;
import xstandard.math.MathEx;
import xstandard.util.ArraysEx;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;

public class VRailOrGridLocationPanel extends javax.swing.JPanel {

	private PositionSet posSet = null;

	private boolean yIsFloat = false;

	private ToggleableDocumentAdapter mainDA = new ToggleableDocumentAdapter() {
		@Override
		public void textChangedUpdateImpl(DocumentEvent e) {
			callListeners();
		}
	};

	private ToggleableActionListener mainAL = new ToggleableActionListener() {
		@Override
		public void actionPerformedImpl(ActionEvent e) {
			callListeners();
		}
	};

	private List<ActionListener> listeners = new ArrayList<>();

	public VRailOrGridLocationPanel() {
		initComponents();
		setYIsFloat(yIsFloat);

		ComponentUtils.setNFValueClass(Integer.class, gridX, gridWorldY, gridZ, railLineID, railPosFront, railPosSide);

		ComponentUtils.addDocumentListenerToTFs(mainDA, gridX, gridWorldY, gridZ, railLineID, railPosFront, railPosSide);
		ComponentUtils.addActionListener(mainAL, btnUsePositionGrid, btnUsePositionRail);
	}

	private void callListeners() {
		for (ActionListener l : listeners) {
			l.actionPerformed(new ActionEvent(this, 0, "ChangePosAny"));
		}
	}

	public void addActionListener(ActionListener l) {
		ArraysEx.addIfNotNullOrContains(listeners, l);
	}
	
	public boolean getYIsFloat() {
		return yIsFloat;
	}

	public void setYIsFloat(boolean val) {
		this.yIsFloat = val;
		String format;
		Class cls;
		if (val) {
			cls = Float.class;
			format = "#0.00";
		} else {
			cls = Integer.class;
			format = "#0";
		}
		ComponentUtils.setNFValueClass(cls, gridWorldY);
		gridWorldY.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat(format))));
	}

	public void load(PositionSet posSet) {
		mainAL.setAllowEvents(false);
		mainDA.setAllowEvents(false);
		this.posSet = posSet;
		if (posSet != null) {
			btnUsePositionGrid.setSelected(!posSet.isPositionRail);
			btnUsePositionRail.setSelected(posSet.isPositionRail);
			gridX.setValue(posSet.gridX);
			gridZ.setValue(posSet.gridZ);
			if (yIsFloat) {
				gridWorldY.setValue(posSet.worldY);
			} else {
				gridWorldY.setValue((int) (posSet.worldY));
			}
			railLineID.setValue(posSet.railLineID);
			railPosFront.setValue(posSet.railPosFront);
			railPosSide.setValue(posSet.railPosSide);
			posTabs.setSelectedComponent(posSet.isPositionRail ? posRailPanel : posGridPanel);
		} else {
			ComponentUtils.clearComponents(btnUsePositionGrid, btnUsePositionRail, gridX, gridZ, gridWorldY, railLineID, railPosFront, railPosSide);
		}
		mainAL.setAllowEvents(true);
		mainDA.setAllowEvents(true);
	}

	public PositionSet save() {
		if (posSet != null) {
			posSet.isPositionRail = btnUsePositionRail.isSelected();
			posSet.gridX = ComponentUtils.getIntFromDocument(gridX, posSet.gridX);
			posSet.gridZ = ComponentUtils.getIntFromDocument(gridZ, posSet.gridZ);
			if (yIsFloat) {
				float val = ComponentUtils.getFloatFromDocument(gridWorldY, posSet.worldY);
				if (!MathEx.impreciseFloatEquals(val, posSet.worldY, 0.01f)) {
					posSet.worldY = val;
				}
			} else {
				posSet.worldY = ComponentUtils.getIntFromDocument(gridWorldY, (int)posSet.worldY);
			}
			posSet.railLineID = ComponentUtils.getIntFromDocument(railLineID, posSet.railLineID);
			posSet.railPosFront = ComponentUtils.getIntFromDocument(railPosFront, posSet.railPosFront);
			posSet.railPosSide = ComponentUtils.getIntFromDocument(railPosSide, posSet.railPosSide);
			return posSet;
		}
		return new PositionSet();
	}

	public static class PositionSet {

		public boolean isPositionRail;

		//field types are the highest precision that any entity has
		public int gridX;
		public float worldY;
		public int gridZ;

		public int railLineID;
		public int railPosFront;
		public int railPosSide;
		
		public PositionSet() {
			
		}
		
		public PositionSet(boolean isPositionRail, int gridX, float worldY, int gridZ, int railLineID, int railPosFront, int railPosSide) {
			this.isPositionRail = isPositionRail;
			this.gridX = gridX;
			this.worldY = worldY;
			this.gridZ = gridZ;
			this.railLineID = railLineID;
			this.railPosFront = railPosFront;
			this.railPosSide = railPosSide;
		}
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        railGridBtnGroup = new javax.swing.ButtonGroup();
        posTabs = new javax.swing.JTabbedPane();
        posGridPanel = new javax.swing.JPanel();
        gridXLabel = new javax.swing.JLabel();
        gridX = new javax.swing.JFormattedTextField();
        gridZ = new javax.swing.JFormattedTextField();
        gridZLabel = new javax.swing.JLabel();
        gridYLabel = new javax.swing.JLabel();
        gridWorldY = new javax.swing.JFormattedTextField();
        btnUsePositionGrid = new javax.swing.JRadioButton();
        posRailPanel = new javax.swing.JPanel();
        railLineNoLabel = new javax.swing.JLabel();
        railLineID = new javax.swing.JFormattedTextField();
        railPosFrontLabel = new javax.swing.JLabel();
        railPosFront = new javax.swing.JFormattedTextField();
        railPosSideLabel = new javax.swing.JLabel();
        railPosSide = new javax.swing.JFormattedTextField();
        btnUsePositionRail = new javax.swing.JRadioButton();

        gridXLabel.setForeground(new java.awt.Color(255, 0, 0));
        gridXLabel.setText("X");

        gridX.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        gridZ.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        gridZLabel.setForeground(new java.awt.Color(0, 0, 255));
        gridZLabel.setText("Z");

        gridYLabel.setForeground(new java.awt.Color(0, 153, 0));
        gridYLabel.setText("Y");

        gridWorldY.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));

        railGridBtnGroup.add(btnUsePositionGrid);
        btnUsePositionGrid.setText("Use grid coordinates");

        javax.swing.GroupLayout posGridPanelLayout = new javax.swing.GroupLayout(posGridPanel);
        posGridPanel.setLayout(posGridPanelLayout);
        posGridPanelLayout.setHorizontalGroup(
            posGridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(posGridPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(posGridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(posGridPanelLayout.createSequentialGroup()
                        .addComponent(gridXLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(gridX, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                        .addComponent(btnUsePositionGrid))
                    .addGroup(posGridPanelLayout.createSequentialGroup()
                        .addGroup(posGridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(posGridPanelLayout.createSequentialGroup()
                                .addComponent(gridYLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(gridWorldY, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(posGridPanelLayout.createSequentialGroup()
                                .addComponent(gridZLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(gridZ, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        posGridPanelLayout.setVerticalGroup(
            posGridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(posGridPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(posGridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gridXLabel)
                    .addComponent(gridX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUsePositionGrid))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(posGridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gridYLabel)
                    .addComponent(gridWorldY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(posGridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gridZLabel)
                    .addComponent(gridZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        posTabs.addTab("Grid", posGridPanel);

        railLineNoLabel.setText("Line no.");

        railLineID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        railPosFrontLabel.setText("Front pos.");

        railPosFront.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        railPosSideLabel.setText("Side pos.");

        railPosSide.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        railGridBtnGroup.add(btnUsePositionRail);
        btnUsePositionRail.setText("Use rail coordinates");

        javax.swing.GroupLayout posRailPanelLayout = new javax.swing.GroupLayout(posRailPanel);
        posRailPanel.setLayout(posRailPanelLayout);
        posRailPanelLayout.setHorizontalGroup(
            posRailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(posRailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(posRailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(railLineNoLabel)
                    .addComponent(railPosFrontLabel)
                    .addComponent(railPosSideLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(posRailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(railLineID, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(railPosFront, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(railPosSide, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addComponent(btnUsePositionRail)
                .addContainerGap())
        );
        posRailPanelLayout.setVerticalGroup(
            posRailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(posRailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(posRailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(railLineNoLabel)
                    .addComponent(railLineID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUsePositionRail))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(posRailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(railPosFrontLabel)
                    .addComponent(railPosFront, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(posRailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(railPosSideLabel)
                    .addComponent(railPosSide, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        posTabs.addTab("Rail", posRailPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(posTabs)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(posTabs)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton btnUsePositionGrid;
    private javax.swing.JRadioButton btnUsePositionRail;
    private javax.swing.JFormattedTextField gridWorldY;
    private javax.swing.JFormattedTextField gridX;
    private javax.swing.JLabel gridXLabel;
    private javax.swing.JLabel gridYLabel;
    private javax.swing.JFormattedTextField gridZ;
    private javax.swing.JLabel gridZLabel;
    private javax.swing.JPanel posGridPanel;
    private javax.swing.JPanel posRailPanel;
    private javax.swing.JTabbedPane posTabs;
    private javax.swing.ButtonGroup railGridBtnGroup;
    private javax.swing.JFormattedTextField railLineID;
    private javax.swing.JLabel railLineNoLabel;
    private javax.swing.JFormattedTextField railPosFront;
    private javax.swing.JLabel railPosFrontLabel;
    private javax.swing.JFormattedTextField railPosSide;
    private javax.swing.JLabel railPosSideLabel;
    // End of variables declaration//GEN-END:variables
}
