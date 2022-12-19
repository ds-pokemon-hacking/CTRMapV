package ctrmap.formats.ntr.nitrowriter.common.resources;

import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class PatriciaTreeNode {

	public String name;

	public int refBit;
	public int lidx;
	public int ridx;
	public int dataEntryNo;

	public void writeNode(DataOutput out) throws IOException {
		out.write(refBit);
		out.write(lidx);
		out.write(ridx);
		out.write(dataEntryNo);
	}

	@Override
	public String toString() {
		return name;
	}
}
