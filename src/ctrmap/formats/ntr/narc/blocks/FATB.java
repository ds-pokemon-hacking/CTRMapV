package ctrmap.formats.ntr.narc.blocks;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import xstandard.fs.accessors.arc.ArcInput;
import xstandard.io.InvalidMagicException;
import xstandard.io.util.StringIO;

public class FATB {

	public static final String FATB_MAGIC = "BTAF";

	public List<DataInfo> entries = new ArrayList<>();
	public int sectionSize;

	public FATB(DataInput in) throws IOException {
		if (!StringIO.checkMagic(in, FATB_MAGIC)) {
			throw new InvalidMagicException("Bad FATB magic.");
		}
		sectionSize = in.readInt();
		int entryCount = in.readInt();
		for (int i = 0; i < entryCount; i++) {
			entries.add(new DataInfo(in));
		}
	}

	public DataInfo insertEntry(ArcInput arcInput) {
		int targetEntry = arcInput.getTargetEntryNum();

		while (targetEntry >= entries.size()) {
			entries.add(new DataInfo());
			sectionSize += DataInfo.BYTES;
		}

		return entries.get(targetEntry);
	}

	public void buildFromFIMBOffsetMap(Map<DataInfo, Integer> offsetMap, NARC garc) {
		for (DataInfo di : entries) {
			di.endOffset = offsetMap.get(di) + (di.endOffset - di.startOffset);
			di.startOffset = offsetMap.get(di);
		}
	}

	public void write(DataOutput dos) throws IOException {
		StringIO.writeStringUnterminated(dos, FATB_MAGIC);
		dos.writeInt(sectionSize);
		dos.writeInt(entries.size());
		for (DataInfo e : entries) {
			e.write(dos);
		}
	}

	public static class DataInfo {

	public static final int BYTES = 8;

	public int startOffset;
	public int endOffset;

	public DataInfo() {

	}

	public DataInfo(DataInput in) throws IOException {
		startOffset = in.readInt();
		endOffset = in.readInt();
	}

	public void write(DataOutput out) throws IOException {
		out.writeInt(startOffset);
		out.writeInt(endOffset);
	}
}

}
