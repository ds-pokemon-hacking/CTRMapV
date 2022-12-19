
package ctrmap.formats.pokemon.gen5.sequence.commands;

/**
 *
 */
public class CommandScrFade extends SeqCommandObject {
	public ScreenFadeMode mode;
	public int duration;
	public int srcColor;
	public int fadeColor;
	
	public static enum ScreenFadeMode {
		NONE,
		TOP,
		BOTTOM,
		BOTH //bitflags
	}
}
