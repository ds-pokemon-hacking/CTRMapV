
package ctrmap.formats.pokemon.gen5.sequence;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import ctrmap.formats.pokemon.gen5.sequence.commands.SeqCommandObject;
import ctrmap.formats.pokemon.gen5.sequence.commands.SeqCommandObjectFactory;

/**
 *
 */
public class SeqCmd {
	public SeqOpCode opCode;
	public int startFrame;
	
	public int[] userData = new int[8];
	
	public SeqCmd(DataInput in) throws IOException {
		opCode = SeqOpCode.values()[in.readInt()];
		startFrame = in.readInt();
		
		for (int i = 0; i < userData.length; i++){
			userData[i] = in.readInt();
		}
	}
	
	public SeqCmd(SeqOpCode opCode, int frame){
		this.opCode = opCode;
		this.startFrame = frame;
	}
	
	public SeqCommandObject createCommandObject(){
		return SeqCommandObjectFactory.createSeqCommandObject(this);
	}
	
	public void write(DataOutput out) throws IOException {
		out.writeInt(opCode.ordinal());
		out.writeInt(startFrame);
		for (int ud : userData){
			out.writeInt(ud);
		}
	}
}
