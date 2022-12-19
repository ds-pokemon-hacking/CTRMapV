package ctrmap.util.tools.qos;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class QOSFSIndex {

	private static final CRC32 CRC_INSTANCE = new CRC32();

	public int field4;
	public int field8;

	public List<QOSFSFile> files = new ArrayList<>();
	
	public QOSFSIndex(DataInput in) throws IOException {
		int entryCount = in.readInt();
		field4 = in.readInt();
		field8 = in.readInt();
		for (int i = 0; i < entryCount; i++) {
			files.add(new QOSFSFile(in));
		}
	}
	
	public void write(DataOutput out) throws IOException {
		out.writeInt(files.size());
		out.writeInt(field4);
		out.writeInt(field8);
		for (QOSFSFile f : files) {
			f.write(out);
		}
	}
	
	public QOSFSFile getEntry(String path) {
		int hash = getPathHash(path);
		for (QOSFSFile f : files) {
			if (f.nameCRC32 == hash) {
				return f;
			}
		}
		return null;
	}

	public static String normalizePath(String path) {
		path = path.toLowerCase().replace('/', '\\');
		if (!path.startsWith(".\\")) {
			path = ".\\" + path;
		}
		return path;
	}

	public static int getPathHash(String path) {
		path = normalizePath(path);
		CRC_INSTANCE.reset();
		CRC_INSTANCE.update(path.getBytes(StandardCharsets.US_ASCII));
		return (int) CRC_INSTANCE.getValue();
	}

	public static class QOSFSFile {

		public int nameCRC32;
		public boolean isCompressed;
		public int romOffset;
		public int hashCorrectionOffset;
		public int uncompSize;

		public QOSFSFile(DataInput in) throws IOException {
			nameCRC32 = in.readInt();
			int field4 = in.readInt();
			isCompressed = (field4 & 1) != 0;
			romOffset = field4 >>> 1;
			int field8 = in.readInt();
			hashCorrectionOffset = field8 & 0xFF;
			uncompSize = field8 >>> 8;
		}

		public void setPath(String path) {
			nameCRC32 = getPathHash(path);
		}

		public void write(DataOutput out) throws IOException {
			out.writeInt(nameCRC32);
			out.writeInt((isCompressed ? 1 : 0) | (romOffset << 1));
			out.writeInt(hashCorrectionOffset | (uncompSize << 8));
		}
	}
}
