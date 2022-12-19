package ctrmap.util.tools.qos;

import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.ReferenceType;
import xstandard.io.serialization.annotations.Inline;
import xstandard.io.util.IndentedPrintStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QOSDecompiler {

	public static void main(String[] args) {
		FSFile scrFile = new DiskFile("D:\\Emugames\\DS\\DStools\\Nintendo_DS_Compressors-CUE\\agentMI6Training_01.ycomp");
		try {
			QOSScript scr = loadScript(scrFile);
			DataIOStream in = scrFile.getDataIOStream();
			IndentedPrintStream out = new IndentedPrintStream(new DiskFile("D:\\Emugames\\DS\\DStools\\Nintendo_DS_Compressors-CUE\\agentMI6Training_01.txt").getNativeOutputStream());
			List<Integer> funcAddresses = new ArrayList<>();
			List<Integer> labelAddresses = new ArrayList<>();
			for (QOSInstructions.Base i : scr.instructions) {
				if (i != null) {
					if (i instanceof QOSInstructions.CommonOp) {
						QOSInstructions.CommonOp cmn = (QOSInstructions.CommonOp) i;
						if (cmn.operandType == QOSInstructions.CommonOp.OperandType.ADDRESS) {
							System.out.println("address operand at " + Integer.toHexString(cmn.operand));
							in.seek(cmn.operand);
							try {
								String str = in.readString();
								boolean invalid = false;
								boolean anyLetter = false;
								for (char c : str.toCharArray()) {
									if (c < 11 || c > 127 || c == 0x1b) {
										invalid = true;
										break;
									}
									if (!anyLetter && Character.isLetter(c)) {
										anyLetter = true;
									}
								}
								if (!invalid && anyLetter && !str.isEmpty()) {
									//out.println(".string off_" + Integer.toHexString(cmn.operand) + " " + str + ";");
									scr.strings.put(cmn.operand, str);
								}
							} catch (Exception ex) {

							}
						}
						if (cmn.opCode == QOSOpCode.B || cmn.opCode == QOSOpCode.BNZ || cmn.opCode == QOSOpCode.BZER || cmn.opCode == QOSOpCode.PUSH) {
							if (cmn.operandType == QOSInstructions.CommonOp.OperandType.ADDRESS) {
								labelAddresses.add(cmn.operand);
							}
						}
					} else if (i.opCode == QOSOpCode.CALL) {
						QOSInstructions.Call c = (QOSInstructions.Call) i;
						if (c.type == QOSInstructions.Call.Type.LOCAL) {
							funcAddresses.add(((QOSInstructions.Call.LocalTarget) c.target).address);
						}
					}
				}
			}
			Map<Integer, String> publicFuncs = new HashMap<>();
			for (QOSSymbol sym : scr.exports) {
				if (sym.segment == 1) { //code
					publicFuncs.put(sym.offset, sym.name);
				}
			}

			out.incrementIndentLevel();

			int index = 0;
			for (QOSInstructions.Base i : scr.instructions) {
				if (publicFuncs.containsKey(i.addr)) {
					out.decrementIndentLevel();
					out.println(publicFuncs.get(i.addr) + ":");
					out.incrementIndentLevel();
				}
				if (funcAddresses.contains(i.addr) || index == 0) {
					out.decrementIndentLevel();
					out.println("sub_" + Integer.toHexString(i.addr) + ":");
					out.incrementIndentLevel();
				}
				if (labelAddresses.contains(i.addr)) {
					out.decrementIndentLevel();
					out.println("off_" + Integer.toHexString(i.addr) + ":");
					out.incrementIndentLevel();
				}
				try {
					out.println(i.dump(scr) + ";");
				} catch (Exception ex) {
					out.println("[ERROR @ 0x" + Integer.toHexString(i.addr) + "]");
				}
				index++;
			}

			out.close();
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(QOSDecompiler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static QOSScript loadScript(FSFile file) throws IOException {
		QOSScript s = new QOSScript();

		DataIOStream io = file.getDataIOStream();
		QOSScriptHeader header = new BinaryDeserializer(io, ByteOrder.LITTLE_ENDIAN, ReferenceType.NONE).deserialize(QOSScriptHeader.class);
		io.seek(header.dataOffset);

		s.data = new int[header.dataSize >> 2];
		for (int i = 0; i < s.data.length; i++) {
			s.data[i] = io.readInt();
		}

		io.seek(header.relocationTableOffset);
		for (int i = 0; i < header.relocationCount; i++) {
			s.data[i] |= 0x80000000; //flag as relocated
		}

		io.seek(header.exportTableOffset);
		s.exports = new QOSSymbol[header.exportTableSize >> 3];
		for (int i = 0; i < s.exports.length; i++) {
			s.exports[i] = new QOSSymbol(io);
		}

		io.seek(header.importTableOffset);
		s.imports = new QOSSymbol[header.importTableSize >> 3];
		for (int i = 0; i < s.imports.length; i++) {
			s.imports[i] = new QOSSymbol(io);
		}

		io.seek(header.codeOffset);
		int limit = header.codeOffset + header.codeSize;
		for (QOSSymbol sym : s.exports) {
			if (sym.nameOfs < limit) {
				limit = sym.nameOfs;
			}
		}
		for (QOSSymbol sym : s.imports) {
			if (sym.nameOfs < limit) {
				limit = sym.nameOfs;
			}
		}
		while (io.getPosition() < limit) {
			s.instructions.add(QOSInstructions.read(io));
		}

		io.close();
		return s;
	}

	public static class QOSScript {

		public List<QOSInstructions.Base> instructions = new ArrayList<>();
		public int[] data;
		public Map<Integer, String> strings = new HashMap<>();
		public QOSSymbol[] exports;
		public QOSSymbol[] imports;
	}

	public static class QOSSymbol {

		public int offset;
		private int nameOfs;
		public String name;
		public int segment;
		public int dataType;
		public int argCount;
		public int field_7;

		public QOSSymbol(DataIOStream in) throws IOException {
			offset = in.readUnsignedShort();
			nameOfs = in.readUnsignedShort();
			in.checkpoint();
			in.seek(nameOfs);
			name = in.readString();
			in.resetCheckpoint();
			segment = in.readUnsignedByte();
			dataType = in.readUnsignedByte();
			argCount = in.readUnsignedByte();
			field_7 = in.readUnsignedByte();
		}
	}

	@Inline
	public static class QOSScriptHeader {

		public int codeOffset;
		public int dataOffset;
		public int exportTableOffset;
		public int importTableOffset;
		public int relocationTableOffset;
		public int codeSize;
		public int dataSize;
		public int exportTableSize;
		public int importTableSize;
		public int relocationCount;
	}
}
