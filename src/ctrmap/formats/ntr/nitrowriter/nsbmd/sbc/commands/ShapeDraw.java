
package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands;

import java.io.DataOutput;
import java.io.IOException;

public class ShapeDraw extends RendererCommand {

	public int shpId;
	
	public ShapeDraw(int shpId){
		this.shpId = shpId;
	}

	@Override
	public SBCOpCode getOpCode() {
		return SBCOpCode.DRAW_MESH;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		out.write(shpId);
	}

}
