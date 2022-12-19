package ctrmap.formats.ntr.nitrowriter.common;

import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DDataBlock;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.PointerTable;
import xstandard.io.structs.TemporaryOffset;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NNSG3DWriter {

	public int BYTES_BASE = 0x10;

	private String signature;
	private int BOM = 0xFEFF;
	private int revision = 1;

	private List<NNSG3DDataBlock> blocks = new ArrayList<>();

	public NNSG3DWriter(String signature) {
		this.signature = signature;
	}

	public final void addBlock(NNSG3DDataBlock blk) {
		blocks.add(blk);
	}

	public byte[] writeToMemory() {
		try {
			DataIOStream out = new DataIOStream();
			write(out);
			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(NNSG3DWriter.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public void write(FSFile fsf) {
		try {
			fsf.delete();
			write(fsf.getDataIOStream());
		} catch (IOException ex) {
			Logger.getLogger(NNSG3DWriter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void write(DataIOStream out) throws IOException {
		int headerStart = out.getPosition();
		out.seek(headerStart + BYTES_BASE);
		List<TemporaryOffset> ptrTable = PointerTable.allocatePointerTable(blocks.size(), out, -headerStart, false);

		for (int i = 0; i < blocks.size(); i++) {
			ptrTable.get(i).setHere();
			out.write(blocks.get(i).getData());
			out.pad(16);
		}
		out.pad(32);
		out.writeStringUnterminated("CTRMapG3D v0.0");
		out.pad(4);

		out.seek(headerStart);
		out.writeStringUnterminated(signature);
		out.writeShort(BOM);
		out.writeShort(revision);
		out.writeInt(out.getLength() - headerStart);
		out.writeShort(BYTES_BASE);
		out.writeShort(blocks.size());
		out.close();
	}
}
