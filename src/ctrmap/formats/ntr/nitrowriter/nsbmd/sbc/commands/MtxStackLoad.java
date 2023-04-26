
package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands;

import java.io.DataOutput;
import java.io.IOException;

public class MtxStackLoad extends RendererCommand {
	public int stackIdx;
	
	public MtxStackLoad(int index) {
		stackIdx = index;
	}

	@Override
	public SBCOpCode getOpCode() {
		return SBCOpCode.MATRIX_LOAD;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		out.write(stackIdx);
	}
}
