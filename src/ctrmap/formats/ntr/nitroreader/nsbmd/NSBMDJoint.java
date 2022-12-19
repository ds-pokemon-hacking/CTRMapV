package ctrmap.formats.ntr.nitroreader.nsbmd;

import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.ComparableTagEnum;
import ctrmap.formats.ntr.nitroreader.common.NNSRotationDecoder;
import ctrmap.formats.ntr.nitroreader.common.Utils;
import ctrmap.renderer.scene.model.Joint;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import org.joml.Matrix3f;

public class NSBMDJoint {

	public String name;

	public Vec3f scale = new Vec3f();
	public Vec3f scaleInv = new Vec3f();
	public Matrix3f rotation;
	public Vec3f translation = new Vec3f();

	public enum NSBMDJointFlag implements ComparableTagEnum {
		TRANS_ZERO(1),
		ROT_ZERO(2),
		SCALE_ONE(4),
		ROT_IS_COMPACT(8),
		ROT_COMPACT_IDX_MASK(0xF0),
		ROT_INV_ONE(256),
		ROT_INV_C(512),
		ROT_INV_D(1024);

		private final int value;

		@Override
		public int getValue() {
			return this.value;
		}

		private NSBMDJointFlag(int opcode) {
			this.value = opcode;
		}
	}

	private static Matrix3f decodeRotationCompact(int flags, float A, float B) {
		boolean invD = Utils.tagComp(flags, NSBMDJointFlag.ROT_INV_D);
		boolean invC = Utils.tagComp(flags, NSBMDJointFlag.ROT_INV_C);
		boolean invOne = Utils.tagComp(flags, NSBMDJointFlag.ROT_INV_ONE);
		int oneIdx = (flags & NSBMDJointFlag.ROT_COMPACT_IDX_MASK.value) >> 4;

		return NNSRotationDecoder.decodeRotationCompact(A, B, invC, invD, invOne, oneIdx, new Matrix3f());
	}

	public NSBMDJoint(NTRDataIOStream in, String name) throws IOException {
		this.name = name;
		int flags = in.readUnsignedShort();
		float m00 = in.readFX16();
		if (!Utils.tagComp(flags, NSBMDJointFlag.TRANS_ZERO)) {
			FXIO.readVecFX32(in, translation);
		}
		if (Utils.tagComp(flags, NSBMDJointFlag.ROT_ZERO)) {
			rotation = new Matrix3f();
		} else if (!Utils.tagComp(flags, NSBMDJointFlag.ROT_IS_COMPACT)) {
			rotation = new Matrix3f();
			rotation.m00(m00);
			for (int col = 0; col < 3; col++) {
				for (int row = 0; row < 3; row++) {
					if (col + row != 0) {
						rotation.set(col, row, in.readFX16());
					}
				}
			}
		} else {
			rotation = decodeRotationCompact(flags, in.readFX16(), in.readFX16());
		}
		if (!Utils.tagComp(flags, NSBMDJointFlag.SCALE_ONE)) {
			FXIO.readVecFX32(in, scale);
			FXIO.readVecFX32(in, scaleInv);
		} else {
			scale.set(1f);
			scaleInv.set(1f);
		}
	}

	public Joint toGeneric() {
		Joint j = new Joint();
		j.name = name;
		j.position = new Vec3f(translation);
		j.scale = new Vec3f(scale);

		j.rotation = getJointRotation(rotation);

		return j;
	}

	private static Vec3f SCALE_UNIT = new Vec3f(1f, 1f, 1f);

	public static Vec3f getJointRotation(Matrix3f rotation) {
		Vec3f vec = new Matrix4(rotation).getRotation(SCALE_UNIT);
		return vec;
	}
}
