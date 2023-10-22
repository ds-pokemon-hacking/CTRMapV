
package ctrmap.formats.ntr.nitrowriter.nsbca;

import ctrmap.formats.ntr.nitrowriter.common.NNSG3DWriter;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.model.Skeleton;
import xstandard.gui.file.ExtensionFilter;

public class NSBCAWriter extends NNSG3DWriter {
	
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Nitro System Binary Character Animation", "*.nsbca");

	public NSBCAWriter(NSBCAExportSettings settings, Skeleton skl, SkeletalAnimation... anm) {
		super("BCA0");
		addBlock(new JNT0(settings, skl, anm));
	}
	
	public NSBCAWriter(Skeleton skl, SkeletalAnimation... anm) {
		this(new NSBCAExportSettings(), skl, anm);
	}
}
