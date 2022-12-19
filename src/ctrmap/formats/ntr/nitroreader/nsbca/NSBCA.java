package ctrmap.formats.ntr.nitroreader.nsbca;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSG3DResource;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.access.MemoryStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSBCA extends NNSG3DResource {
	public static final String MAGIC = "BCA0";
	
    public NSBCAAnimationBlock JNT0;

    public NSBCA(NSBCAAnimationBlock JNT0) {
        this.JNT0 = JNT0;
    }
	
	public NSBCA(FSFile fsf){
		this(fsf.getIO());
	}
	
	public NSBCA(byte[] bytes){
		this(new MemoryStream(bytes));
	}
	
	public NSBCA(IOStream io){
		this(new NTRDataIOStream(io));
	}
	
	public NSBCA(NTRDataIOStream io){
		try {
			readBase(io);
			seekBlock(io, 0);
			JNT0 = new NSBCAAnimationBlock(io);
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(NSBCA.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public boolean acceptsModel(Model mdl){
		if (mdl.skeleton != null){
			for (NSBCAAnimation anm : JNT0.animations){
				if (anm.getNodes().size() > mdl.skeleton.getJoints().size()){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public int getMaxNodesCount(){
		int out = 0;
		for (NSBCAAnimation a : JNT0.animations){
			out = Math.max(out, a.getNodes().size());
		}
		return out;
	}
	
	public G3DResource toGeneric(Skeleton skl){
		G3DResource res = new G3DResource();
		res.addSklAnimes(JNT0.toGeneric(skl));
		return res;
	}
}
