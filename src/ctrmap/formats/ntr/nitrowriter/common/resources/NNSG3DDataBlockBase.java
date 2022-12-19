
package ctrmap.formats.ntr.nitrowriter.common.resources;

import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryValue;
import java.io.IOException;

/**
 *
 */
public class NNSG3DDataBlockBase implements NNSG3DDataBlock {
	private final String signature;
	
	public NNSG3DResourceTree tree = new NNSG3DResourceTree();
	
	public NNSG3DDataBlockBase(String signature){
		this.signature = signature;
	}
	
	@Override
	public byte[] getData() throws IOException {
		DataIOStream out = new DataIOStream();
		out.writeStringUnterminated(signature);
		TemporaryValue len = new TemporaryValue(out);
		tree.write(out);
		len.set(out.getLength());
		return out.toByteArray();
	}
}
