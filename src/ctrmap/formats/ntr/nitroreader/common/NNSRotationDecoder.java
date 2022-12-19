package ctrmap.formats.ntr.nitroreader.common;

import org.joml.Matrix3f;

public class NNSRotationDecoder {

	public static Matrix3f decodeRotationCompact(float A, float B, boolean invC, boolean invD, boolean invOne, int oneIdx, Matrix3f dest) {
		float C = invC ? -B : B;
		float D = invD ? -A : A;
		float one = invOne ? -1f : 1f;

		int oneCol = oneIdx / 3;
		int oneRow = oneIdx % 3;
		float[] lut = new float[]{A, B, C, D};
		int lutIndex = 0;
		for (int col = 0; col < 3; col++) {
			for (int row = 0; row < 3; row++) {
				if (row == oneRow || col == oneCol) {
					dest.set(col, row, 0f);
				} else {
					dest.set(col, row, lut[lutIndex]);
					lutIndex++;
				}
			}
		}
		dest.set(oneCol, oneRow, one);
		return dest;
	}
}
