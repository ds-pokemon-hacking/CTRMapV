
package ctrmap.util.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.io.util.IndentedPrintStream;

/**
 *
 */
public class SndResIDEnumGen {
	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(new File("D:\\_REWorkspace\\pokescript_genv\\snd\\sndNames.txt"));
			IndentedPrintStream out = new IndentedPrintStream(new File("D:\\_REWorkspace\\pokescript_genv\\snd\\sndEnum.h"));
			out.println("enum SoundResID {");
			out.incrementIndentLevel();
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int seq = line.indexOf("SEQ");
				if (seq != -1) {
					int id = Integer.parseInt(line.substring(0, seq).trim());
					out.println(line.substring(seq, line.length()).trim() + " = " + id + ",");
				}
			}
			out.decrementIndentLevel();
			out.println("}");
		} catch (IOException ex) {
			Logger.getLogger(SndResIDEnumGen.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
