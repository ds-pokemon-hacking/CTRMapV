package ctrmap.formats.pokemon.gen5.gfbca;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationFrame;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class GFBCAFrame {

	public Vec3f scale;
	public Vec3f rotation;
	public Vec3f translation;

	public GFBCAFrame(DataIOStream in, boolean s, boolean r, boolean t) throws IOException {
		if (s) {
			scale = new Vec3f(in);
		}
		if (r) {
			rotation = new Vec3f(in);
		}
		if (t) {
			translation = new Vec3f(in);
		}
	}

	private GFBCAFrame() {

	}

	public static GFBCAFrame createFromCameraState(Camera cam) {
		GFBCAFrame f = new GFBCAFrame();
		f.rotation = cam.rotation.clone();
		f.translation = cam.translation.clone();
		return f;
	}

	public static GFBCAFrame createFromSkeletalFrame(SkeletalAnimationFrame frame) {
		GFBCAFrame f = new GFBCAFrame();
		f.translation = frame.getTranslation();
		f.rotation = frame.getRotationEuler();
		f.rotation.mul(MathEx.RADIANS_TO_DEGREES);
		f.scale = frame.getScale();
		return f;
	}

	public static GFBCAFrame createFromMatrix(Matrix4 bake) {
		GFBCAFrame f = new GFBCAFrame();
		f.translation = bake.getTranslation();
		f.rotation = bake.getRotation();
		f.rotation.mul(MathEx.RADIANS_TO_DEGREES);
		f.scale = bake.getScale();
		return f;
	}

	public void write(DataOutput out, boolean s, boolean r, boolean t) throws IOException {
		if (s) {
			scale.write(out);
		}
		if (r) {
			rotation.write(out);
		}
		if (t) {
			translation.write(out);
		}
	}
}
