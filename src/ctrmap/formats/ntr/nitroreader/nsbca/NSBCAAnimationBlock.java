package ctrmap.formats.ntr.nitroreader.nsbca;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import java.util.ArrayList;
import java.util.List;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.model.Skeleton;
import java.io.IOException;

public class NSBCAAnimationBlock {
    public List<NSBCAAnimation> animations;

    public NSBCAAnimationBlock(NTRDataIOStream data) throws IOException {
        data.setBaseHere();
        String magic = data.readPaddedString(4);
        int size = data.readInt();
		animations = NNSPatriciaTreeReader.processOffsetTree(data, (io, entry) -> {
			return new NSBCAAnimation(data, entry.getName());
		});
		data.resetBase();
    }
	
	public List<SkeletalAnimation> toGeneric(Skeleton skeleton){
		List<SkeletalAnimation> l = new ArrayList<>();
		for (NSBCAAnimation a : animations){
			l.add(a.toGeneric(skeleton));
		}
		return l;
	}
}
