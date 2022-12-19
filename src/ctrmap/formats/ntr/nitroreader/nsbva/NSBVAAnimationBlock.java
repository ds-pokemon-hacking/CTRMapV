package ctrmap.formats.ntr.nitroreader.nsbva;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.util.List;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import java.io.IOException;

public class NSBVAAnimationBlock {
    public List<NSBVAAnimation> animations;

    public NSBVAAnimationBlock(NTRDataIOStream data) throws IOException {
        data.setBaseHere();
        String magic = data.readPaddedString(4);
        long size = data.readInt();
		animations = NNSPatriciaTreeReader.processOffsetTree(data, (io, entry) -> {
			return new NSBVAAnimation(data, entry.getName());
		});
		data.resetBase();
    }
}
