package ctrmap.formats.ntr.narc.blocks;

import xstandard.io.InvalidMagicException;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.iface.DataOutputEx;
import xstandard.io.util.StringIO;
import java.io.IOException;
import java.nio.ByteOrder;

public class NARC {

	public static final String MAGIC = "NARC";
	
	public static final int VERSION_1_00 = 0x100;
	
	public static final int BOM_LE = 0xFEFF;
	public static final int BOM_BE = 0xFFFE;

	public int version = VERSION_1_00;
	public int BOM = BOM_LE;
	public int arcSize;
	public int sectionSize;

	public NARC() {

	}

	public NARC(DataInputEx in) throws IOException {
		if (!StringIO.checkMagic(in, MAGIC)) {
			throw new InvalidMagicException("Bad GARC magic.");
		}
		in.order(ByteOrder.BIG_ENDIAN);
		BOM = in.readUnsignedShort();
		in.orderByBOM(BOM, BOM_BE, BOM_LE);
		version = in.readUnsignedShort();
		if (version != VERSION_1_00) {
			throw new UnsupportedOperationException("Only v1.00 NARCs are supported.");
		}
		arcSize = in.readInt();
		sectionSize = in.readUnsignedShort();

		int blockCount = in.readUnsignedShort();
		if (blockCount != 3) {
			throw new UnsupportedOperationException("Only NARCs with 3 blocks are supported.");
		}
	}

	public void write(DataOutputEx out) throws IOException {
		StringIO.writeStringUnterminated(out, MAGIC);
		out.orderByBOM(BOM, BOM_BE, BOM_LE);
		out.writeShort(BOM_BE); //ordering of output will swap the bytes accordingly
		out.writeShort(version);
		out.writeInt(arcSize);
		out.writeShort(sectionSize);
		out.writeShort(3);
	}
}
