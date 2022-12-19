package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;

public class POSSCALE extends SBCCommand {
    private boolean inv;

    public POSSCALE(int flags) {
        inv = flags == 1;
    }

	@Override
	public void toGeneric(NSBMDModelConverter conv) {
		conv.setGlobalScale(inv);
	}
}
