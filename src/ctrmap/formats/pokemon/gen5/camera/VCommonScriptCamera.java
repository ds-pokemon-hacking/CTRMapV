
package ctrmap.formats.pokemon.gen5.camera;

import ctrmap.formats.ntr.common.FXIO;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VCommonScriptCamera {
	
	public float pitch;
	public float distance;
	public Vec3f targetPos;
	
	public int animationLength;
	
	public int param34;
	public int param38;
	public int param3C;
	public int param40;
	public int param44;
	
	public VCommonScriptCamera(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();
			pitch = FXIO.readFX32(in);
			distance = FXIO.readFX32(in);
			targetPos = FXIO.readVecFX32(in);
			animationLength = in.readInt();
			param34 = in.readInt();
			param38 = in.readInt();
			param3C = in.readInt();
			param40 = in.readInt();
			param44 = in.readInt();
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(VCommonScriptCamera.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
