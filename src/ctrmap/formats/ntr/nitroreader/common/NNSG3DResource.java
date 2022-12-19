package ctrmap.formats.ntr.nitroreader.common;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

public class NNSG3DResource {

	protected NNSG3DResourceHeader header;

	protected final void readBase(NTRDataIOStream io) throws IOException {
		header = new NNSG3DResourceHeader(io);
	}

	protected final boolean seekBlock(NTRDataIOStream io, int index) throws IOException {
		if (index < header.offsets.length) {
			io.seek(header.offsets[index]);
			return true;
		}
		return false;
	}
}
