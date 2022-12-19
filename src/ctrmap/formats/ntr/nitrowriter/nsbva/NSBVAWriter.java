
package ctrmap.formats.ntr.nitrowriter.nsbva;

import ctrmap.formats.ntr.nitrowriter.common.NNSG3DWriter;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.model.Skeleton;
import xstandard.gui.file.ExtensionFilter;

public class NSBVAWriter extends NNSG3DWriter {
	
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Nitro System Binary Visibility Animation", "*.nsbva");

	public NSBVAWriter(Skeleton skl, VisibilityAnimation... anm) {
		super("BVA0");
		addBlock(new VIS0(skl, anm));
	}

}
