
package ctrmap.formats.ntr.nitrowriter.nsbtx;

import ctrmap.formats.ntr.nitrowriter.common.NNSG3DWriter;
import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.renderer.scene.texturing.Texture;
import xstandard.gui.file.ExtensionFilter;
import java.util.List;

public class NSBTXWriter extends NNSG3DWriter {
	
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Nitro System Binary Texture", "*.nsbtx");

	public NSBTXWriter(List<Texture> textures, NNSWriterLogger log) {
		super("BTX0");
		addBlock(new TEX0(textures, log));
	}

}
