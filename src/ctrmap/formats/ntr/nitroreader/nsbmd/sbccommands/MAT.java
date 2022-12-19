package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;
import java.io.IOException;
import xstandard.math.BitMath;

public class MAT extends SBCCommand {
    private final int matIdx;
	private final boolean isForReuse;
	private final boolean isReused;

    public MAT(NTRDataIOStream data, int flags) throws IOException {
        matIdx = data.read();
		//0x2066584/ARM9,W2U: setting these flags stores the
		//material into a global state for faster repeated access
		//this is not mandatory, but it makes rendering faster
		//beware that this will result in UB with material indices over 63
		//since the state bit is written into a 2*32bit array
		isReused = BitMath.checkIntegerBit(flags, 0);
		isForReuse = BitMath.checkIntegerBit(flags, 1);
    }

    public int getMatID() {
        return matIdx;
    }

	@Override
	public void toGeneric(NSBMDModelConverter conv) {
		conv.applyMaterial(matIdx);
	}
}
