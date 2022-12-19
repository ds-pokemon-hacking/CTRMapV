package ctrmap.formats.ntr.nitroreader.nsbta;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.formats.ntr.nitroreader.common.NNSG3DResource;
import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.access.MemoryStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSBTA extends NNSG3DResource {
	public static final String MAGIC = "BTA0";
	
    public NSBTAAnimationBlock SRT0;

    public NSBTA(NSBTAAnimationBlock SRT0) {
        this.SRT0 = SRT0;
    }
	
	public NSBTA(FSFile fsf) {
		this(fsf.getIO());
	}
	
	public NSBTA(byte[] bytes){
		this(new MemoryStream(bytes));
	}
	
	public NSBTA(IOStream io){
		this(new NTRDataIOStream(io));
	}
	
	public NSBTA(NTRDataIOStream io){
		try {
			readBase(io);
			seekBlock(io, 0);
			SRT0 = new NSBTAAnimationBlock(io);
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(NSBTA.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public G3DResource toGeneric(){
		G3DResource res = new G3DResource();
		res.materialAnimations.addAll(SRT0.toGeneric());
		return res;
	}
}
