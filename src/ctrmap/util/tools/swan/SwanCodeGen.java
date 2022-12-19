package ctrmap.util.tools.swan;

import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.io.util.IndentedPrintStream;
import xstandard.text.FormattingUtils;
import xstandard.text.StringEx;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SwanCodeGen {

	public static void genFromDB(SwanDB db) {
		FSFile rootDir = db.getYaml().getFile().getParent();
		for (SwanSourceFile f : db.sourceFiles) {
			FSFile dest = rootDir.getChild(f.path);
			dest.getParent().mkdirs();
			genFile(db, f, dest);
		}
	}

	public static void genFile(SwanDB db, SwanSourceFile src, FSFile dest) {
		IndentedPrintStream out = new IndentedPrintStream(dest.getNativeOutputStream());

		String filename = FSUtil.getFileName(src.path);
		String includeGuardName = "__" + filename.toUpperCase().replace('.', '_');

		out.println("#ifndef " + includeGuardName);
		out.println("#define " + includeGuardName);
		out.println();
		out.println("#include \"swantypes.h\"");
		for (SwanEnum e : src.enums) {
			if (e.defineFlagOps) {
				out.println("#include \"swan_cpp_enum_ops.h\"");
				break;
			}
		}
		out.println();

		if (src.macros != null) {
			String m = src.macros.trim();
			if (!m.isEmpty()) {
				out.println(m);
				out.println();
			}
		}

		if (!src.structures.isEmpty()) {
			boolean printed = false;
			for (SwanStructure struct : src.structures) {
				if (!struct.isPureCpp() && !struct.isNs()) {
					String tn = struct.definition.getTypeName();
					if (tn != null) {
						boolean isUnion = false;
						int fbIdx = struct.definition.content.indexOf('{');
						if (fbIdx != -1) {
							isUnion = struct.definition.content.lastIndexOf("union", fbIdx) != -1;
						}
						if (isUnion) {
							out.println("UNION_DECLARE(" + tn + ")");
						} else {
							out.println("STRUCT_DECLARE(" + tn + ")");
						}
						printed = true;
					}
				}
			}
			if (printed) {
				out.println();
			}
		}

		if (!src.enums.isEmpty()) {
			for (SwanEnum enm : src.enums) {
				String tn = enm.definition.getTypeName();
				if (tn != null && !enm.definition.content.contains("SWAN_PACKED")) {
					out.println("ENUM_DECLARE(" + tn + ")");
				}
			}
			out.println();
		}

		if (!src.typedefs.isEmpty()) {
			for (SwanTypedef plainTypedef : src.typedefs) {
				printCodeBlock(out, plainTypedef.content);
			}
			out.println();
		}

		for (SwanEnum enm : src.enums) {
			String cName = enm.definition.getTypeName();
			printCodeBlock(out, makeEnumDef(enm.definition.content));
			if (enm.definition.cppName != null) {
				printCppName(out, cName, enm.definition.cppName);
			}
			if (enm.defineFlagOps) {
				out.println("DEFINE_ENUM_FLAG_OPERATORS(" + cName + ")");
			}
			out.println();
		}

		//CPP types
		if (!src.structures.isEmpty() || !src.typedefs.isEmpty()) {
			boolean printed = false;
			for (SwanStructure struct : src.structures) {
				if (!struct.isNs()) {
					if (!struct.isPureCpp()) {
						String cName = struct.definition.getTypeName();
						if (struct.definition.cppName != null) {
							printCppName(out, cName, struct.definition.cppName);
							printed = true;
						}
					} else {
						printCppName(out, null, struct.definition.getTypeName());
						printed = true;
					}
				}
			}
			for (SwanTypedef plainTypedef : src.typedefs) {
				if (plainTypedef.cppName != null) {
					printCppName(out, plainTypedef.getTypeName(), plainTypedef.cppName);
					printed = true;
				}
			}
			if (printed) {
				out.println();
			}
		}

		if (!src.includes.isEmpty()) {
			for (String incl : src.includes) {
				out.println("#include \"" + incl + "\"");
			}
			out.println();
		}

		if (!src.functions.isEmpty()) {
			out.println("C_DECL_BEGIN");
			out.println();

			int ppIdx = SwanTypedef.getPrettyPrintFirstNameIdx(src.functions, true);
			for (SwanTypedef func : src.functions) {
				printFuncDef(out, func.prettyPrint(ppIdx, true));
			}

			out.println();
			out.println("C_DECL_END");
			out.println();
		}

		for (SwanStructure struct : src.structures) {
			if (!struct.isNs()) {
				printStruct(out, db, src, struct);
				out.println();
			}
		}

		for (SwanStructure struct : src.structures) {
			if (struct.isNs()) {
				out.println("#ifdef __cplusplus");
				out.println("namespace " + getCppNamespace(struct.definition.getTypeName()) + " {\n");
				out.print(genCppInlines(db, src, struct));
				out.println("}");
				out.println("#endif");
				out.println();
			}
		}

		for (SwanTypedef gvar : src.gvars) {
			printCodeBlock(out, "extern " + gvar.content);
			out.println();
		}

		out.println("#endif //" + includeGuardName);
		out.println("// Tchaikovsky code generator");

		out.close();
	}

	private static String makeEnumDef(String def) {
		if (!def.contains("SWAN_PACKED")) {
			int start = def.indexOf('{');
			if (start != -1) {
				String first = def.substring(0, start);
				if (!first.endsWith("\n")) {
					first += "\n";
				}
				return first
					+ "#ifdef __cplusplus\n"
					+ ": u32\n"
					+ "#endif\n" + def.substring(start);
			}
		}
		return def;
	}

	private static String getCppNamespace(String cppName) {
		int nsBegin = cppName.lastIndexOf(":") + 1; //0 if -1
		if (nsBegin != 0) {
			String cppNamespace = cppName.substring(0, nsBegin - "::".length());
			return cppNamespace;
		}
		return null;
	}

	private static void printCppName(PrintStream out, String cName, String cppName) {
		String cppNamespace = getCppNamespace(cppName);
		if (cppName != null) { //same as C type
			String cppLocalName = cppName.substring(cppName.lastIndexOf(":") + 1);
			if (cName == null) {
				out.println("namespace " + cppNamespace + " { struct " + cppLocalName + "; }");
			} else {
				if (cppLocalName.equals(cName)) {
					out.println("SWAN_CPPTYPE(" + cppNamespace + ", " + cName + ")");
				} else {
					out.println("SWAN_CPPTYPE_EX(" + cppNamespace + ", " + cppLocalName + ", " + cName + ")");
				}
			}
		}
	}

	private static void printStruct(IndentedPrintStream out, SwanDB db, SwanSourceFile file, SwanStructure struct) {
		boolean purecpp = struct.isPureCpp();
		if (purecpp) {
			out.println("#ifdef __cplusplus");
		}
		String def = struct.definition.content;
		int cppInjectIdx = def.lastIndexOf('}');
		if (cppInjectIdx != -1) {
			def = def.substring(0, cppInjectIdx) + genCppInlines(db, file, struct) + def.substring(cppInjectIdx, def.length());
		}
		printCodeBlock(out, def);
		if (purecpp) {
			out.println("#endif //__cplusplus");
		}
	}

	private static String genCppInlines(SwanDB db, SwanSourceFile file, SwanStructure struct) {
		if (struct.methods.isEmpty()) {
			return "";
		}
		StringBuilder bld = new StringBuilder();

		boolean purecpp = struct.isPureCpp();
		boolean isNs = struct.isNs();
		if (!purecpp) {
			bld.append("  \n");
			bld.append("  #ifdef __cplusplus\n");
			bld.append("  \n");
		}

		Map<String, String> cToCppNames = new HashMap<>();

		for (SwanSourceFile f2 : db.sourceFiles) {
			if (file.includes.contains(f2.path) || f2 == file) {
				for (SwanStructure s2 : f2.structures) {
					String cppName = s2.definition.cppName;
					if (cppName != null && !cppName.trim().isEmpty()) {
						cToCppNames.put(s2.definition.getTypeName(), cppName);
					}
				}
				for (SwanEnum e2 : f2.enums) {
					String cppName = e2.definition.cppName;
					if (cppName != null && !cppName.trim().isEmpty()) {
						cToCppNames.put(e2.definition.getTypeName(), cppName);
					}
				}
			}
		}

		for (SwanMethod m : struct.methods) {
			SwanTypedef cTypedef = file.getFuncByName(m.cName);
			if (cTypedef == null) {
				System.err.println("Failed to find C function for C++ method " + m.cppName + " (C name: " + m.cName + ")");
			} else {
				bld.append("  INLINE ");
				if (m.isStatic && !isNs) {
					bld.append("static ");
				}
				String prototype = SwanTypedef.sanitizeCodeBlock(cTypedef.content).replace("extern ", "");

				for (Map.Entry<String, String> e : cToCppNames.entrySet()) {
					int index = 0;
					String typename = e.getKey();
					int len = typename.length();
					while ((index = prototype.indexOf(typename, index)) != -1) {
						if (index < 1 || !SwanTypedef.isNameChar(prototype.charAt(index - 1))) {
							int endIndex = index + len;
							if (endIndex >= prototype.length() || !SwanTypedef.isNameChar(prototype.charAt(endIndex))) {
								prototype = prototype.substring(0, index) + e.getValue() + prototype.substring(endIndex, prototype.length());
								index = index + e.getValue().length();
							}
						}
						index++;
					}
				}

				boolean isReturn = !prototype.startsWith("void ");
				List<String> args = getFuncArgs(m.cName, prototype);
				int[] swizzleParams = m.getParamSwizzleIndices();
				if (m.isStatic || isNs) {
					bld.append(prototype.replace(m.cName, m.cppName));
				} else {
					if (!args.isEmpty()) {
						int firstArgIdx = prototype.indexOf('(', prototype.indexOf(m.cName)) + 1;
						int firstArgEndIdx = prototype.indexOf(args.get(0), firstArgIdx) + args.get(0).length();
						if (prototype.charAt(firstArgEndIdx) == ',') {
							firstArgEndIdx++;
						}
						bld.append(prototype.substring(0, firstArgIdx).replace(m.cName, m.cppName));
						if (swizzleParams == null) {
							bld.append(prototype.substring(firstArgEndIdx, prototype.length()).trim()); //remove first parameter - substitute with `this`
						} else {
							HashSet<Integer> usedParams = new HashSet<>();
							boolean first = true;
							for (int sp : swizzleParams) {
								if (sp > 0) {
									if (!usedParams.contains(sp)) {
										if (first) {
											first = false;
										} else {
											bld.append(", ");
										}
										bld.append(args.get(sp - 1));
										usedParams.add(sp);
									}
								}
							}
							bld.append(")");
						}
					} else {
						System.err.println("Non-static C function args empty!!");
					}
				}
				bld.append(" {\n");
				bld.append("    ");
				if (isReturn) {
					bld.append("return ");
				}
				bld.append(m.cName);
				bld.append("(");
				String[] spExtra = m.getSwizzleParamExtra(swizzleParams);
				for (int i = 0; i < args.size(); i++) {
					if (i != 0) {
						bld.append(", ");
					}
					int argIdx = i;
					if (swizzleParams != null) {
						argIdx = swizzleParams[i];
					}
					if (spExtra != null && spExtra[i] != null) {
						bld.append(spExtra[i]);
					} else {
						if (argIdx == 0 && !(m.isStatic || isNs)) {
							bld.append("this");
						} else {
							int idx = swizzleParams != null ? argIdx - 1 : argIdx;
							if (idx >= 0 && idx < args.size()) {
								bld.append(getArgName(args.get(idx)));
							} else {
								bld.append("[OUT OF RANGE SWIZZLE: ").append(idx).append("]");
							}
						}
					}
				}
				bld.append(");\n");
				bld.append("  }\n\n");
			}
		}

		if (!purecpp) {
			bld.append("  #endif\n");
		}
		return bld.toString();
	}

	private static String getArgName(String arg) {
		return arg.substring(arg.lastIndexOf(' ') + 1);
	}

	private static List<String> getFuncArgs(String funcName, String def) {
		int firstArgIdx = def.indexOf('(', def.indexOf(funcName)) + 1;
		int endIdx = def.lastIndexOf(')');
		String onlyArgs = def.substring(firstArgIdx, endIdx);
		List<String> out = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		int blevel = 0;
		for (int i = 0; i < onlyArgs.length(); i++) {
			char c = onlyArgs.charAt(i);
			switch (c) {
				case '(':
					blevel++;
					break;
				case ')':
					blevel--;
					break;
				case ',':
					if (blevel == 0) {
						out.add(sb.toString().trim());
						sb = new StringBuilder();
					}
					continue;
			}
			sb.append(c);
		}
		out.add(sb.toString().trim());
		return out;
	}

	private static void printFuncDef(PrintStream out, String def) {
		if (!def.contains("extern ") && !def.contains("INLINE")) {
			def = "extern " + def;
		}
		if (!def.endsWith(";")) {
			def += ";";
		}
		printCodeBlock(out, def);
	}

	private static void printCodeBlock(PrintStream out, String block) {
		out.print(indentWithTabs(SwanTypedef.sanitizeCodeBlock(block)));
	}

	private static String indentWithTabs(String block) {
		String[] lines = StringEx.splitOnecharFast(block, '\n');
		StringBuilder out = new StringBuilder();
		for (String line : lines) {
			boolean printed = false;
			for (int i = 0; i < line.length(); i++) {
				if (!Character.isWhitespace(line.charAt(i))) {
					for (int j = 0; j < i / 2; j++) {
						out.append("    ");
					}
					out.append(line.substring(i));
					printed = true;
					break;
				}
			}
			if (!printed) {
				out.append(line);
			}
			out.append('\n');
		}
		return out.toString();
	}

}
