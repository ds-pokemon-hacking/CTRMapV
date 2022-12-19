package ctrmap.formats.ntr.nitroreader.nsbca.tracks;

import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSRotationDecoder;
import ctrmap.formats.ntr.nitroreader.common.Utils;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import org.joml.Matrix3f;

public abstract class NSBCARotationTrack {

	public abstract Matrix3f[] getData(NTRDataIOStream byteReader, int j, int j2, int j3, int i) throws IOException;

	public abstract int getFrameStep();

	public static Matrix3f readMatrix(NTRDataIOStream data, int JAC_offset, int ofsRot3, int ofsRot5, int matrix_index) throws IOException {
		boolean isRot3 = Utils.flagComp(matrix_index, 0x8000);
		matrix_index &= 0x7FFF;
		data.checkpoint();
		data.seek(JAC_offset);
		if (isRot3) {
			data.skipBytes((6 * matrix_index) + ofsRot3);
			return decodeRotationCompact(data);
		} else {
			data.skipBytes((10 * matrix_index) + ofsRot5);
			return decodeRotation3x3(data);
		}
	}

	private static Matrix3f decodeRotationCompact(NTRDataIOStream data) throws IOException {
		int flags = data.readUnsignedShort();
		
		float A = data.readFX16();
		float B = data.readFX16();
		
		boolean invD = Utils.flagComp(flags, 64);
		boolean invC = Utils.flagComp(flags, 32);
		boolean invOne = Utils.flagComp(flags, 16);
		
		Matrix3f result = NNSRotationDecoder.decodeRotationCompact(A, B, invC, invD, invOne, flags & 0xF, new Matrix3f());
		data.resetCheckpoint();
		return result;
	}

	public static Matrix3f decodeRotation3x3(NTRDataIOStream data) throws IOException {
		final short _0 = data.readShort();
		final short _1 = data.readShort();
		final short _2 = data.readShort();
		final short _3 = data.readShort();
		final short _4 = data.readShort();

		final float m00 = FX.unfx16(_0 >> 3);
		final float m01 = FX.unfx16(_1 >> 3);
		final float m02 = FX.unfx16(_2 >> 3);
		final float m10 = FX.unfx16(_3 >> 3);
		final float m11 = FX.unfx16(_4 >> 3);
		final float m12 = FX.unfx16(
			((_4 << 31) >> 20)
			| ((_0 & 0b111) << 9)
			| ((_1 & 0b111) << 6)
			| ((_2 & 0b111) << 3)
			| ((_3 & 0b111) << 0)
		);
		data.resetCheckpoint();

		Matrix3f mtx = new Matrix3f();
		Vec3f vec_tmp = new Vec3f(m00, m01, m02);
		vec_tmp.cross(m10, m11, m12);

		mtx.setColumn(0, m00, m01, m02);
		mtx.setColumn(1, m10, m11, m12);
		mtx.setColumn(2, vec_tmp);

		return mtx;
	}
}
