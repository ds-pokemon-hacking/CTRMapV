package ctrmap.formats.ntr.nitroreader.common;

import ctrmap.formats.ntr.common.gfx.GXColor;

public class Utils {

	public static boolean flagComp(int n, int flag) {
		return (n & flag) == flag;
	}

	public static boolean flagComp(int n, long flag) {
		return (n & flag) == flag;
	}

	public static boolean flagComp(long n, int flag) {
		return (((long) flag) & n) == ((long) flag);
	}

	public static boolean flagComp(long n, long flag) {
		return (n & flag) == flag;
	}

	public static boolean tagComp(int value, ComparableTagEnum tag) {
		return flagComp(value, tag.getValue());
	}

	public static boolean tagComp(long value, ComparableTagEnum tag) {
		long tagValue = Integer.toUnsignedLong(tag.getValue());
		return flagComp(value, tagValue);
	}

	public static int GXColorToRGB(short value) {
		return GXColor.bit5to8((value >> 10) & 0x1F) //B
			| (GXColor.bit5to8((value >> 5) & 0x1F) << 8) //G
			| (GXColor.bit5to8((value >> 0) & 0x1F) << 16) //R
			| (0xFF000000); //A - always 255
	}

	public static int getValueCount(int frameCount, int step) {
		return (int) Math.ceil(frameCount / step);
	}
}
