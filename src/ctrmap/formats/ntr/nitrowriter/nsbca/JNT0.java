package ctrmap.formats.ntr.nitrowriter.nsbca;

import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DDataBlockBase;
import ctrmap.formats.ntr.nitrowriter.common.settings.AnimationImportSettingsBase;

public class JNT0 extends NNSG3DDataBlockBase {

	public JNT0(Skeleton skl, SkeletalAnimation... anm) {
		super("JNT0");
		for (SkeletalAnimation a : anm) {
			tree.addResource(new JointAnimationResource(a, skl, new AnimationImportSettingsBase()));
		}
	}
}
