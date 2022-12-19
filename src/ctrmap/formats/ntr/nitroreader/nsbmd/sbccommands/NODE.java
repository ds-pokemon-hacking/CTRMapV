package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;
import java.io.IOException;

public class NODE extends SBCCommand {
    private int visgroupId;
    boolean defaultVisibility;

    public NODE(NTRDataIOStream data) throws IOException {
        visgroupId = data.read();
        defaultVisibility = data.readBoolean();
    }

	@Override
	public void toGeneric(NSBMDModelConverter conv) {
		conv.setVisGroup(visgroupId, defaultVisibility);
	}
}
