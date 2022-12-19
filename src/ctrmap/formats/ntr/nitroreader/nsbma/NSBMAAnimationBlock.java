package ctrmap.formats.ntr.nitroreader.nsbma;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import java.util.ArrayList;
import java.util.List;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import java.io.IOException;

public class NSBMAAnimationBlock {

	public List<NSBMAAnimation> animations;

	public NSBMAAnimationBlock(NTRDataIOStream data) throws IOException {
		data.setBaseHere();
		String magic = data.readPaddedString(4);
		int size = data.readInt();
		animations = NNSPatriciaTreeReader.processOffsetTree(data, (io, entry) -> {
			return new NSBMAAnimation(data, entry.getName());
		});
		data.resetBase();
	}

	public List<MaterialAnimation> toGeneric() {
		List<MaterialAnimation> l = new ArrayList<>();
		for (NSBMAAnimation a : animations) {
			l.add(a.toGeneric());
		}
		return l;
	}
}
