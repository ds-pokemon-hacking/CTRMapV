package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.elements;

import java.io.DataOutput;
import java.io.IOException;

public interface TranslationElement {
	public void write(DataOutput out) throws IOException;
}
