package ctrmap.formats.ntr.nitroreader.nsbca;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCARotationTrack;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCAScaleTrack;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCATranslationTrack;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDJoint;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import java.util.List;
import org.joml.Matrix3f;

public class NSBCATransform {

	public NSBCATransformHeader tags;

	public Matrix3f[] r;
	public int rStep;
	public float[] sx;
	public int sxStep;
	public float[] sy;
	public int syStep;
	public float[] sz;
	public int szStep;
	public float[] tx;
	public int txStep;
	public float[] ty;
	public int tyStep;
	public float[] tz;
	public int tzStep;

	public NSBCATransform(NTRDataIOStream data, NSBCATransformHeader tags, int JAC_offset, int ofsRot3, int ofsRot5, int numFrame) throws IOException {
		this.tags = tags;
		NSBCATranslationTrack tx = tags.tx;
		NSBCATranslationTrack ty = tags.ty;
		NSBCATranslationTrack tz = tags.tz;
		NSBCARotationTrack r = tags.r;
		NSBCAScaleTrack sx = tags.sx;
		NSBCAScaleTrack sy = tags.sy;
		NSBCAScaleTrack sz = tags.sz;
		this.tx = tx.getData(data, JAC_offset, numFrame);
		this.ty = ty.getData(data, JAC_offset, numFrame);
		this.tz = tz.getData(data, JAC_offset, numFrame);
		this.r = r.getData(data, JAC_offset, ofsRot3, ofsRot5, numFrame);
		this.sx = sx.getData(data, JAC_offset, numFrame);
		this.sy = sy.getData(data, JAC_offset, numFrame);
		this.sz = sz.getData(data, JAC_offset, numFrame);
		txStep = tx.getFrameStep();
		tyStep = ty.getFrameStep();
		tzStep = tz.getFrameStep();
		rStep = r.getFrameStep();
		sxStep = sx.getFrameStep();
		syStep = sy.getFrameStep();
		szStep = sz.getFrameStep();
	}

	public SkeletalBoneTransform toGeneric() {
		SkeletalBoneTransform bt = new SkeletalBoneTransform();

		if (!tags.isBindPoseS) {
			copyKFL(sx, sxStep, bt.sx);
			copyKFL(sy, syStep, bt.sy);
			copyKFL(sz, szStep, bt.sz);
		}

		if (!tags.isBindPoseT) {
			copyKFL(tx, txStep, bt.tx);
			copyKFL(ty, tyStep, bt.ty);
			copyKFL(tz, tzStep, bt.tz);
		}

		if (!tags.isBindPoseR) {
			copyKFLAsMatrix(r, rStep, bt.rx, bt.ry, bt.rz);
		}

		return bt;
	}

	private static void copyKFLAsMatrix(Matrix3f[] matrices, int step, List<KeyFrame> x, List<KeyFrame> y, List<KeyFrame> z) {
		for (int i = 0; i < matrices.length; i++) {
			Vec3f vec = NSBMDJoint.getJointRotation(matrices[i]);
			int f = i * step;
			x.add(new KeyFrame(f, vec.x));
			y.add(new KeyFrame(f, vec.y));
			z.add(new KeyFrame(f, vec.z));
		}
	}

	private static void copyKFL(float[] values, int step, List<KeyFrame> kfl) {
		for (int i = 0; i < values.length; i++) {
			kfl.add(new KeyFrame(i * step, values[i]));
		}
	}
}
