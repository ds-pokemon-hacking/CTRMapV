package ctrmap.util.tools;

/**
 *
 */
public class CompCharUnicodeTableGen {

	private static final int[] TABLE = new int[]{
		0x152, 0x153, 0x15E, 0x15F, 0x2018, 0x201C, 0x201D,
		0x201E, 0x2026, 0x2460, 0x2461, 0x2462, 0x2463, 0x2464,
		0x2465, 0x2466, 0x2467, 0x2468, 0x2469, 0x246A, 0x246B,
		0x246C, 0x246D, 0x246E, 0x246F, 0x2470, 0x2471, 0x2472,
		0x2473, 0x2474, 0x2475, 0x2476, 0x2477, 0x2478, 0x2479,
		0x247A, 0x247B, 0x247C, 0x247D, 0x247E, 0x247F, 0x2480,
		0x2481, 0x2482, 0x2483, 0x2484, 0x2485, 0x2486, 0x2487,
		0xFF65
	};
	
	public static void main(String[] args) {
		for (int entry : TABLE) {
			System.out.print('\'' + Character.toString((char)entry) + '\'' + ',' + ' ');
		}
	}
}
