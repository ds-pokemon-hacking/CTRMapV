
package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands;

import java.io.DataOutput;
import java.io.IOException;

public class NodeMtxApply extends RendererCommand {
	public int srcMtx = -1;
	public int dstMtx = -1;
	
	public int jntId;
	public int parentJntId = 0;
	
	public NodeMtxApply(int jntId) {
		this(jntId, jntId);
		//There are no defined rules as far as I know to handle root bone IDs in this format
		//However, the only time they are used are on the 1 forced root bone, which always has
		//ID 0 and parent ID also 0. Thus I assume that if the ID=PID, it means that the bone does not have a
		//parent.
	}
	
	public NodeMtxApply(int jntId, int parentJntId) {
		this.jntId = jntId;
		this.parentJntId = parentJntId;
	}

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
