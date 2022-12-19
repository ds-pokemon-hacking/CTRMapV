
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements;

import ctrmap.formats.ntr.nitrowriter.common.resources.animation.FX16Elem;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class ScaFX16Elem extends FX16Elem implements ScaleElement {

	public ScaFX16Elem(float f) {
		super(f);
	}
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeShort(value_fx);
		out.writeShort(0);
	}
}
