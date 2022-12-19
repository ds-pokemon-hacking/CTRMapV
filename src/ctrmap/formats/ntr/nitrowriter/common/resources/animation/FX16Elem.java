
package ctrmap.formats.ntr.nitrowriter.common.resources.animation;

import ctrmap.formats.ntr.common.FX;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class FX16Elem {
	protected short value_fx;
	
	public FX16Elem(float f){
		value_fx = FX.fx16(f);
	}

	public void write(DataOutput out) throws IOException {
		out.writeShort(value_fx);
	}
}
