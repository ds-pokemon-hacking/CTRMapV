
package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands;

import java.io.DataOutput;
import java.io.IOException;

public class MaterialApply extends RendererCommand {

	private int matId;
	
	public MaterialApply(int matId){
		this.matId = matId;
	}
	
	@Override
	public int calcFlags() {
		//It seems like there is some usage of the flags when a material is used more than once
		//However, our writer sorts all meshes by materials, so such a scenario should not occur here
		return 0;
	}

	@Override
	public SBCOpCode getOpCode() {
		return SBCOpCode.APPLY_MATERIAL;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		out.write(matId);
	}

}
