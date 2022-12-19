
package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeMtxBlendApply extends RendererCommand {

	public int dstIdx;
	
	public List<NodeMtxBlendSource> sources = new ArrayList<>();

	@Override
	public SBCOpCode getOpCode() {
		return SBCOpCode.MATRIX_SMOOTH_SKINNING;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		out.write(dstIdx);
		out.write(sources.size());
		
		for (NodeMtxBlendSource src : sources){
			out.write(src.srcIdx);
			out.write(src.jntId);
			out.write(src.weight);
		}
	}

	public static class NodeMtxBlendSource {
		public int srcIdx;
		public int jntId;
		public short weight;
	}
}
