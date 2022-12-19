
package ctrmap.formats.pokemon.gen5.sequence.commands;

/**
 *
 */
public class CommandBGMFade extends SeqCommandObject {
	public BGMFadeMode mode;
	public int duration;
	
	public static enum BGMFadeMode {
		IN,
		OUT
	}
}
