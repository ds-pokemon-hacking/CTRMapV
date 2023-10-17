package ctrmap.formats.ntr.nitrowriter.nsbca.transforms;

import java.util.List;

/**
 *
 */
public abstract class TransformTrack {

	protected SkeletalTransformComponent comp;
	
	public int startFrame;
	public int endFrame;
	
	public boolean isConstant = false;
	public boolean isBindPose = false;
	public boolean isIdentity = false;
	
	public TransformTrack(SkeletalTransformComponent component, List<TransformFrame> frames){
		this.comp = component;
		startFrame = 0;
		endFrame = frames.size(); 
		//for now we just force it like this. I have yet to study the behavior of the frames before and after the start/end. Do they get clamped or ignored?
	}

	public final void setIsBindPose() {
		isBindPose = true;
		isConstant = true;
		isIdentity = false;
	}
	
	public final void setIsIdentity() {
		isBindPose = false;
		isConstant = true;
		isIdentity = true;
	}

	protected abstract int getInfoImpl();
	
	public int getInfo() {
		int flags = getInfoImpl();
		flags |= startFrame;
		flags |= endFrame << 16;
		return flags;
	}
}
