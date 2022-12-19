package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.elements;

import ctrmap.formats.ntr.common.FX;
import static ctrmap.formats.ntr.common.FX.fx16;
import java.io.DataOutput;
import java.io.IOException;
import org.joml.Matrix3f;

/**
 *
 */
public class Rot3x3CompactElem implements RotationElement {

	private short fx_m00;
	private short fx_m01;
	private short fx_m02;
	private short fx_m10;
	private short fx_m11;

	private short fx_m12;

	private Matrix3f mat_debug;

	public Rot3x3CompactElem(Matrix3f mat) {
		//System.out.println(new Matrix4(mat).getRotation());
		fx_m00 = getFxLim(mat.m00);
		fx_m01 = getFxLim(mat.m01);
		fx_m02 = getFxLim(mat.m02);
		fx_m10 = getFxLim(mat.m10);
		fx_m11 = getFxLim(mat.m11);

		fx_m12 = getFxLim(mat.m12);

		mat_debug = mat;
	}
	
	private static short getFxLim(float flt){
		return (short)Math.min(4095, fx16(flt)); //we can't write 4096 (1) as a full value since that bit is the signum
	}

	@Override
	public void write(DataOutput out) throws IOException {
		short _4 = (short) (fx_m11 << 3 | ((fx_m12 >> 15) & 0b111)); //Signum of m12
		short _3 = (short) (fx_m10 << 3 | ((fx_m12) & 0b111));
		short _2 = (short) (fx_m02 << 3 | ((fx_m12 >>= 3) & 0b111));
		short _1 = (short) (fx_m01 << 3 | ((fx_m12 >>= 3) & 0b111));
		short _0 = (short) (fx_m00 << 3 | ((fx_m12 >> 3) & 0b111));

		out.writeShort(_0);
		out.writeShort(_1);
		out.writeShort(_2);
		out.writeShort(_3);
		out.writeShort(_4);
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Rot3x3CompactElem) {
			Rot3x3CompactElem e = (Rot3x3CompactElem) o;
			return FX.fx_ImpreciseEquals(e.fx_m00, fx_m00, 1)
				&& FX.fx_ImpreciseEquals(e.fx_m01, fx_m01, 1)
				&& FX.fx_ImpreciseEquals(e.fx_m02, fx_m02, 1)
				&& FX.fx_ImpreciseEquals(e.fx_m10, fx_m10, 1)
				&& FX.fx_ImpreciseEquals(e.fx_m11, fx_m11, 1)
				&& FX.fx_ImpreciseEquals(e.fx_m12, fx_m12, 1);
		}
		return false;
	}
}
