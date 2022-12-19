
package ctrmap.formats.pokemon.gen5.gfbtp;

import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.util.ArraysEx;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GFBTP {
		
	public int frameCount;
	
	public List<GFBTPKeyFrameGroup> groups = new ArrayList<>();
		
	public GFBTP(DataIOStream io) throws IOException {
		int kfCount = io.readInt();
	
		KeyFrame[] keyFrames = new KeyFrame[kfCount];
		for (int i = 0; i < kfCount; i++){
			KeyFrame kf = new KeyFrame();
			kf.frame = io.readUnsignedShort();
			keyFrames[i] = kf;
		}
		io.align(4);
		
		for (int i = 0; i < kfCount; i++){
			keyFrames[i].texIdx = io.read();
		}
		io.align(4);
		
		for (int i = 0; i < kfCount; i++){
			keyFrames[i].palIdx = io.read();
		}
		io.align(4);
		
		int targetCount = io.readInt();
		int[] targetOffsets = new int[targetCount + 1];
		int currentTgt;
		for (currentTgt = 0; currentTgt < targetCount; currentTgt++){
			targetOffsets[currentTgt] = io.read();
		}
		targetOffsets[currentTgt] = kfCount;
		
		io.align(4);
		
		frameCount = io.readInt();
		
		//Slice keyframes into groups
		for (int i = 0; i < targetCount; i++){
			groups.add(new GFBTPKeyFrameGroup(keyFrames, targetOffsets[i], targetOffsets[i + 1]));
		}
	}
	
	public static class GFBTPKeyFrameGroup {
		public List<KeyFrame> keyFrames;
		
		public GFBTPKeyFrameGroup(KeyFrame[] keyFrames, int offset, int endOffset){
			this.keyFrames = ArraysEx.asList(Arrays.copyOfRange(keyFrames, offset, endOffset));
		}
	}
	
	public static class KeyFrame {
		public int frame;
		public int texIdx;
		public int palIdx;
	}
}
