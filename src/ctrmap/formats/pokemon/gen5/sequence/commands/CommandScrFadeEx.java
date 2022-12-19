
package ctrmap.formats.pokemon.gen5.sequence.commands;

/**
 *
 */
public class CommandScrFadeEx extends SeqCommandObject {
	public CommandScrFade.ScreenFadeMode mode;
	
	public int srcColor;
	public int fadeColor;
	public int dstColor;
	
	public int fadeOutDuration;
	public int fadeInDuration;
}
