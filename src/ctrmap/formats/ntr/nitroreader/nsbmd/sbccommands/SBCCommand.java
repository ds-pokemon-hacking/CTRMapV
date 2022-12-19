package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;

public abstract class SBCCommand {

    public enum SBCOpCode {
        NOP,
        RET,
        NODE,
        MTX,
        MAT,
        SHP,
        NODEDESC,
        BB,
        BBY,
        NODEMIX,
        CALLDL,
        POSSCALE,
        ENVMAP,
        PRJMAP;
        
		private static final SBCOpCode[] values = values();

        public int getOpcode() {
            return ordinal();
        }

        public static SBCOpCode valueOf(int opcode) {
            return values[opcode];
        }
    }
	
	public abstract void toGeneric(NSBMDModelConverter conv);
}
