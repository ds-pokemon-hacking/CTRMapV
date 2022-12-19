package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;
import java.io.IOException;

public class CALLDL extends SBCCommand {
    private int dlOffset;
    private int dlSize;

    public CALLDL(NTRDataIOStream data) throws IOException {
        dlOffset = data.readInt();
        dlSize = data.readInt();
    }

	@Override
	public void toGeneric(NSBMDModelConverter conv) {
	}
}
