package ctrmap.creativestudio.nitroplugin;

import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import xstandard.gui.DialogUtils;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

public class NGCSNNSWriterLogger implements NNSWriterLogger {

	public final List<String> errors = new ArrayList<>();

	@Override
	public void out(String str) {

	}

	@Override
	public void err(String str) {
		errors.add(str);
		System.err.println(str);
	}

	public void popupErrDialog(Frame parent) {
		if (!errors.isEmpty()) {
			DialogUtils.showWarningMessage(parent, "Finished with errors", String.join("\n", errors));
		}
	}
}
