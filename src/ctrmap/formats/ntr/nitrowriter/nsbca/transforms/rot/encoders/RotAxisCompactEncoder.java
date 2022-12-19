package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.encoders;

import ctrmap.formats.ntr.nitrowriter.common.math.RotAxisCompact;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.elements.RotAxisCompactElem;
import xstandard.math.MathEx;
import xstandard.math.MatrixUtil;
import java.awt.Point;
import org.joml.Matrix3f;

/**
 *
 */
public class RotAxisCompactEncoder extends RotationEncoder {

	private Matrix3f currentMtx = null;
	private final float[] currentMtxBuf = new float[9];
	private final float[][] currentMtxArr = new float[3][3];

	private final Point STATICRC = new Point();
	private final float[] VARCOMPS = new float[4];
	
	private boolean invOne = false;
	private boolean invC = false;
	private boolean invD = false;
	
	private boolean currentCalcValid = false;
	
	private RotAxisCompact currentRotation;

	private void loadMatrix(Matrix3f mat) {
		currentMtx = mat;
		mat.get(currentMtxBuf);
		MatrixUtil.decompose3x3(currentMtxBuf, currentMtxArr);
	}

	private boolean findSTATICRC() {
		for (int col = 0; col < 3; col++) {
			for (int row = 0; row < 3; row++) {
				float v = currentMtxArr[col][row];
				invOne = v == -1f;
				if (v == 1f || invOne) {
					boolean invalid = false;
					for (int i = 0; i < 3; i++) {
						if (i != row && currentMtxArr[col][i] != 0f) {
							invalid = true;
							break;
						}
						if (i != col && currentMtxArr[row][i] != 0f) {
							invalid = true;
							break;
						}
					}
					if (!invalid) {
						STATICRC.x = col;
						STATICRC.y = row;
						return true;
					}
				}
			}
		}
		return false;
	}

	private void findVARCOMPS() {
		int idx = 0;
		for (int col = 0; col < 3; col++) {
			for (int row = 0; row < 3; row++) {
				if (row != STATICRC.y && col != STATICRC.x) {
					VARCOMPS[idx] = currentMtxArr[col][row];
					idx++;
				}
			}
		}
	}

	private boolean setVARCOMPSFlags() {
		if (MathEx.impreciseFloatEquals(VARCOMPS[0], VARCOMPS[3], 0.001f)) {
			invD = false;
		} else if (MathEx.impreciseFloatEquals(VARCOMPS[0], -VARCOMPS[3], 0.001f)) {
			invD = true;
		} else {
			return false;
		}
		if (MathEx.impreciseFloatEquals(VARCOMPS[1], VARCOMPS[2], 0.001f)) {
			invC = false;
		} else if (MathEx.impreciseFloatEquals(VARCOMPS[1], -VARCOMPS[2], 0.001f)) {
			invC = true;
		} else {
			return false;
		}

		return true;
	}

	private boolean tryCalcRotAxisCompact(Matrix3f mat) {
		if (currentMtx != mat) {
			currentCalcValid = false;
			loadMatrix(mat);
			if (findSTATICRC()) {
				findVARCOMPS();
				currentCalcValid = setVARCOMPSFlags();
			}
			return currentCalcValid;
		}
		return currentCalcValid;
	}

	@Override
	public boolean canEncodeMatrix(Matrix3f mat) {
		currentRotation = RotAxisCompact.tryMake(mat);
		currentMtx = mat;
		return currentRotation != null;
	}

	@Override
	public RotAxisCompactElem encodeMatrix(Matrix3f mat) {
		if (currentMtx != mat){
			currentRotation = RotAxisCompact.tryMake(mat);
		}
		
		RotAxisCompactElem e = new RotAxisCompactElem(currentRotation);
		encodedElements.add(e);
		return e;
	}

}
