package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements;

import java.io.DataOutput;
import java.io.IOException;

public interface ScaleElement {
	public void write(DataOutput out) throws IOException;
}
