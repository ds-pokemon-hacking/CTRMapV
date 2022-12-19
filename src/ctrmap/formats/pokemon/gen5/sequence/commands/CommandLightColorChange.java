
package ctrmap.formats.pokemon.gen5.sequence.commands;

import xstandard.math.vec.RGBA;

/**
 *
 */
public class CommandLightColorChange extends SeqCommandObject {
	public int lightId;
	
	public int r;
	public int g;
	public int b;
	
	public RGBA getColor(){
		return new RGBA(r / 31f, g / 31f, b / 31f, 1f);
	}
}
