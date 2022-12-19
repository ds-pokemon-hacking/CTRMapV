package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.elements;

import java.io.DataOutput;
import java.io.IOException;

public interface RotationElement {
	public void write(DataOutput out) throws IOException;
}
