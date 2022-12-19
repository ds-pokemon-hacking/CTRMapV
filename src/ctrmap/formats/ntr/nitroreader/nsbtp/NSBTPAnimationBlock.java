package ctrmap.formats.ntr.nitroreader.nsbtp;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.util.ArrayList;
import java.util.List;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import java.io.IOException;

public class NSBTPAnimationBlock {
    public List<NSBTPAnimation> animations;

    public NSBTPAnimationBlock(NTRDataIOStream data) throws IOException {
        data.setBaseHere();
        String magic = data.readPaddedString(4);
        long blockSize = data.readInt();
        animations = NNSPatriciaTreeReader.processOffsetTree(data, (io, entry) -> {
			return new NSBTPAnimation(data, entry.getName());
		});
		data.resetBase();
    }
	
	public List<MaterialAnimation> toGeneric(){
		List<MaterialAnimation> l = new ArrayList<>();
		for (NSBTPAnimation a : animations){
			l.add(a.toGeneric());
		}
		return l;
	}
}
