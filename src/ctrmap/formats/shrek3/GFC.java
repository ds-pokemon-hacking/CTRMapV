package ctrmap.formats.shrek3;

import com.jcraft.jzlib.InflaterInputStream;
import com.jcraft.jzlib.VicariousInflaterInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.InputStreamReadable;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import xstandard.io.util.IOUtils;
import xstandard.text.FormattingUtils;
import xstandard.util.collections.IntList;

public class GFC {

	public static final int GFC_MAGIC = 0x8008;

	public int gobSize;
	public List<ChunkInfo> chunks;
	public IntList hashes;
	public List<FileInfo> files;

	public GFC(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();
			in.order(ByteOrder.BIG_ENDIAN);
			if (in.readInt() == GFC_MAGIC) {
				gobSize = in.readInt();
				int chunkCount = in.readInt();
				int fileCount = in.readInt();
				chunks = new ArrayList<>(chunkCount);
				hashes = new IntList(chunkCount);
				files = new ArrayList<>(fileCount);
				for (int i = 0; i < chunkCount; i++) {
					chunks.add(new ChunkInfo(in));
				}
				for (int i = 0; i < chunkCount; i++) {
					hashes.add(in.readInt());
				}
				for (int i = 0; i < fileCount; i++) {
					files.add(new FileInfo(in));
				}
			} else {
				throw new RuntimeException("Invalid GFC magic!");
			}
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(GFC.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void write(FSFile dest) {
		try {
			DataOutStream out = dest.getDataOutputStream();
			out.order(ByteOrder.BIG_ENDIAN);
			out.writeInt(GFC_MAGIC);
			out.writeInt(gobSize);
			out.writeInt(chunks.size());
			out.writeInt(files.size());
			for (ChunkInfo ci : chunks) {
				ci.write(out);
			}
			for (int i = 0; i < hashes.size(); i++) {
				out.writeInt(hashes.get(i));
			}
			for (FileInfo fi : files) {
				fi.write(out);
			}
			out.close();
		} catch (IOException ex) {
			Logger.getLogger(GFC.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void addCharArr(char[] arr) {
		int index = 9;
		while (true) {
			if (arr[index] == '9') {
				arr[index] = 'a';
				break;
			} else if (arr[index] == 'f') {
				arr[index] = '0';
				index--;
			} else {
				arr[index]++;
				break;
			}
		}
	}

	private static void scanForModelStuff() {
		FSFile gfcFile = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data\\gob\\main.gfc");
		FSFile strings = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\strings.txt");
		FSFile arm9 = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\arm9.bin");
		GFC gfc = new GFC(gfcFile);
		DataIOStream arm9io = arm9.getDataIOStream();
		int dataSegStart = 0x7C398;

		try {
			Set<String> usedBaseNames = new HashSet<>();
			Scanner s = new Scanner(strings.getNativeInputStream());
			while (s.hasNextLine()) {
				String line = s.nextLine();
				if (line.contains("collisionspheres") || line.contains("textureinfo")) {
					String baseName = line.substring(0, Math.max(line.indexOf("collisionspheres"), line.indexOf("textureinfo")));
					if (usedBaseNames.contains(baseName)) {
						continue;
					}
					else {
						usedBaseNames.add(baseName);
					}
					
					int hash = Integer.parseUnsignedInt(line.substring(2, 2 + 8), 16);
					int start = dataSegStart;
					while (true) {
						IOUtils.SearchResult res = IOUtils.searchForInt32(arm9io, start, -1, hash);
						if (res != null) {
							arm9io.readInt();
							int subhash = arm9io.readInt();
							String path1 = String.format(".\\%08x.%08x.geometry.bin", hash, subhash);
							String path2 = String.format(".\\%08x.%08x.bones.bin", hash, subhash);
							if (gfc.getFileInfo(path1) != null) {
								System.out.println(path1);
							}
							if (gfc.getFileInfo(path2) != null) {
								System.out.println(path2);
							}
							start = arm9io.getPosition() + 16;
						} else {
							break;
						}
					}
				}
			}
			s.close();
		} catch (IOException ex) {
			Logger.getLogger(GFC.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void scanForG3dFiles() {
		Set<Integer> knownHashes = new HashSet<>();

		GFC g = new GFC(new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data\\gob\\main.gfc"));
		for (FileInfo i : g.files) {
			knownHashes.add(i.hash);
		}

		char[][] buffers = new char[][]{
			".\\00000000.collisionspheres.bin".toCharArray(),
			".\\00000000.animation.bin".toCharArray(),
			".\\00000000.textureinfo.bin".toCharArray(),
			".\\00000000.texture.bin".toCharArray()
		};
		for (long i = 0; i < 0xFFFFFFFFL; i++) {
			addCharArr(buffers[0]);
			for (int x = 2; x < 10; x++) {
				buffers[1][x] = buffers[0][x];
				buffers[2][x] = buffers[0][x];
				buffers[3][x] = buffers[0][x];
			}
			for (char[] buf : buffers) {
				if (knownHashes.contains(GFCCRC.getPathHash(buf))) {
					System.out.println(new String(buf));
				}
			}
		}
	}

	private static void filterStrings() {
		//reduce hash collision
		try {
			FSFile strings = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\strings.txt");
			FSFile arm9 = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\arm9.bin");
			DataIOStream arm9io = arm9.getDataIOStream();
			int dataSegStart = 0x7C398;
			Scanner s = new Scanner(strings.getNativeInputStream());
			while (s.hasNextLine()) {
				String line = s.nextLine();
				if (line.contains("collisionspheres") || line.contains("textureinfo") || line.contains("animation")) {
					int hash = Integer.parseUnsignedInt(line.substring(2, 2 + 8), 16);
					if (IOUtils.searchForInt32(arm9io, dataSegStart, -1, 4, hash) != null) {
						System.out.println(line);
					}
				} else {
					System.out.println(line);
				}
			}
			s.close();
		} catch (IOException ex) {
			Logger.getLogger(GFC.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void filterTextureStrings() {
		//reduce hash collision
		try {
			FSFile strings = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\strings.txt");
			FSFile uncompRoot = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data_uncomp\\gob");
			Set<Integer> possibleTexHashes = new HashSet<>();
			for (FSFile ch : uncompRoot.listFiles()) {
				if (ch.getName().endsWith(".textureinfo.bin")) {
					DataInStream in = ch.getDataInputStream();
					int count = in.getLength() >> 2;
					for (int i = 0; i < count; i++) {
						possibleTexHashes.add(in.readInt());
					}
					in.close();
				}
			}

			Scanner s = new Scanner(strings.getNativeInputStream());
			while (s.hasNextLine()) {
				String line = s.nextLine();
				if (line.endsWith(".texture.bin")) {
					int hash = Integer.parseUnsignedInt(line.substring(2, 2 + 8), 16);
					if (possibleTexHashes.contains(hash)) {
						System.out.println(line);
					}
				} else {
					System.out.println(line);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(GFC.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void findSfxNames() {
		Scanner sx = new Scanner(new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data_uncomp\\gob\\sound_ui.xml").getNativeInputStream());
		while (sx.hasNextLine()) {
			String line = sx.nextLine();
			int index = line.indexOf("sfx");
			if (index != -1) {
				int end = line.indexOf("<", index);
				if (end != -1) {
					System.out.println(".\\" + line.substring(index, end));
				}
			}
		}
	}
	
	private static List<String> loadStrings() {
		List<String> strings = new ArrayList<>();
		Scanner s = new Scanner(new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\strings.txt").getNativeInputStream());
		while (s.hasNextLine()) {
			strings.add(s.nextLine());
		}
		s.close();
		return strings;
	}
	
	private static void corruptUnknownFilesInGfc() {
		FSFile gfc = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data\\gob\\main.gfc");
		FSFile gfced = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data\\gob\\main_ed.gfc");
		GFC g = new GFC(gfc);
		Set<Integer> myHashes = new HashSet<>();
		for (String s : loadStrings()) {
			myHashes.add(GFCCRC.getPathHash(s));
		}
		for (FileInfo fi : g.files) {
			if (!myHashes.contains(fi.hash)) {
				fi.firstChunkId = -1;
			}
		}
		g.write(gfced);
	}

	public static void main(String[] args) {
		FSFile outDir = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data_uncomp");
		FSFile gob = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data\\gob\\main.gob");
		FSFile gfc = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data\\gob\\main.gfc");

		new GFC(gfc).dumpGOB(gob, outDir, "gob", loadStrings());
	}

	public void dumpGOB(FSFile gob, FSFile outDir, String rootPath, List<String> fileNames) {
		Map<Integer, String> nameHashMap = new HashMap<>();
		for (String fn : fileNames) {
			nameHashMap.put(GFCCRC.getPathHash(fn), fn);
		}

		FSFile outRoot = outDir.getChild(rootPath);
		outRoot.mkdirs();

		int recognizedCount = 0;
		for (FileInfo fi : files) {
			String name = nameHashMap.get(fi.hash);
			FSFile out;
			if (name != null) {
				out = outRoot.getChild(name);
				recognizedCount++;
			} else {
				out = outRoot.getChild("_Unknown").getChild(String.format("%08X.bin", fi.hash));
			}
			System.out.println("Dumping file " + (name == null ? "0x" + Integer.toHexString(fi.hash) : name));
			out.getParent().mkdirs();
			byte[] dumped = getFileData(gob, fi);
			if (dumped != null) {
				out.setBytes(dumped);
			} else {
				System.err.println("Error dumping " + name);
			}
		}
		System.out.println("Recognized " + (recognizedCount / (float) files.size() * 100f) + "% of files.");
	}

	public FileInfo getFileInfo(String path) {
		return getFileInfo(GFCCRC.getPathHash(path));
	}

	public FileInfo getFileInfo(long hash) {
		hash &= 0xFFFFFFFFL;
		int end = files.size();
		int start = 0;
		int mid;
		long val;

		while (start < end) {
			mid = start + ((end - start) >> 1);
			val = files.get(mid).hash & 0xFFFFFFFFL;
			if (val == hash) {
				return files.get(mid);
			} else if (hash > val) {
				start = mid + 1;
			} else {
				end = mid;
			}
		}
		return null;
	}

	public byte[] getFileData(FSFile gob, FileInfo file) {
		try {
			if (file.size == 0) {
				return new byte[0];
			}
			IOStream in = gob.getIO();
			DataIOStream out = new DataIOStream();
			int nextChunk = file.firstChunkId & 0xFFFF;
			while (nextChunk != 0x7FFF) {
				ChunkInfo c = chunks.get(nextChunk);
				in.seek(c.offset);
				switch (c.compression) {
					case '0':
						FSUtil.transferStreams(in, out, c.size);
						break;
					case 'b':
						VVCompressor.vvBlockCompress(in.readBytes(c.size), out, c.size);
						break;
					case 'z':
						InflaterInputStream compStream = new VicariousInflaterInputStream(in.getInputStream());
						FSUtil.transferStreams(new InputStreamReadable(compStream), out);
						break;
				}
				nextChunk = c.next;
			}
			in.close();
			out.close();
			if (out.getLength() != file.size) {
				//throw new RuntimeException("Decompressed length not matched! Expected " + file.size + " got " + out.getLength());
			}
			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(GFC.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static class ChunkInfo {

		public int size;
		public int offset;
		public int next;
		public char compression;

		public ChunkInfo(DataInput in) throws IOException {
			size = in.readInt();
			offset = in.readInt();
			next = in.readInt();
			compression = (char) (in.readByte());
			in.skipBytes(3);
		}
		
		public void write(DataOutput out) throws IOException {
			out.writeInt(size);
			out.writeInt(offset);
			out.writeInt(next);
			out.write(compression);
			out.write(0);
			out.writeShort(0);
		}
	}

	public static class FileInfo {

		public int hash;
		public int size;
		public int firstChunkId;

		public FileInfo(DataInput in) throws IOException {
			hash = in.readInt();
			size = in.readInt();
			firstChunkId = in.readInt();
		}
		
		public void write(DataOutput out) throws IOException {
			out.writeInt(hash);
			out.writeInt(size);
			out.writeInt(firstChunkId);
		}
	}
}
