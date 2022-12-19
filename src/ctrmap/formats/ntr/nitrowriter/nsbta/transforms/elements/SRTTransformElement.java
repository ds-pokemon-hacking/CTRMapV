package ctrmap.formats.ntr.nitrowriter.nsbta.transforms.elements;

import java.io.DataOutput;
import java.io.IOException;

public interface SRTTransformElement {

	public void write(DataOutput out) throws IOException;
}
