
package ctrmap.formats.ntr.nitrowriter.nsbta.transforms;

import static ctrmap.formats.ntr.nitrowriter.nsbta.SRTAnimationResource.setFlagIf;
import ctrmap.formats.ntr.nitrowriter.nsbta.transforms.elements.SRTAngleElem;
import ctrmap.renderer.scene.animation.material.MaterialAnimationFrame;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class SRTRotationTrack extends SRTTransformTrack {
	
	public SRTRotationTrack(SRTTrackType type, List<MaterialAnimationFrame> bakedAnimation, boolean constant) {
		super(type, constant);
		isFX16 = false;
		
		//as far as I know, rotation elements can only be 4 bytes (two fx16s)
		if (constant){
			elements.add(new SRTAngleElem(bakedAnimation.get(0).r.value));
		}
	}
	
	@Override
	public TemporaryOffset writeTrackInfo(DataIOStream out) throws IOException {
		int flags = setFlagIf(0, MaterialCoordAnimationFlags.TRACK_IS_CONSTANT, constant);
		out.writeInt(flags);
		if (constant){
			elements.get(0).write(out);
		}
		else {
			return new TemporaryOffset(out);
		}
		return null;
	}
}
