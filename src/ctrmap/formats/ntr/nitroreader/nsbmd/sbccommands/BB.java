package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;
import ctrmap.renderer.scene.model.Joint;
import java.io.IOException;
import xstandard.math.BitMath;

public class BB extends SBCCommand {
    
    private int unusedJointIndex;
    private int srcStackPos = -1;
	private int dstStackPos = -1;

    public BB(NTRDataIOStream data, int flags) throws IOException {
        unusedJointIndex = data.read();
        if (BitMath.checkIntegerBit(flags, 0)) {
            dstStackPos = data.read() & 31;
        }
        if (BitMath.checkIntegerBit(flags, 1)) {
            srcStackPos = data.read() & 31;
        }
    }

	@Override
	public void toGeneric(NSBMDModelConverter conv) {
		Joint j = conv.getCurrentSingleJoint();
		if (j != null) {
			j.flags = Joint.BB_AXIS_X | Joint.BB_AXIS_Y | Joint.BB_AXIS_Z;
		}
	}
}
