package ctrmap.formats.ntr.nitroreader.nsbva;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.formats.ntr.nitroreader.common.NNSG3DResource;
import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.access.MemoryStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSBVA extends NNSG3DResource {
	public static final String MAGIC = "BVA0";
	
    public NSBVAAnimationBlock VIS0;
	
	public NSBVA(byte[] bytes) {
		this(new MemoryStream(bytes));
	}
	
	public NSBVA(FSFile fsf) {
		this(fsf.getIO());
	}
	
	public NSBVA(IOStream io){
		this(new NTRDataIOStream(io));
	}
	
	public NSBVA(NTRDataIOStream io){
		try {
			readBase(io);
			seekBlock(io, 0);
			VIS0 = new NSBVAAnimationBlock(io);
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(NSBVA.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public boolean acceptsModel(Model mdl){
		if (mdl != null && mdl.skeleton != null){
			int jc = mdl.skeleton.getJoints().size();
			for (NSBVAAnimation a : VIS0.animations){
				if (a.tracks.size() > jc){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public G3DResource toGeneric(Skeleton skl){
		G3DResource res = new G3DResource();
		
		for (NSBVAAnimation a : VIS0.animations){
			res.addVisAnime(a.toGeneric(skl));
		}
		
		return res;
	}
}
