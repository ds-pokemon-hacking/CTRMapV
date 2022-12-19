package ctrmap.util.tools.ovl;

import xstandard.arm.ARMAssembler;
import xstandard.arm.ThumbAssembler;
import xstandard.text.FormattingUtils;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.listeners.DocumentAdapterEx;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class BranchCalculator extends javax.swing.JFrame {

	public BranchCalculator() {
		initComponents();

		DocumentListener dl = new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				setCalcResult();
			}
		};
		ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setCalcResult();
			}
		};

		srcAdd.getDocument().addDocumentListener(dl);
		tgtAdd.getDocument().addDocumentListener(dl);
		btnISAARM.addChangeListener(cl);
		btnISAThumb.addChangeListener(cl);
		btnIsLE.addChangeListener(cl);

		setLocationRelativeTo(null);
	}

	public void setCalcResult() {
		int srcAddress = -1;
		try {
			srcAddress = Integer.parseInt(srcAdd.getText(), 16);
			srcAddStatus.setText("OK");
		} catch (NumberFormatException ex) {
			srcAddStatus.setText("!");
			return;
		}
		int tgtAddress = -1;
		try {
			tgtAddress = Integer.parseInt(tgtAdd.getText(), 16);
			tgtAddStatus.setText("OK");
		} catch (NumberFormatException ex) {
			tgtAddStatus.setText("!");
			return;
		}
		try {
			int result = 0;
			DataIOStream ba = new DataIOStream();
			ba.setBase(srcAddress);
			if (btnISAARM.isSelected()) {
				ARMAssembler.writeBranchInstruction(ba, tgtAddress, true);

			} else {
				ThumbAssembler.writeBranchLinkInstruction(ba, tgtAddress);
			}
			result = ByteBuffer.wrap(ba.toByteArray()).getInt();
			String resStr;
			if (btnIsLE.isSelected()) {
				if (btnISAARM.isSelected()) {
					resStr = FormattingUtils.getStrWithLeadingZeros(8, Integer.toHexString(Integer.reverseBytes(result)));
				} else {
					resStr = FormattingUtils.getStrWithLeadingZeros(4, Integer.toHexString(Short.reverseBytes((short) ((result >>> 16) & 0xFFFF)) & 0xFFFF))
							+ " " + FormattingUtils.getStrWithLeadingZeros(4, Integer.toHexString(Short.reverseBytes((short) result) & 0xFFFF));
				}
			} else {
				resStr = FormattingUtils.getStrWithLeadingZeros(8, Integer.toHexString(result));
			}
			resultBytes.setText(resStr);
		} catch (IOException ex) {
			Logger.getLogger(BranchCalculator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        isaBtnGroup = new javax.swing.ButtonGroup();
        srcAddLabel = new javax.swing.JLabel();
        srcAdd = new javax.swing.JTextField();
        tgtAddLabel = new javax.swing.JLabel();
        tgtAdd = new javax.swing.JTextField();
        isaLabel = new javax.swing.JLabel();
        btnISAARM = new javax.swing.JRadioButton();
        btnISAThumb = new javax.swing.JRadioButton();
        rslLabel = new javax.swing.JLabel();
        resultBytes = new javax.swing.JTextField();
        btnCopy = new javax.swing.JButton();
        srcAddStatus = new javax.swing.JLabel();
        tgtAddStatus = new javax.swing.JLabel();
        btnIsLE = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Branch Calculator");
        setResizable(false);

        srcAddLabel.setText("Source address: 0x");

        tgtAddLabel.setText("Target address: 0x");

        isaLabel.setText("Instruction set:");

        isaBtnGroup.add(btnISAARM);
        btnISAARM.setSelected(true);
        btnISAARM.setText("ARM");

        isaBtnGroup.add(btnISAThumb);
        btnISAThumb.setText("Thumb");

        rslLabel.setText("Hex bytes:");

        btnCopy.setText("Copy");
        btnCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopyActionPerformed(evt);
            }
        });

        srcAddStatus.setText("OK");

        tgtAddStatus.setText("OK");

        btnIsLE.setText("Little Endian");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(srcAddLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tgtAddLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(isaLabel)
                    .addComponent(rslLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnIsLE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnISAARM)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnISAThumb))
                            .addComponent(srcAdd)
                            .addComponent(tgtAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                            .addComponent(resultBytes))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCopy)
                            .addComponent(srcAddStatus)
                            .addComponent(tgtAddStatus))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srcAddLabel)
                    .addComponent(srcAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(srcAddStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tgtAddLabel)
                    .addComponent(tgtAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tgtAddStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isaLabel)
                    .addComponent(btnISAARM)
                    .addComponent(btnISAThumb))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(resultBytes)
                        .addComponent(btnCopy))
                    .addComponent(rslLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnIsLE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopyActionPerformed
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(resultBytes.getText()), null);
    }//GEN-LAST:event_btnCopyActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		ComponentUtils.setSystemNativeLookAndFeel();

		java.awt.EventQueue.invokeLater(() -> {
			BranchCalculator bc = new BranchCalculator();
			bc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			bc.setVisible(true);
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCopy;
    private javax.swing.JRadioButton btnISAARM;
    private javax.swing.JRadioButton btnISAThumb;
    private javax.swing.JCheckBox btnIsLE;
    private javax.swing.ButtonGroup isaBtnGroup;
    private javax.swing.JLabel isaLabel;
    private javax.swing.JTextField resultBytes;
    private javax.swing.JLabel rslLabel;
    private javax.swing.JTextField srcAdd;
    private javax.swing.JLabel srcAddLabel;
    private javax.swing.JLabel srcAddStatus;
    private javax.swing.JTextField tgtAdd;
    private javax.swing.JLabel tgtAddLabel;
    private javax.swing.JLabel tgtAddStatus;
    // End of variables declaration//GEN-END:variables
}
