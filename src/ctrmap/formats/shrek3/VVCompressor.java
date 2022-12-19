package ctrmap.formats.shrek3;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.cli.ArgumentBuilder;
import xstandard.cli.ArgumentContent;
import xstandard.cli.ArgumentPattern;
import xstandard.cli.ArgumentType;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataInStream;

public class VVCompressor {

	private static final ArgumentPattern[] ARG_PTNS = new ArgumentPattern[]{
		new ArgumentPattern("mode", "Compress/decompress mode (c/d).", ArgumentType.STRING, "c", "-m", "--mode"),
		new ArgumentPattern("input", "Input file path.", ArgumentType.STRING, null, "-i", "--input"),
		new ArgumentPattern("output", "Output file path.", ArgumentType.STRING, null, "-o", "--output"),
		new ArgumentPattern("maxDist", "Maximum compression distance.", ArgumentType.INT, 8192, "--maxdist")
	};

	private static void printHelp(String exitMessage) {
		if (exitMessage != null) {
			System.out.println(exitMessage);
			System.out.println();
		}
		System.out.println("Shrek the Third DS decompressor v1.0");
		System.out.println();
		new ArgumentBuilder(ARG_PTNS).print();
	}

	public static void main(String[] args) {
		ArgumentBuilder bld = new ArgumentBuilder(ARG_PTNS);
		bld.parse(args);

		ArgumentContent input = bld.getContent("input", true);
		if (input == null) {
			input = bld.defaultContent;
		}
		if (input == null || input.contents.isEmpty()) {
			printHelp("No input file specified.");
			return;
		}
		FSFile inFile = new DiskFile(input.stringValue());
		if (!inFile.canRead() || inFile.isDirectory()) {
			printHelp("Could not read input file.");
			return;
		}

		ArgumentContent output = bld.getContent("output", true);
		FSFile outFile;
		if (output == null) {
			outFile = inFile.getParent().getChild(inFile.getName() + ".B");
		} else {
			outFile = new DiskFile(output.stringValue());
		}
		if (!outFile.canWrite() || outFile.isDirectory()) {
			printHelp("Can not write to output file.");
			return;
		}

		String mode = bld.getContent("mode").stringValue();

		try {
			if (mode.equals("c")) {
				int maxDist = bld.getContent("maxDist").intValue();
				DataIOStream out = outFile.getDataIOStream();
				out.setLength(0);
				vvBlockCompress(inFile.getBytes(), out, maxDist);
				out.close();
			} else if (mode.equals("d")) {
				DataInStream in = inFile.getDataInputStream();
				outFile.setBytes(vvBlockDecompress(in));
				in.close();
			}
			else {
				printHelp("Unknown operation mode: " + mode);
				return;
			}
		} catch (IOException ex) {
			Logger.getLogger(VVCompressor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static byte[] vvBlockDecompress(byte[] bytes) {
		try {
			return vvBlockDecompress(new DataInStream(bytes));
		} catch (IOException ex) {
			Logger.getLogger(VVCompressor.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static byte[] vvBlockDecompress(DataInStream input) throws IOException {
		byte[] out = new byte[input.readInt()];
		vvdecompress(input, out);
		return out;
	}

	public static void vvdecompress(DataInStream input, byte[] uncomp) throws IOException {
		int uncompSize = uncomp.length;
		int outIndex = 0;
		U16BitReader br = new U16BitReader(input);
		while (outIndex < uncompSize) {
			if (br.readBit()) {
				int dispSize = br.readVarLenInt() + 2;
				int dispBlockCount = br.readVarLenInt();
				int disp = input.read() + 256 * dispBlockCount - 511; //minimum value encoded by VarLenInt is 2, the 511 negates that
				//System.out.println("read disp " + disp + " bc " + dispBlockCount + " dispsize " + dispSize);

				for (int i = outIndex - disp; dispSize > 0; dispSize--, i++) {
					uncomp[outIndex] = uncomp[i];
					outIndex++;
				}
			} else {
				uncomp[outIndex] = input.readByte();
				outIndex++;
			}
		}
	}

	public static byte[] vvBlockCompress(byte[] input, int maxDist) {
		try {
			DataIOStream out = new DataIOStream();
			vvBlockCompress(input, out, maxDist);
			out.close();
			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(VVCompressor.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static void vvBlockCompress(byte[] input, DataIOStream out, int maxDist) throws IOException {
		out.writeInt(input.length);
		vvcompress(input, out, maxDist);
	}

	public static void vvcompress(byte[] input, DataIOStream out, int maxDist) throws IOException {
		U16BitWriterManager bw = new U16BitWriterManager(out);
		int inputIndex = 0;
		DispBlock db = new DispBlock();
		while (inputIndex < input.length) {
			if (findDispBlock(db, input, inputIndex, maxDist)) {
				//System.out.println("found db in " + Arrays.toString(input) + " at " + db.startOffset + " size " + db.length + " for 0x" + Integer.toHexString(inputIndex));
				bw.writeBit(true);
				bw.writeVarLenInt(db.length - 2);
				int disp = inputIndex - db.startOffset - 1;
				inputIndex += db.length;
				//System.out.println("disp0 " + disp);
				disp += 512; //default minimum disp
				//System.out.println("disp " + disp);
				int block = disp >> 8;
				int remainder = disp & 0xFF;
				//System.out.println("varlendisp " + block);
				//System.out.println("nonvarlendisp " + remainder);
				bw.writeVarLenInt(block);
				bw.notifyOtherWriteStart();
				out.write(remainder);
			} else {
				int inByte = input[inputIndex];
				bw.writeBit(false);
				bw.notifyOtherWriteStart();
				//System.out.println("write byte " + inputIndex + " val " + inByte + " @ " + Integer.toHexString(out.getPosition()));
				out.write(inByte);
				inputIndex++;
			}
		}
		bw.forceFlush();
		out.close();
	}

	private static boolean findDispBlock(DispBlock dest, byte[] input, int inputIndex, int maxDist) {
		int maxlen = 0;
		int minIdx = Math.max(0, inputIndex - maxDist);
		Outer:
		for (int i = inputIndex - 1; i >= minIdx; i--) {
			if (input[inputIndex] == input[i]) {
				for (int j = i; j < input.length; j++) {
					int cmpIdx = inputIndex + (j - i);
					if (cmpIdx >= input.length || input[j] != input[cmpIdx]) {
						int len = j - i;
						if (len >= maxlen) {
							dest.startOffset = i;
							dest.length = len;
							maxlen = len;
							if (cmpIdx == input.length) {
								break Outer;
							}
						}
						break;
					}
				}
			}
		}
		return maxlen >= 4;
	}

	private static class DispBlock {

		public int startOffset;
		public int length;
	}

	private static void testing() {
		try {
			/*DataIOStream out = new DataIOStream();
			U16BitWriterManager bw = new U16BitWriterManager(out);
			bw.writeVarLenInt(420420);
			bw.notifyOtherWriteStart();
			System.out.println("otherwrite notify " + out.getPosition());
			out.writeInt(0xAAAAAAAA);
			System.out.println("pos " + out.getPosition());
			bw.writeVarLenInt(666);
			bw.forceFlush();
			out.seek(0);
			for (byte b : out.readBytes(out.getLength())) {
				System.out.print(FormattingUtils.getStrWithLeadingZeros(2, Integer.toHexString(b & 0xFF)) + " ");
			}
			System.out.println();
			out.seek(0);
			U16BitReader br = new U16BitReader(out);
			System.out.println(br.readVarLenInt());
			System.out.println(Integer.toHexString(out.readInt()));
			System.out.println(br.readVarLenInt());*/
			byte[] testBytes = /*new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}*/ new DiskFile("D:\\_REWorkspace\\pokescript_genv\\_CUTSCENE_IDB\\overlay\\overlay_0036.bin").getBytes();
			byte[] compressed = vvBlockCompress(testBytes, 8192);
			FSUtil.writeBytesToFile(new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\strings.B"), compressed);
			/*System.out.println("Compressed data dump:");
			for (byte b : compressed) {
				System.out.print(Integer.toHexString(b & 0xFF) + " ");
			}
			System.out.println();*/
			byte[] decompressed = vvBlockDecompress(compressed);
			FSUtil.writeBytesToFile(new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\strings.B.dec"), decompressed);
			//System.out.println(Arrays.toString(decompressed));
			System.out.println(Arrays.equals(testBytes, decompressed));
			new DataIOStream().close();
		} catch (IOException ex) {
			Logger.getLogger(VVCompressor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static class U16BitWriterManager {

		private DataIOStream out;

		private boolean wantSwapBuffers = false;
		private int lastStreamPos;
		private U16BitWriter cur;

		public U16BitWriterManager(DataIOStream out) {
			this.out = out;
			lastStreamPos = -1;
		}

		public void writeBit(boolean bit) throws IOException {
			if (cur == null) {
				lastStreamPos = out.getPosition();
				cur = new U16BitWriter();
			}
			cur.writeBit(bit);
			if (wantSwapBuffers) {
				if (cur.isAligned()) {
					forceFlush();
					wantSwapBuffers = false;
				}
			}
		}

		public void writeVarLenInt(int value) throws IOException {
			int msb = Integer.SIZE - Integer.numberOfLeadingZeros(value) - 1;
			for (int bitIndex = msb - 1; bitIndex >= 0; bitIndex--) {
				writeBit((value & (1 << bitIndex)) != 0);
				writeBit(bitIndex != 0);
			}
		}

		public void notifyOtherWriteStart() throws IOException {
			if (cur == null) {
				return;
			}
			if (cur.isAligned()) {
				forceFlush();
			} else {
				if (!wantSwapBuffers) {
					CheckpointIfBefore cp = new CheckpointIfBefore(out, lastStreamPos);
					cur.allocateInStream(out);
					cp.restore();
					wantSwapBuffers = true;
				} //else already requested
			}
		}

		public void forceFlush() throws IOException {
			if (cur != null) {
				CheckpointIfBefore cp = new CheckpointIfBefore(out, lastStreamPos);
				cur.writeToStream(out);
				cp.restore();
				cur = null;
			}
		}

		private static class CheckpointIfBefore {

			private boolean checked;
			private DataIOStream io;

			public CheckpointIfBefore(DataIOStream out, int myPos) throws IOException {
				checked = out.getPosition() > myPos;
				this.io = out;
				if (checked) {
					io.checkpoint();
				}
				out.seek(myPos);
			}

			public void restore() throws IOException {
				if (checked) {
					io.resetCheckpoint();
				}
			}
		}
	}

	private static class U16BitWriter {

		private final List<Short> data = new ArrayList<>();

		private short curVal;
		private byte curIdx = 0;

		public U16BitWriter() {
			curIdx = 15;
			curVal = 0;
		}

		public boolean isAligned() {
			return curIdx == 15;
		}

		public void writeBit(boolean isOne) {
			if (isOne) {
				curVal |= (short) (1 << curIdx);
			}
			curIdx--;
			if (curIdx < 0) {
				flush();
			}
		}

		public void flush() {
			if (!isAligned()) {
				data.add(curVal);
				curVal = 0;
				curIdx = 15;
			}
		}

		public void allocateInStream(DataOutput out) throws IOException {
			for (int i = 0; i < data.size(); i++) {
				out.writeShort(0);
			}
			if (!isAligned()) {
				//System.out.println("alloc unaligned");
				out.writeShort(0);
			}
		}

		public void writeToStream(DataOutput out) throws IOException {
			flush();
			for (Short d : data) {
				out.write(d & 0xFF);
				out.write((d >> 8) & 0xFF);
			}
			data.clear();
		}
	}

	private static class U16BitReader {

		private DataInputEx in;

		private short curVal;
		private byte curIdx = 0;

		public U16BitReader(DataInputEx in) {
			this.in = in;
			curIdx = 0;
			curVal = 0;
		}

		public boolean readBit() throws IOException {
			curIdx--;
			if (curIdx < 0) {
				curVal = (short) (in.readUnsignedByte() | (in.readUnsignedByte() << 8));
				curIdx = 15;
			}
			boolean ret = (curVal & 0x8000) != 0;
			curVal <<= 1;
			return ret;
		}

		public int readVarLenInt() throws IOException {
			int val = 1;
			do {
				val = (val << 1) | (readBit() ? 1 : 0);
			} while (readBit());
			return val;
		}
	}
}
