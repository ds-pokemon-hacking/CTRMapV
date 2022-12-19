
package ctrmap.formats.pokemon.gen5.sequence.commands;

import ctrmap.formats.pokemon.gen5.sequence.SeqOpCode;

/**
 *
 */
public class CommandSEAnimate extends SeqCommandObject {
	public int SEid;
	public int startValue;
	public int endValue;
	public int duration;
	public int channel;
	
	public SEAnimTarget getSEAnimTarget(){
		return SEAnimTarget.values()[getOpCode().ordinal() - SeqOpCode.CMD_SE_ANIM_VOLUME.ordinal()];
	}
	
	public static enum SEAnimTarget {
		VOLUME,
		PAN,
		SPEED
	}
}
