
package ctrmap.formats.ntr.nitrowriter.common.resources.animation;

import ctrmap.formats.ntr.common.FX;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class FX32Elem {
	protected int value_fx;
	
	public FX32Elem(float f){
		value_fx = FX.fx32(f);
	}

	public void write(DataOutput out) throws IOException {
		out.writeInt(value_fx);
	}
}
