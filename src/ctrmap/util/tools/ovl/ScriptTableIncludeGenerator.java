package ctrmap.util.tools.ovl;

import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.util.IndentedPrintStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ScriptTableIncludeGenerator {

	public static final int EVCMD_TABLE_PTR = 0x2153C34;
	public static final int EVCMD_NELEMS_PTR = 0x2153C38;
	public static final int EVCMD_PERMS_PTR = 0x2153DC4;

	public static final int OVL_LOADADDR = 0x2150400;

	public static final String OVL12_PATH = "D:\\_REWorkspace\\CTRMapProjects\\White2\\vfs\\overlay\\overlay_0012.bin";
	public static final String RESULT_PATH = "D:\\_REWorkspace\\pokescript_genv\\codeinjection_new\\swan\\field\\field_evcmd_def.h";

	public static void main(String[] args) {
		try {
			IndentedPrintStream out = new IndentedPrintStream(new File(RESULT_PATH));

			out.println("#ifndef __FIELD_EVCMD_DEF_H");
			out.println("#define __FIELD_EVCMD_DEF_H");
			out.println();

			DiskFile f = new DiskFile(OVL12_PATH);

			DataIOStream io = f.getDataIOStream();

			io.setBase(OVL_LOADADDR);

			io.seek(EVCMD_TABLE_PTR);
			int tableAdr = io.readInt();
			io.seek(EVCMD_NELEMS_PTR);
			int countAdr = io.readInt();
			io.seek(EVCMD_PERMS_PTR);
			int permsAdr = io.readInt();
			io.seek(countAdr);
			int count = io.readInt();

			
			String[] permNames = new String[] {"EVCMD_PERM_EVENT", "EVCMD_PERM_INIT1", "EVCMD_PERM_INIT2"};
			
			out.println("#define EVCMD_PERM_NONE 0");
			for (int i = 0; i < permNames.length; i++) {
				out.println("#define " + permNames[i] + " " + (1 << i));
			}
			
			out.println();

			out.println("#ifdef EVCMD_REDEFINE");
			out.println();
			out.println("int EVCMD_TABLE[] = {");
			out.incrementIndentLevel();

			io.seek(tableAdr);
			for (int i = 0; i < count; i++) {
				out.print("0x" + Integer.toHexString(io.readInt()));
				out.print(",");
				if (i % 4 == 0) {
					out.print(" //0x" + Integer.toHexString(i));
				}
				out.println();
			}
			out.println("#ifdef EVCMD_TABLE_EXTENSION");
			out.println("#include EVCMD_TABLE_EXTENSION");
			out.println("#endif");

			out.decrementIndentLevel();
			out.println("};");
			out.println();

			out.println("char EVCMD_PERM_TABLE[] = {");
			out.incrementIndentLevel();

			
			io.seek(permsAdr);
			for (int i = 0; i < count; i++) {
				int perm = io.read();

				boolean first = true;

				for (int j = 0; j <= 2; j++) {
					if ((perm & (1 << j)) != 0) {
						if (!first) {
							out.print(" | ");
						}
						out.print(permNames[j]);
						first = false;
					}
				}
				if (perm == 0) {
					out.print("EVCMD_PERM_NONE");
				}

				out.print(",");
				if (i % 4 == 0) {
					out.print(" //0x" + Integer.toHexString(i));
				}
				out.println();
			}
			out.println("#ifdef EVCMD_PERM_EXTENSION");
			out.println("#include EVCMD_PERM_EXTENSION");
			out.println("#endif");

			out.decrementIndentLevel();
			out.println("};");
			out.println();
			out.println("int EVCMD_MAX = NELEMS(EVCMD_TABLE);");
			out.println();
			out.println("#else");
			out.println();
			out.println("extern int EVCMD_TABLE[];");
			out.println("extern int EVCMD_MAX;");

			out.println();
			out.println("#endif");

			out.println();
			out.println("extern int** pEVCMD_TABLE;");
			out.println("extern int* pEVCMD_MAX;");

			io.close();
			out.println();
			out.println("#endif");
			out.close();
		} catch (IOException ex) {
			Logger.getLogger(ScriptTableIncludeGenerator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
