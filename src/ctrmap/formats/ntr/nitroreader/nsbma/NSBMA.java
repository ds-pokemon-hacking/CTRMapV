package ctrmap.formats.ntr.nitroreader.nsbma;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSG3DResource;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSBMA extends NNSG3DResource {

	public static final String MAGIC = "BMA0";

	public NSBMAAnimationBlock MAT0;

	public NSBMA(NSBMAAnimationBlock MAT0) {
		this.MAT0 = MAT0;
	}

	public NSBMA(FSFile fsf) {
		this(fsf.getIO());
	}

	public NSBMA(IOStream io) {
		this(new NTRDataIOStream(io));
	}
	
	public NSBMA(NTRDataIOStream io) {
		try {
			readBase(io);
			seekBlock(io, 0);
			MAT0 = new NSBMAAnimationBlock(io);
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(NSBMA.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public G3DResource toGeneric() {
		G3DResource res = new G3DResource();
		res.addMatAnimes(MAT0.toGeneric());
		return res;
	}
}
