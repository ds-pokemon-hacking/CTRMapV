
package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands;

import java.io.DataOutput;
import java.io.IOException;

public class PosScaleMul extends RendererCommand {

	public boolean inv;
	
	public PosScaleMul(boolean inv){
		this.inv = inv;
	}
	
	@Override
	public int calcFlags() {
		return inv ? 1 : 0;
	}

	@Override
	public SBCOpCode getOpCode() {
		return SBCOpCode.SCALE_GLOBAL;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		
	}

}
