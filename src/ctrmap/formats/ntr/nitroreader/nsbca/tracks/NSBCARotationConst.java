package ctrmap.formats.ntr.nitroreader.nsbca.tracks;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;
import org.joml.Matrix3f;

public class NSBCARotationConst extends NSBCARotationTrack {
    private final int const_offset;

    public NSBCARotationConst(NTRDataIOStream data) throws IOException {
        const_offset = data.readInt();
    }

    public NSBCARotationConst() {
        const_offset = -1;
    }

	@Override
    public Matrix3f[] getData(NTRDataIOStream data, int JAC_offset, int ofsRot3, int ofsRot5, int numframes) throws IOException {
        if (const_offset == -1) {
            return new Matrix3f[]{new Matrix3f()};
        }
        data.seek(const_offset + JAC_offset);
        Matrix3f[] fArr = new Matrix3f[1];
        fArr[0] = readMatrix(data, JAC_offset, ofsRot3, ofsRot5, const_offset);
        return fArr;
    }

	@Override
    public int getFrameStep() {
        return 1;
    }
}
