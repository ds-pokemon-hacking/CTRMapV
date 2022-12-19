
package ctrmap.formats.pokemon.gen5.sequence.commands;

import ctrmap.formats.pokemon.gen5.sequence.SeqOpCode;

/**
 *
 */
public class SeqCommandObject {
	private transient SeqOpCode opCode;
	public transient int startFrame;
	
	public SeqOpCode getOpCode(){
		return opCode;
	}
	
	void setOpCode(SeqOpCode op){
		opCode = op;
	}
}
