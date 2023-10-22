
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements;

import java.io.DataOutput;
import java.io.IOException;

public class ScaFX16Elem extends AbstractScaleElement{

	public ScaFX16Elem(float value) {
		super(value);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeShort(value_fx);
		out.writeShort(invValue_fx);
	}
}
