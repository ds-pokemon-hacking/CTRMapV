package ctrmap.formats.ntr.narc.blocks;

import xstandard.io.InvalidMagicException;
import xstandard.io.util.StringIO;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FNTB {

	public static final String FNTB_MAGIC = "BTNF";

	public List<DirectoryInfo> entries = new ArrayList<>();
	public int sectionSize;

	public FNTB(DataInput in) throws IOException {
		if (!StringIO.checkMagic(in, FNTB_MAGIC)) {
			throw new InvalidMagicException("Bad FNTB magic");
		}
		sectionSize = in.readInt();
		entries.add(new DirectoryInfo(in));
		if (entries.get(0).childCount != 1){
			throw new UnsupportedOperationException("FNTB is not supported yet.");
		}
	}

	public void write(DataOutput dos) throws IOException {
		StringIO.writeStringUnterminated(dos, FNTB_MAGIC);
		dos.writeInt(sectionSize);
		for (DirectoryInfo e : entries) {
			e.write(dos);
		}
	}

	public static class DirectoryInfo {

		public static final int BYTES = 8;

		public int startOffset;
		public short firstFilePos;
		public short childCount;

		public DirectoryInfo() {

		}

		public DirectoryInfo(DataInput in) throws IOException {
			startOffset = in.readInt();
			firstFilePos = in.readShort();
			childCount = in.readShort();
		}

		public void write(DataOutput out) throws IOException {
			out.writeInt(startOffset);
			out.writeShort(firstFilePos);
			out.writeShort(childCount);
		}
	}

}
