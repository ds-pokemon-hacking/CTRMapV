
package ctrmap.util.tools;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.io.util.IndentedPrintStream;
import xstandard.text.FormattingUtils;
import xstandard.text.StringEx;

/**
 *
 */
public class MedalEnumGen {
	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(new File("D:\\_REWorkspace\\pokescript_genv\\snd\\medalNames.txt"));
			IndentedPrintStream out = new IndentedPrintStream(new File("D:\\_REWorkspace\\pokescript_genv\\snd\\medalEnum.h"));
			out.println("enum MedalID {");
			out.incrementIndentLevel();
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] params = line.split("\\s+");
				if (params.length >= 3) {
					int id = Integer.parseInt(params[0]);
					String name = FormattingUtils.getEnumlyString(StringEx.join(' ', 2, -1, params));
					out.println("MEDAL_" + name + " = " + id + ",");
				}
			}
			out.decrementIndentLevel();
			out.println("}");
		} catch (IOException ex) {
			Logger.getLogger(MedalEnumGen.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
