package ctrmap.formats.ntr.nitroreader.nsbtx;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSG3DResource;
import xstandard.fs.FSFile;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSBTX extends NNSG3DResource {

	public static final String MAGIC = "BTX0";

	public NSBTXDataBlock TEX0;

	public NSBTX(NSBTXDataBlock TEX0) {
		this.TEX0 = TEX0;
	}

	public NSBTX(FSFile fsf) {
		this(new NTRDataIOStream(fsf.getIO()), true);
	}

	public NSBTX(NTRDataIOStream reader) {
		this(reader, false);
	}

	private NSBTX(NTRDataIOStream io, boolean close) {
		try {
			readBase(io);
			seekBlock(io, 0);
			TEX0 = new NSBTXDataBlock(io);
			if (close) {
				io.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(NSBTX.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public NSBTX(byte[] data) {
		this(new NTRDataIOStream(data), true);
	}

	public static NSBTX readFromBytes(byte[] bytes) {
		return new NSBTX(new NTRDataIOStream(bytes), true);
	}
}
