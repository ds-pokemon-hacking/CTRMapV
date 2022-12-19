package ctrmap.formats.ntr.narc.blocks;

import xstandard.io.InvalidMagicException;
import xstandard.io.util.StringIO;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FIMG {

	public static final String FIMG_MAGIC = "GMIF";
	public static final int HEADER_BYTES = 8;

	public int partitionSize;

	public FIMG(DataInput in) throws IOException {
		if (!StringIO.checkMagic(in, FIMG_MAGIC)) {
			throw new InvalidMagicException("Bad FIMG magic.");
		}
		partitionSize = in.readInt();
	}

	public void write(DataOutput out) throws IOException {
		StringIO.writeStringUnterminated(out, FIMG_MAGIC);
		out.writeInt(partitionSize);
	}
}
