package ctrmap.formats.ntr.nitroreader.nsbca.tracks;

import ctrmap.formats.ntr.nitroreader.common.NNSAnimationTrackFlags;
import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.Utils;
import java.io.IOException;
import org.joml.Matrix3f;

public class NSBCARotationCurve extends NSBCARotationTrack {
    private int flags;
    private int offset;
	
    public NSBCARotationCurve(NTRDataIOStream data) throws IOException {
        this.flags = data.readInt();
        this.offset = data.readInt();
    }

	@Override
    public Matrix3f[] getData(NTRDataIOStream data, int JAC_offset, int ofsRot3, int ofsRot5, int numFrame) throws IOException {
        int valueCount = Utils.getValueCount(numFrame, getFrameStep());
        
		data.seek(offset + JAC_offset);
		
		Matrix3f[] rotation_matrices = new Matrix3f[valueCount];
        for (int i = 0; i < valueCount; i++) {
            rotation_matrices[i] = readMatrix(data, JAC_offset, ofsRot3, ofsRot5, data.readUnsignedShort());
        }
		
        return rotation_matrices;
    }

	@Override
    public int getFrameStep() {
        return NNSAnimationTrackFlags.getFrameStep(flags);
    }
}
