
package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands;

import java.io.DataOutput;
import java.io.IOException;

public class VisGroupApply extends RendererCommand {

	public int visGroupId;
	public boolean defaultVisibility;
	
	public VisGroupApply(int visGroupId, boolean defaultVisibility){
		this.visGroupId = visGroupId;
		this.defaultVisibility = defaultVisibility;
	}

	@Override
	public SBCOpCode getOpCode() {
		return SBCOpCode.SET_VISGROUP;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		out.write(visGroupId);
		out.writeBoolean(defaultVisibility);
	}

}
