
package ctrmap.formats.pokemon.gen5.sequence;

import java.io.DataInput;
import java.io.IOException;

/**
 *
 */
public class SeqHeader {
	public static final String RS_MAGIC = "SEQ0";
	
	public int seqId;
	
	public int paramsPtr;
	public int commandsPtr;
	public int endCommandsPtr;
	public int resourcesPtr;
	public int resourcesCount;
	
	public SeqHeader(int seqId, DataInput in) throws IOException {
		this.seqId = seqId;
		paramsPtr = in.readInt();
		commandsPtr = in.readInt();
		endCommandsPtr = in.readInt();
		resourcesPtr = in.readInt();
		resourcesCount = in.readInt();
	}
}
