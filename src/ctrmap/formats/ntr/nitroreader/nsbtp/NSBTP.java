package ctrmap.formats.ntr.nitroreader.nsbtp;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.formats.ntr.nitroreader.common.NNSG3DResource;
import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.access.MemoryStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSBTP extends NNSG3DResource {

	public static final String MAGIC = "BTP0";

	public NSBTPAnimationBlock PAT0;

	public NSBTP(NSBTPAnimationBlock pat0) {
		this.PAT0 = pat0;
	}

	public NSBTP(FSFile fsf) {
		this(fsf.getIO());
	}

	public NSBTP(byte[] bytes) {
		this(new MemoryStream(bytes));
	}

	public NSBTP(IOStream io) {
		this(new NTRDataIOStream(io));
	}
	
	public NSBTP(NTRDataIOStream io) {
		try {
			readBase(io);
			seekBlock(io, 0);
			PAT0 = new NSBTPAnimationBlock(io);
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(NSBTP.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public G3DResource toGeneric() {
		G3DResource res = new G3DResource();
		res.materialAnimations.addAll(PAT0.toGeneric());
		return res;
	}
}
