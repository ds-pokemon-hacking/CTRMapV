package ctrmap.formats.ntr.nitroreader.nsbca.tracks;

import ctrmap.formats.ntr.nitroreader.common.NNSAnimationTrackFlags;
import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.Utils;
import java.io.IOException;

public class NSBCATranslationCurve extends NSBCATranslationTrack {
    private int flags;
    private int offset;

    public NSBCATranslationCurve(NTRDataIOStream data) throws IOException {
        this.flags = data.readInt();
        this.offset = data.readInt();
    }

	@Override
    public float[] getData(NTRDataIOStream data, int JAC_offset, int numFrame) throws IOException {
        boolean isFX16 = NNSAnimationTrackFlags.isFX16Skl(flags);
		
        int valueCount = Utils.getValueCount(numFrame, getFrameStep());
        float[] values = new float[valueCount];
        data.seek(offset + JAC_offset);
		
        if (isFX16) {
            for (int i = 0; i < valueCount; i++) {
                values[i] = data.readFX16();
            }
        } else {
            for (int i = 0; i < valueCount; i++) {
                values[i] = data.readFX32();
            }
        }
        return values;
    }

	@Override
    public int getFrameStep() {
        return NNSAnimationTrackFlags.getFrameStep(flags);
    }
}
