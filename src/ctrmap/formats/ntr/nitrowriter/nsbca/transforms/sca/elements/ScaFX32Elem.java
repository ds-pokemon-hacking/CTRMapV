
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements;

import ctrmap.formats.ntr.nitrowriter.common.resources.animation.FX32Elem;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class ScaFX32Elem extends FX32Elem implements ScaleElement {	
	public ScaFX32Elem(float f){
		super(f);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(value_fx);
		out.writeInt(0);
	}
}
