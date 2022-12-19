package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;
import java.io.IOException;

public class SHP extends SBCCommand {
    private int meshIdx;

    public SHP(NTRDataIOStream data) throws IOException {
        meshIdx = data.read();
    }

	@Override
	public void toGeneric(NSBMDModelConverter conv) {
		conv.applyMesh(meshIdx);
	}
}
