package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;
import java.io.IOException;

public class MTX extends SBCCommand {
    private int stackIndex;

    public MTX(NTRDataIOStream data) throws IOException {
        stackIndex = data.read();
    }

	@Override
	public void toGeneric(NSBMDModelConverter conv) {
		conv.loadMatrix(stackIndex);
	}
}
