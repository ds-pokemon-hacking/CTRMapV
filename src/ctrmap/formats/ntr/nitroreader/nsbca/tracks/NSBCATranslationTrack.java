package ctrmap.formats.ntr.nitroreader.nsbca.tracks;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

public abstract class NSBCATranslationTrack {
    public abstract float[] getData(NTRDataIOStream io, int j, int i) throws IOException;

    public abstract int getFrameStep();
}
