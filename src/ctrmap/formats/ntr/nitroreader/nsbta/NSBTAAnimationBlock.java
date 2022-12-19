package ctrmap.formats.ntr.nitroreader.nsbta;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import java.util.ArrayList;
import java.util.List;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import java.io.IOException;

public class NSBTAAnimationBlock {
    public List<NSBTAAnimation> animations;

    public NSBTAAnimationBlock(NTRDataIOStream data) throws IOException {
        data.setBaseHere();
        String magic = data.readPaddedString(4);
        int blockSize = data.readInt();
		animations = NNSPatriciaTreeReader.processOffsetTree(data, (io, entry) -> {
			return new NSBTAAnimation(data, entry.getName());
		});
		data.resetBase();
    }
	
	public List<MaterialAnimation> toGeneric(){
		List<MaterialAnimation> l = new ArrayList<>();
		for (NSBTAAnimation a : animations){
			l.add(a.toGeneric());
		}
		return l;
	}
}
