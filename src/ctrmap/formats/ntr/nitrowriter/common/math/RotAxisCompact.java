
package ctrmap.formats.ntr.nitrowriter.common.math;

import ctrmap.formats.ntr.common.FX;
import xstandard.math.MathEx;
import xstandard.math.MatrixUtil;
import org.joml.Matrix3f;

/**
 *
 */
public class RotAxisCompact {
	private static final float[] STATIC_TEMP_MTX_BUF = new float[9];
	private static final float[][] STATIC_TEMP_MTX = new float[3][3];
	
	private final float[] TEMP_MTX_BUF = new float[9];
	private final float[][] TEMP_MTX = new float[3][3];
	
	public int oneCol;
	public int oneRow;
	
	public boolean invOne; 
	public boolean invC;
	public boolean invD;
	
	public short a;
	public short b;
	
	public RotAxisCompact(Matrix3f mtx){
		mtx.get(TEMP_MTX_BUF);
		MatrixUtil.decompose3x3(TEMP_MTX_BUF, TEMP_MTX);
		if (findSTATICRC(TEMP_MTX)){
			findVarComps(TEMP_MTX);
		}
	}
	
	private RotAxisCompact(){
		
	}
	
	public int getIdxOne(){
		return oneCol * 3 + oneRow;
	}
	
	public static synchronized RotAxisCompact tryMake(Matrix3f mtx){
		RotAxisCompact result = new RotAxisCompact();
		mtx.get(STATIC_TEMP_MTX_BUF);
		MatrixUtil.decompose3x3(STATIC_TEMP_MTX_BUF, STATIC_TEMP_MTX);
		if (result.findSTATICRC(STATIC_TEMP_MTX)){
			if (result.findVarComps(STATIC_TEMP_MTX)){
				return result;
			}
		}
		return null;
	}
	
	private boolean findSTATICRC(float[][] mtx) {
		for (int col = 0; col < 3; col++) {
			for (int row = 0; row < 3; row++) {
				float v = mtx[col][row];
				invOne = v == -1f;
				if (v == 1f || invOne) {
					boolean invalid = false;
					for (int i = 0; i < 3; i++) {
						if (i != row && mtx[col][i] != 0f) {
							invalid = true;
							break;
						}
						if (i != col && mtx[row][i] != 0f) {
							invalid = true;
							break;
						}
					}
					if (!invalid) {
						oneCol = col;
						oneRow = row;
						return true;
					}
				}
			}
		}
		oneCol = -1;
		oneRow = -1;
		return false;
	}

	private boolean findVarComps(float[][] mtx) {
		int idx = 0;
		float[] varComps = new float[4];
		for (int col = 0; col < 3; col++) {
			for (int row = 0; row < 3; row++) {
				if (row != oneRow && col != oneCol) {
					varComps[idx] = mtx[col][row];
					idx++;
				}
			}
		}
		
		if (MathEx.impreciseFloatEquals(varComps[0], varComps[3], 0.001f)) {
			invD = false;
		} else if (MathEx.impreciseFloatEquals(varComps[0], -varComps[3], 0.001f)) {
			invD = true;
		} else {
			return false;
		}
		if (MathEx.impreciseFloatEquals(varComps[1], varComps[2], 0.001f)) {
			invC = false;
		} else if (MathEx.impreciseFloatEquals(varComps[1], -varComps[2], 0.001f)) {
			invC = true;
		} else {
			return false;
		}
		
		a = FX.fx16(varComps[0]);
		b = FX.fx16(varComps[1]);
		
		return true;
	}
}
