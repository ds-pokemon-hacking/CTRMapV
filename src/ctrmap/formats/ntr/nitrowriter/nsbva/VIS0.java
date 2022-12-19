package ctrmap.formats.ntr.nitrowriter.nsbva;

import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DDataBlockBase;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.model.Skeleton;

/**
 *
 */
public class VIS0 extends NNSG3DDataBlockBase {

	public VIS0(Skeleton skl, VisibilityAnimation... animations) {
		super("VIS0");
		for (VisibilityAnimation animation : animations) {
			tree.addResource(new VisibilityAnimationResource(animation, skl));
		}
	}
}
