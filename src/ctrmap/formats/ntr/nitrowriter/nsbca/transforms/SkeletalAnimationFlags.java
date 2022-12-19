
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms;

/**
 *
 */
public class SkeletalAnimationFlags {
	public static final int TRANSFORM_TRANSLATION_IS_BIND_POSE = (1 << 2);
	public static final int TRANSFORM_TRANSLATION_IS_CONSTANT_X = (1 << 3);
	public static final int TRANSFORM_TRANSLATION_IS_CONSTANT_Y = (1 << 4);
	public static final int TRANSFORM_TRANSLATION_IS_CONSTANT_Z = (1 << 5);
	
	public static final int TRANSFORM_ROTATION_IS_BIND_POSE = (1 << 7);
	public static final int TRANSFORM_ROTATION_IS_CONSTANT = (1 << 8);
	
	public static final int TRANSFORM_SCALE_IS_BIND_POSE = (1 << 10);
	public static final int TRANSFORM_SCALE_IS_CONSTANT_X = (1 << 11);
	public static final int TRANSFORM_SCALE_IS_CONSTANT_Y = (1 << 12);
	public static final int TRANSFORM_SCALE_IS_CONSTANT_Z = (1 << 13);
	
	public static final int TRACK_ENC_IS_FX16 = (1 << 29);
}
