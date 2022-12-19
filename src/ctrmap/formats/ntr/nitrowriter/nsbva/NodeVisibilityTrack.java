
package ctrmap.formats.ntr.nitrowriter.nsbva;

import ctrmap.renderer.scene.animation.visibility.VisibilityBoneTransform;
import xstandard.io.util.BitWriter;
import java.io.IOException;

public class NodeVisibilityTrack {
	
	private boolean[] values;
	
	public NodeVisibilityTrack(VisibilityBoneTransform visBT, int frameCount){
		values = new boolean[frameCount];
		for (int i = 0; i <= frameCount; i++){
			values[i] = visBT.isVisible(i);
		} 
	}
	
	public void write(BitWriter writer, int frame) throws IOException {
		writer.writeBit(values[frame]);
	}
}
