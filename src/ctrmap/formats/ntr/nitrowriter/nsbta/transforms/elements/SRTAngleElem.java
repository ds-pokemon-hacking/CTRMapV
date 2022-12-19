
package ctrmap.formats.ntr.nitrowriter.nsbta.transforms.elements;

import ctrmap.formats.ntr.common.FX;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class SRTAngleElem implements SRTTransformElement {

	private short fx_sin;
	private short fx_cos;
	
	public SRTAngleElem(float angle){
		fx_sin = FX.fx16((float)Math.sin(angle));
		fx_cos = FX.fx16((float)Math.cos(angle));
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeShort(fx_sin);
		out.writeShort(fx_cos);
	}

}
