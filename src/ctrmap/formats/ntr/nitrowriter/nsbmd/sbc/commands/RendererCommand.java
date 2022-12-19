
package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands;

import java.io.DataOutput;
import java.io.IOException;

public abstract class RendererCommand {	
	public int calcFlags() {
		return 0;
	}
	public abstract SBCOpCode getOpCode();
	public abstract void writeParams(DataOutput out) throws IOException;
}
