
package ctrmap.formats.pokemon.gen5.sequence.commands;

public class CommandG2DCellGra extends SeqCommandObject {
	public static final int GAMEFLAG_W = 22;
	public static final int GAMEFLAG_B = 23;
	public static final int GAMEFLAG_ALL = 255;
	
	public int gameFlags;
	
	public int palID;
	public int graID;
	public int scrID;
	
	public int fadeInDuration;
	public int stayDuration;
	public int fadeOutDuration;
}
