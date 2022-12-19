
package ctrmap.formats.ntr.nitrowriter.nsbta.transforms;

import ctrmap.formats.ntr.nitrowriter.common.resources.PatriciaTreeNode;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import ctrmap.renderer.scene.animation.material.MaterialAnimationFrame;
import ctrmap.renderer.util.AnimeProcessor;
import java.util.ArrayList;
import java.util.List;

public class SRT0Transform extends PatriciaTreeNode {
	
	public static final int BYTES = 40;
	
	public SRTTransformTrack sx;
	public SRTTransformTrack sy;
	public SRTTransformTrack tx;
	public SRTTransformTrack ty;
	public SRTTransformTrack r;
	
	public SRT0Transform(MatAnimBoneTransform bt, int frameCount){
		name = bt.name;
		
		List<MaterialAnimationFrame> bakedAnimation = new ArrayList<>();
		for (int i = 0; i <= frameCount; i++){
			bakedAnimation.add(bt.getFrame(i, 0));
		}
		
		sx = new SRTTransformTrack(SRTTransformTrack.SRTTrackType.SX, bakedAnimation, AnimeProcessor.checkConstant(bt.msx[0]));
		sy = new SRTTransformTrack(SRTTransformTrack.SRTTrackType.SY, bakedAnimation, AnimeProcessor.checkConstant(bt.msy[0]));
		tx = new SRTTransformTrack(SRTTransformTrack.SRTTrackType.TX, bakedAnimation, AnimeProcessor.checkConstant(bt.mtx[0]));
		ty = new SRTTransformTrack(SRTTransformTrack.SRTTrackType.TY, bakedAnimation, AnimeProcessor.checkConstant(bt.mty[0]));
		r  = new SRTRotationTrack (SRTTransformTrack.SRTTrackType.R,  bakedAnimation, AnimeProcessor.checkConstant(bt.mrot[0]));
	}
}
