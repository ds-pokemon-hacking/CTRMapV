package ctrmap.formats.ntr.nitrowriter.common.resources;

import java.io.IOException;

public abstract class NNSG3DResource extends PatriciaTreeNode {	
	public abstract byte[] getBytes() throws IOException;
}
