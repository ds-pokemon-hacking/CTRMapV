
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms;

import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.RotationTrack;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.ScaleTrack;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.TranslationTrack;

/**
 *
 */
public class BakedTransform {
	
	public int jointId;
	
	public boolean nonexistent = false;
	
	public ScaleTrack sx;
	public ScaleTrack sy;
	public ScaleTrack sz;
	public RotationTrack rotation;
	public TranslationTrack tx;
	public TranslationTrack ty;
	public TranslationTrack tz;
	
	public BakedTransform(int jointId) {
		this.jointId = jointId;
	}
	
	public boolean isBindR(){
		return nonexistent || rotation.isBindPose;
	}
	
	public boolean isBindT(){
		return nonexistent || (tx.isBindPose && ty.isBindPose && tz.isBindPose);
	}
	
	public boolean isBindS(){
		return nonexistent || (sx.isBindPose && sy.isBindPose && sz.isBindPose);
	}
}
