package ctrmap.formats.ntr.nitroreader.nsbta;

import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSAnimationTrackFlags;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import ctrmap.formats.ntr.nitroreader.common.Utils;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import ctrmap.renderer.util.AnimeProcessor;
import xstandard.math.FAtan;
import java.io.IOException;
import java.util.List;

public class NSBTATrack {

	public String name;

	private int scaleUStep;
	private int scaleVStep;
	private int rotateStep;
	private int translateUStep;
	private int translateVStep;

	private float[] rotation;
	private float[] scaleU;
	private float[] scaleV;
	private float[] transU;
	private float[] transV;

	public NSBTATrack(NTRDataIOStream data, int numFrame, NNSPatriciaTreeReader.Entry entry) throws IOException {
		name = entry.getName();
		int sx1 = entry.getParam(0);
		int sx2 = entry.getParam(1);
		int sy1 = entry.getParam(2);
		int sy2 = entry.getParam(3);
		int rot = entry.getParam(4);
		int rot2 = entry.getParam(5);
		int tx1 = entry.getParam(6);
		int tx2 = entry.getParam(7);
		int ty1 = entry.getParam(8);
		int ty2 = entry.getParam(9);

		translateUStep = NNSAnimationTrackFlags.getFrameStep(tx1);
		translateVStep = NNSAnimationTrackFlags.getFrameStep(ty1);
		rotateStep = NNSAnimationTrackFlags.getFrameStep(rot);
		scaleUStep = NNSAnimationTrackFlags.getFrameStep(sx1);
		scaleVStep = NNSAnimationTrackFlags.getFrameStep(sy1);
		transU = readVectorChannel(data, numFrame, tx1, tx2, translateUStep);
		transV = readVectorChannel(data, numFrame, ty1, ty2, translateVStep);
		rotation = readRotationChannel(data, numFrame, rot, rot2, rotateStep);
		scaleU = readVectorChannel(data, numFrame, sx1, sx2, scaleUStep);
		scaleV = readVectorChannel(data, numFrame, sy1, sy2, scaleVStep);
	}

	public MatAnimBoneTransform toGeneric() {
		MatAnimBoneTransform bt = new MatAnimBoneTransform();
		bt.name = name;

		copyKFL(scaleU, scaleUStep, bt.msx[0]);
		copyKFL(scaleV, scaleVStep, bt.msy[0]);

		copyKFL(transU, translateUStep, bt.mtx[0]);
		copyKFL(transV, translateVStep, bt.mty[0]);

		copyKFL(rotation, rotateStep, bt.mrot[0]);

		return bt;
	}

	private static void copyKFL(float[] values, int step, List<KeyFrame> kfl) {
		for (int i = 0; i < values.length; i++) {
			KeyFrame kf = new KeyFrame(i * step, values[i]);
			kfl.add(kf);
		}
		AnimeProcessor.createKeyframes(kfl, 0.001f);
	}

	private float[] readVectorChannel(NTRDataIOStream data, int frameCount, int flags, int param2, int step) throws IOException {
		boolean isConst = NNSAnimationTrackFlags.isConstant(flags);
		boolean fx16 = NNSAnimationTrackFlags.isFX16(flags);
		if (!isConst) {
			int valueCount = Utils.getValueCount(frameCount, step);
			float[] values = new float[valueCount];
			data.seek(param2);
			int i;
			if (fx16) {
				for (i = 0; i < valueCount; i++) {
					values[i] = data.readFX16();
				}
			} else {
				for (i = 0; i < valueCount; i++) {
					values[i] = data.readFX32();
				}
			}
			return values;
		} else if (fx16) {
			return new float[]{FX.unfx16((param2 << 16) >> 16)};
		} else {
			return new float[]{FX.unfx32(param2)};
		}
	}

	private float atan2(short sin, short cos) {
		return FAtan.atan2(
			FX.unfx16(sin),
			FX.unfx16(cos)
		);
	}

	private float[] readRotationChannel(NTRDataIOStream data, int frameCount, int flags, int param2, int step) throws IOException {
		boolean isConst = NNSAnimationTrackFlags.isConstant(flags);
		float[] values;
		if (NNSAnimationTrackFlags.isFX16(flags)) {
			throw new UnsupportedOperationException("FX16 NOT ALLOWED IN ROTATION ???");
		} else if (isConst) {
			values = new float[1];
			values[0] = atan2((short) param2, (short) (param2 >> 16));
			return values;
		} else {
			int valueCount = Utils.getValueCount(frameCount, step);
			values = new float[valueCount];
			data.seek(param2);
			for (int i = 0; i < valueCount; i++) {
				values[i] = atan2(data.readShort(), data.readShort());
			}
			return values;
		}
	}
}
