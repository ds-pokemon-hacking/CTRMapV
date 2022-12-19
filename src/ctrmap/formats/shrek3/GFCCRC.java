package ctrmap.formats.shrek3;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class GFCCRC {

	private static final CRC32 CRC_INSTANCE = new CRC32();

	public static void normalizePath(char[] path) {
		for (int i = 0; i < path.length; i++) {
			path[i] = Character.toLowerCase(path[i]);
		}
	}
	
	public static String normalizePath(String path) {
		return path.toLowerCase();
	}

	public static int getByteArrCrc(byte[] bytes) {
		CRC_INSTANCE.reset();
		CRC_INSTANCE.update(bytes);
		return (int) CRC_INSTANCE.getValue();
	}
	
	public static int getCharArrCrc(char[] chars) {
		CRC_INSTANCE.reset();
		for (char c : chars) {
			CRC_INSTANCE.update(c & 0xFF);
		}
		return (int) CRC_INSTANCE.getValue();
	}

	public static int getPathHash(String path) {
		return getByteArrCrc(normalizePath(path).getBytes(StandardCharsets.US_ASCII));
	}
	
	public static int getPathHash(char[] chars) {
		return getCharArrCrc(chars);
	}
}
