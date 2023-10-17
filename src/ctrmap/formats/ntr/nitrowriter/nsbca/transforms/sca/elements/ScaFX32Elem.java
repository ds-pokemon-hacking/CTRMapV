package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements;

import java.io.DataOutput;
import java.io.IOException;

public class ScaFX32Elem extends AbstractScaleElement {

	public ScaFX32Elem(float value) {
		super(value);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(value_fx);
		out.writeInt(0);
	}
}
