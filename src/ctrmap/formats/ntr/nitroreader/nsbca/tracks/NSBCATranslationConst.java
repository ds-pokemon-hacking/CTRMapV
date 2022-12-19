package ctrmap.formats.ntr.nitroreader.nsbca.tracks;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

public class NSBCATranslationConst extends NSBCATranslationTrack {
    private final float const_trans;

    public NSBCATranslationConst(NTRDataIOStream data) throws IOException {
        const_trans = data.readFX32();
    }

    public NSBCATranslationConst() {
        const_trans = 0.0f;
    }

	@Override
    public float[] getData(NTRDataIOStream data, int JAC_offset, int numframes) throws IOException {
        return new float[]{const_trans};
    }

	@Override
    public int getFrameStep() {
        return 1;
    }
}
