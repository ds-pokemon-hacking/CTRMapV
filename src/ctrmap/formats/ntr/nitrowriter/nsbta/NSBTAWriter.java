
package ctrmap.formats.ntr.nitrowriter.nsbta;

import ctrmap.formats.ntr.nitrowriter.common.NNSG3DWriter;
import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import xstandard.gui.file.ExtensionFilter;

public class NSBTAWriter extends NNSG3DWriter {
	
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Nitro System Binary Texture Animation", "*.nsbta");

	public NSBTAWriter(NNSWriterLogger log, MaterialAnimation... anm) {
		super("BTA0");
		addBlock(new SRT0(log, anm));
	}

}
