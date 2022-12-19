package ctrmap.formats.ntr.nitroreader.nsbca.tracks;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

public class NSBCAScaleConst extends NSBCAScaleTrack {
    private final float const_invScale;
    private final float const_scale;

    public NSBCAScaleConst(NTRDataIOStream data) throws IOException {
        this.const_scale = data.readFX32();
        this.const_invScale = data.readFX32();
    }

    public NSBCAScaleConst() {
        this.const_scale = 1.0f;
        this.const_invScale = 1.0f;
    }

	@Override
    public float[] getData(NTRDataIOStream data, int JAC_offset, int numframes) {
        return new float[]{this.const_scale};
    }

	@Override
    public int getFrameStep() {
        return 1;
    }
}
