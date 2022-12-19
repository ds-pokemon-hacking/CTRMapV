package ctrmap.formats.qos;

/**
 *
 */
public class QOSConstantDivisionDebug {

	private static int divideBy6144(long size) {
		long v5 = size >> 31L;
		long v6 = 0x1800 * (v5 + ((int) ((0x2AAAAAABL * (int) size) >> 32) >> 10));
		long v9 = 0x2AAAAAABL * (int) size;
		return (int) (v9 >> 42);
	}
	
	private static int align6144(long size) {
		long v5 = size >> 31L;
		long v6 = 0x1800 * (v5 + ((int) ((0x2AAAAAABL * (int) size) >> 32) >> 10));
		return (int) v6;
	}

	public static void main(String[] args) {
		int last = -1;
		for (int i = 0; i < 1000000; i++) {
			int div = divideBy6144(i);
			if (div != last) {
				System.out.println(div + " (" + i + ")");
				System.out.println(align6144(i + 420));
				last = div;
			}
		}
	}
}
