package ctrmap.formats.ntr.nitroreader.common;

import xstandard.math.BitMath;

public class NNSAnimationTrackFlags {
	private static final int BIT_VALUE_FX16_FLAG = 28;
	private static final int BIT_VALUE_FX16_SKL_FLAG = 29;
	private static final int BIT_VALUE_CONST_FLAG = 29;
	private static final int BIT_STEP_START = 30;
	private static final int BIT_STEP_COUNT = 2;
	
	public static boolean isFX16Skl(int flags) {
		return BitMath.checkIntegerBit(flags, BIT_VALUE_FX16_SKL_FLAG);
	}
	
	public static boolean isFX16(int flags) {
		return BitMath.checkIntegerBit(flags, BIT_VALUE_FX16_FLAG);
	}
	
	public static boolean isConstant(int flags) {
		return BitMath.checkIntegerBit(flags, BIT_VALUE_CONST_FLAG);
	}

	public static int getFrameStep(int flags) {
		return (1 << BitMath.getIntegerBits(flags, BIT_STEP_START, BIT_STEP_COUNT));
	}
}
