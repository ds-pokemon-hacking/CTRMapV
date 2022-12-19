
package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands;

import java.io.DataOutput;
import java.io.IOException;

public class NodeMtxApply extends RendererCommand {
	public int srcMtx = -1;
	public int dstMtx = -1;
	
	public int jntId;
	public int parentJntId = 0;

	@Override
	public SBCOpCode getOpCode() {
		return SBCOpCode.MATRIX_JOINT;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		out.write(jntId);
		out.write(parentJntId);
		out.write(0);
		if (dstMtx != -1){
			out.write(dstMtx);
		}
		if (srcMtx != -1){
			out.write(srcMtx);
		}
	}

	@Override
	public int calcFlags() {
		return (srcMtx != -1 ? 2 : 0) | (dstMtx != -1 ? 1 : 0);
	}
}
