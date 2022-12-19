
package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands;

import java.io.DataOutput;
import java.io.IOException;

public class EndSBC extends RendererCommand {

	@Override
	public SBCOpCode getOpCode() {
		return SBCOpCode.RETURN;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		
	}

}
