package ctrmap.formats.ntr.nitrowriter.nsbtx;

import ctrmap.formats.ntr.nitroreader.common.Utils;
import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.util.texture.TextureCodec;
import ctrmap.renderer.util.texture.TextureConverter;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.util.IOUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import xstandard.io.util.BitConverter;

public class GETextureCompressor {

	public static final boolean GECOMP_DEBUG = false;

	private static void debugPrint(String str) {
		if (GECOMP_DEBUG) {
			System.out.println(str);
		}
	}

	/*public static void main(String[] args) {
		Texture tex = TextureConverter.readTextureFromFile(new DiskFile("D:\\_REWorkspace\\pokescript_genv\\_TEX\\DebugTex2.png"));
		List<PaletteEntry> gpal = new ArrayList<>();
		CompressedTextureResource cmpr = encodeCompressedTexture("text", tex.width, tex.height, GETextureConverter.createRGB5A1(TextureCodec.getRGBA(tex, tex.format)), gpal);

		BufferedImage img = debugDecode(tex.width, tex.height, getBlocksFromRaw(cmpr.getBytes()), cmpr.idxData);

		try {
			ImageIO.write(img, "png", new File("D:\\_REWorkspace\\pokescript_genv\\_TEX\\decode.png"));
		} catch (IOException ex) {
			Logger.getLogger(GETextureCompressor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}*/
	public static TexCmprOutput encodeCompressedTexture(String name, int width, int height, short[] colors) {
		ColorBlock[] blocks = getColorBlocks(width, height, colors);

		TextureCompressorTempData[] tempOut = new TextureCompressorTempData[colors.length / (4 * 4)];

		debugPrint("begin block compression");

		for (int i = 0; i < blocks.length; i++) {
			encodeBlock(blocks[i], i, tempOut);
		}

		debugPrint("begin optimization");
		debugPrint("unoptimized gpsz " + calcGPalSize(tempOut));

		//Forcefully compress CCCC/CCCT blocks to CCAT/CCII if they won't fit into palette index range
		int nAnyparent = 0;
		List<TextureCompressorTempData> fastableList = new ArrayList<>();
		for (int i = 0; i < tempOut.length; i++) {
			TextureCompressorTempData td = tempOut[i];
			td.useFast = td.wasFastDecidedAsBetterAnyway();
			recalculateParents(tempOut, td);
			if (td.parent == null) {
				if (!td.useFast) {
					fastableList.add(td);
				}
			} else {
				nAnyparent++;
			}
		}

		int gPalSizeNeed = calcGPalSize(tempOut);

		Collections.sort(fastableList);
		debugPrint("gpsz " + gPalSizeNeed);
		debugPrint("any parent " + nAnyparent);

		while (gPalSizeNeed > 16384) {
			if (!fastableList.isEmpty()) {
				TextureCompressorTempData temp = fastableList.remove(0);
				if (!temp.useFast) {
					temp.useFast = true;
					gPalSizeNeed--;

					if (recalculateParents(tempOut, temp)) {
						gPalSizeNeed--;
					}
				}
			} else {
				break;
			}
		}
		debugPrint("after gpszn " + gPalSizeNeed);

		int checksumGpalSize = 0;

		List<PaletteEntry> palette = new ArrayList<>(gPalSizeNeed);
		
		//Generate index data
		int[] blockData = new int[tempOut.length];
		IndexData[] idxData = new IndexData[tempOut.length];
		for (int i = 0; i < tempOut.length; i++) {
			TextureCompressorTempData parentTempData = tempOut[i];
			TextureCompressorTempData ogTempData = parentTempData;
			parentTempData = ogTempData.getTopmostParent();
			ColorBlock.ColorSetResult colorSet = parentTempData.useFast ? parentTempData.fast : parentTempData.full;

			PaletteEntry pe;

			if (parentTempData.resultPalEntry == null) {
				pe = new PaletteEntry();
				pe.colors = colorSet.getColors();
				pe.nRealColors = colorSet.mode.nRealColors;
				palette.add(pe);
				checksumGpalSize += parentTempData.useFast ? 1 : 2;

				parentTempData.resultPalEntry = pe;
				ogTempData.resultPalEntry = pe;
			} else { //is parented
				pe = parentTempData.resultPalEntry;
				ogTempData.resultPalEntry = pe;
			}

			//checksumGpalSize += dbgOgTempData.countedToPalTotal;
			IndexData id = new IndexData();
			id.mode = colorSet.mode;
			id.palEntry = pe;

			idxData[i] = id;
			blockData[i] = getBlockData(blocks[i], colorSet);
		}
		debugPrint("checksum gpal size " + checksumGpalSize);

		palette.sort((o1, o2) -> {
			return o1.getExistingColorLength() - o2.getExistingColorLength();
		});
		//globalPalette.get(globalPalette.size() - 1).isLastOfTexture = true;

		TexCmprOutput out = new TexCmprOutput();
		out.texData = getByteArr(blockData);
		out.indexData = idxData;
		out.cmprPalette = palette.toArray(new PaletteEntry[palette.size()]);
		return out;
	}

	private static boolean recalculateParents(TextureCompressorTempData[] tempdatas, TextureCompressorTempData changedElem) {
		if (changedElem.parent != null) {
			return false;
		}
		for (TextureCompressorTempData td : tempdatas) {
			if (td != changedElem) {
				if (changedElem.canBeChildOf(td, changedElem.useFast, td.useFast)) {
					changedElem.parent = td;
					return true;
				}
			} else {
				break;
			}
		}
		return false;
	}

	private static int calcGPalSize(TextureCompressorTempData[] tempdatas) {
		int out = 0;
		for (TextureCompressorTempData td : tempdatas) {
			if (td.getTopmostParent() == td) {
				out += td.useFast ? 1 : 2;
			}
		}
		return out;
	}

	public static BufferedImage debugDecode(int width, int height, int[] blocks, IndexData[] idxData) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		int idx = 0;
		for (int y = 0; y < height; y += 4) {
			for (int x = 0; x < width; x += 4) {
				int block = blocks[idx];
				IndexData id = idxData[idx];
				idx++;
				for (int y2 = 0; y2 < 4; y2++) {
					for (int x2 = 0; x2 < 4; x2++) {
						int colIdx = block & 3;
						block >>= 2;
						short color = id.palEntry.colors[colIdx];
						img.setRGB(x + x2, y + y2, Utils.GXColorToRGB(color));
					}
				}
			}
		}

		return img;
	}

	private static int[] getBlocksFromRaw(byte[] bArr) {
		int[] iArr = new int[bArr.length >> 2];

		for (int i = 0, j = 0; i < iArr.length; i++, j += 4) {
			iArr[i] = BitConverter.toInt32LE(bArr, j);
		}

		return iArr;
	}

	private static byte[] getByteArr(int[] iArr) {
		byte[] bArr = new byte[iArr.length * 4];

		for (int i = 0, j = 0; i < iArr.length; i++, j += 4) {
			BitConverter.fromInt32LE(iArr[i], bArr, j);
		}

		return bArr;
	}

	private static ColorBlock[] getColorBlocks(int width, int height, short[] colors) {
		ColorBlock[] blocks = new ColorBlock[colors.length / (4 * 4)];
		int blockIndex = 0;
		for (int y = 0; y < height; y += 4) {
			for (int x = 0; x < width; x += 4) {
				ColorBlock blk = new ColorBlock();

				int blockColIndex = 0;
				for (int bY = y; bY < y + 4; bY++) {
					int offsVert = bY * width;
					for (int bX = x; bX < x + 4; bX++) {
						blk.colors[blockColIndex] = colors[offsVert + bX];
						blockColIndex++;
					}
				}

				blocks[blockIndex] = blk;
				blockIndex++;
			}
		}
		return blocks;
	}

	private static void encodeBlock(ColorBlock block, int blockIndex, TextureCompressorTempData[] out) {
		block.process();

		TextureCompressorTempData outData = new TextureCompressorTempData();

		if (block.uniqueColors.size() <= 2) {
			ColorBlock.ColorSetResult rsl = new ColorBlock.ColorSetResult(3, BlockColorMode.CCAT, !block.hasTransparency);
			block.getUCRGBToArrays(rsl.r, rsl.g, rsl.b, 0, block.uniqueColors.size() - 1);
			ColorBlock.makeAvg123(rsl.r, rsl.g, rsl.b);
			outData.fast = rsl;
		} else {
			ColorBlock.ColorSetResult rsl2 = block.getColorSetForModeCCAT();
			if (block.hasTransparency) {
				outData.fast = rsl2;
				ColorBlock.ColorSetResult rsl1 = block.getColorSetForModeCCCT();

				if (rsl2 == null || rsl1.diffTotal < rsl2.diffTotal) {
					outData.full = rsl1;
				}
			} else {
				ColorBlock.ColorSetResult rsl3 = block.getColorSetForModeCCCC(); //no point in even trying CCCT since the 3 colors have to get aligned
				ColorBlock.ColorSetResult rsl4 = block.getColorSetForModeCCII();

				if (rsl4.diffTotal < rsl2.diffTotal) {
					outData.fast = rsl4;
				} else {
					outData.fast = rsl2;
				}

				if (outData.fast.diffTotal > rsl3.diffTotal) {
					outData.full = rsl3;
				}
			}
		}

		out[blockIndex] = outData;
	}

	private static int findAcceptingPaletteIndex(List<PaletteEntry> palettes, PaletteEntry cand) {
		for (int i = 0; i < palettes.size(); i++) {
			if (palettes.get(i).accepts(cand)) {
				return i;
			}
		}
		return -1;
	}

	private static int getBlockData(ColorBlock blk, ColorBlock.ColorSetResult colorSet) {
		int result = 0;
		for (int i = 0, j = 0; i < blk.colors.length; i++, j += 2) {
			if (blk.colors[i] == 0) {
				result |= 3 << j;
			} else {
				result |= blk.getClosestColorIdx(colorSet, i) << j;
			}
		}
		return result;
	}

	public static class PaletteEntry {

		public short[] colors;

		private int nRealColors;

		public int getExistingColorLength() {
			return nRealColors;
		}

		public boolean accepts(PaletteEntry e) {
			if (e == this) {
				return true;
			}

			int ecl = nRealColors;
			int eecl = e.colors.length;
			if (ecl >= eecl) {
				for (int i = 0; i < eecl; i++) {
					if (colors[i] != e.colors[i]) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}

	private static class ColorBlock {

		public boolean hasTransparency;

		public final short[] colors = new short[4 * 4];

		public final byte[] cR = new byte[4 * 4];
		public final byte[] cG = new byte[4 * 4];
		public final byte[] cB = new byte[4 * 4];

		public final List<Short> uniqueColors = new ArrayList<>();

		public byte[] ucR;
		public byte[] ucG;
		public byte[] ucB;

		public void process() {
			//Detect transparency
			for (int i = 0; i < colors.length; i++) {
				if ((colors[i] & 0x8000) == 0) {
					hasTransparency = true;
					colors[i] = 0;
				}
			}

			for (int i = 0; i < colors.length; i++) {
				if (colors[i] != 0) {
					if (!uniqueColors.contains(colors[i])) {
						uniqueColors.add(colors[i]);
					}

					cR[i] = getR(colors[i]);
					cG[i] = getG(colors[i]);
					cB[i] = getB(colors[i]);
				}
			}

			Collections.sort(uniqueColors);

			ucR = new byte[uniqueColors.size()];
			ucG = new byte[ucR.length];
			ucB = new byte[ucR.length];

			for (int i = 0; i < ucR.length; i++) {
				short uc = uniqueColors.get(i);
				ucR[i] = getR(uc);
				ucG[i] = getG(uc);
				ucB[i] = getB(uc);
			}
		}

		private int getClosestColorIdx(ColorBlock.ColorSetResult cs, int colIndex) {
			int minDiff = Integer.MAX_VALUE;
			int minDiffIdx = -1;
			int diff;

			for (int i = 0; i < cs.r.length; i++) {
				diff = Math.abs(cR[colIndex] - cs.r[i]) + Math.abs(cG[colIndex] - cs.g[i]) + Math.abs(cB[colIndex] - cs.b[i]);

				if (diff < minDiff) {
					minDiff = diff;
					minDiffIdx = i;
				}
			}

			return minDiffIdx;
		}

		public void getUCRGBToArrays(byte[] r, byte[] g, byte[] b, int... indices) {
			int max = ucR.length - 1;
			for (int i = 0; i < indices.length; i++) {
				int index = indices[i];
				if (index > max) {
					index = max;
				}
				if (index < 0) {
					continue;
				}
				r[i] = ucR[index];
				g[i] = ucG[index];
				b[i] = ucB[index];
			}
		}

		public static void makeAvg123(byte[]... arrays) {
			for (int i = 0; i < arrays.length; i++) {
				arrays[i][2] = (byte) ((arrays[i][0] + arrays[i][1]) / 2);
			}
		}

		public static void makeInterp1234(byte[]... arrays) {
			for (int i = 0; i < arrays.length; i++) {
				arrays[i][2] = (byte) ((arrays[i][0] * 5 + arrays[i][1] * 3) / 8);
				arrays[i][3] = (byte) ((arrays[i][0] * 3 + arrays[i][1] * 5) / 8);
			}
		}

		public ColorSetResult getColorSetForModeCCCT() {
			ColorSetResult currentSet = new ColorSetResult(3, BlockColorMode.CCCT);

			List<Short> palette = new ArrayList<>(uniqueColors);

			GETextureConverter.limitPalette(palette, 3);
			for (int i = 0; i < Math.min(3, palette.size()); i++) {
				short c = palette.get(i);
				currentSet.r[i] = getR(c);
				currentSet.g[i] = getG(c);
				currentSet.b[i] = getB(c);
			}

			currentSet.diffTotal = addUpDiffForColSet(currentSet.r, currentSet.g, currentSet.b);

			return currentSet;
		}

		public ColorSetResult getColorSetForModeCCAT() {
			ColorSetResult currentSet = new ColorSetResult(3, BlockColorMode.CCAT);

			List<Short> palette = new ArrayList<>(uniqueColors);

			GETextureConverter.limitPalette(palette, 2);
			for (int i = 0; i < Math.min(2, palette.size()); i++) {
				short c = palette.get(i);
				currentSet.r[i] = getR(c);
				currentSet.g[i] = getG(c);
				currentSet.b[i] = getB(c);
			}
			makeAvg123(currentSet.r, currentSet.g, currentSet.b);

			currentSet.diffTotal = addUpDiffForColSet(currentSet.r, currentSet.g, currentSet.b);

			return currentSet;
		}

		public ColorSetResult getColorSetForModeCCCC() {
			ColorSetResult currentSet = new ColorSetResult(4, BlockColorMode.CCCC);

			List<Short> palette = new ArrayList<>(uniqueColors);

			GETextureConverter.limitPalette(palette, 4);
			for (int i = 0; i < Math.min(4, palette.size()); i++) {
				short c = palette.get(i);
				currentSet.r[i] = getR(c);
				currentSet.g[i] = getG(c);
				currentSet.b[i] = getB(c);
			}

			currentSet.diffTotal = addUpDiffForColSet(currentSet.r, currentSet.g, currentSet.b);

			return currentSet;
		}

		public ColorSetResult getColorSetForModeCCII() {
			ColorSetResult currentSet = new ColorSetResult(4, BlockColorMode.CCII);

			List<Short> palette = new ArrayList<>(uniqueColors);

			GETextureConverter.limitPalette(palette, 2);
			for (int i = 0; i < Math.min(2, palette.size()); i++) {
				short c = palette.get(i);
				currentSet.r[i] = getR(c);
				currentSet.g[i] = getG(c);
				currentSet.b[i] = getB(c);
			}
			makeInterp1234(currentSet.r, currentSet.g, currentSet.b);

			currentSet.diffTotal = addUpDiffForColSet(currentSet.r, currentSet.g, currentSet.b);

			return currentSet;
		}

		private int addUpDiffForColSet(byte[] r, byte[] g, byte[] b) {
			int diffTotal = 0;
			for (int ci = 0; ci < cR.length; ci++) {
				if (colors[ci] == 0) {
					continue;
				}
				int minDiff = Integer.MAX_VALUE;

				for (int i = 0; i < r.length; i++) {
					int diff = Math.abs(cR[ci] - r[i]) + Math.abs(cG[ci] - g[i]) + Math.abs(cB[ci] - b[i]);

					if (diff < minDiff) {
						minDiff = diff;
					}
				}

				diffTotal += minDiff;
			}
			return diffTotal;
		}

		private static class ColorSetResult {

			public final boolean isOptimizedCCAT;
			public final BlockColorMode mode;

			public final byte[] r;
			public final byte[] g;
			public final byte[] b;

			public int diffTotal;

			public ColorSetResult(int colCount, BlockColorMode mode) {
				this(colCount, mode, false);
			}

			public ColorSetResult(int colCount, BlockColorMode mode, boolean isOptimizedCCAT) {
				this.mode = mode;
				this.isOptimizedCCAT = isOptimizedCCAT;
				r = new byte[colCount];
				g = new byte[colCount];
				b = new byte[colCount];
			}

			public boolean contains(ColorSetResult other) {
				boolean modeCompatible = false;
				switch (other.mode) {
					case CCAT:
					case CCCT:
						modeCompatible = mode == BlockColorMode.CCAT || mode == BlockColorMode.CCCT || (isOptimizedCCAT && (mode == BlockColorMode.CCCC || mode == BlockColorMode.CCII)); //must have transparency
						break;
					case CCCC:
						modeCompatible = mode == BlockColorMode.CCCC; //only CCCC can contain another CCCC
						break;
					case CCII:
						modeCompatible = mode == BlockColorMode.CCCC || mode == BlockColorMode.CCII;
						break;
				}

				if (modeCompatible) {
					Outer:
					for (int i = 0; i < other.r.length; i++) {
						short col = other.getColor(i);
						for (int j = 0; j < r.length; j++) {
							short refCol = getColor(j);
							if (refCol == col) {
								continue Outer;
							}
						}
						return false;
					}
					return true;
				}
				return false;
			}

			public short getColor(int index) {
				return (short) (r[index] | (g[index] << 5) | (b[index] << 10));
			}

			public short[] getColors() {
				short[] colors = new short[r.length];
				for (int i = 0; i < colors.length; i++) {
					colors[i] = getColor(i);
				}
				return colors;
			}
		}

		public static byte getR(short col) {
			return (byte) (col & 31);
		}

		public static byte getG(short col) {
			return (byte) ((col >> 5) & 31);
		}

		public static byte getB(short col) {
			return (byte) ((col >> 10) & 31);
		}

		public static int rgb5to8(int value) {
			return value << 3 | value >>> 2;
		}
	}

	public static class TextureCompressorTempData implements Comparable<TextureCompressorTempData> {

		public ColorBlock.ColorSetResult full;
		public ColorBlock.ColorSetResult fast;

		public int lastGPalCalcInfluence;
		public boolean useFast;

		public TextureCompressorTempData parent;

		public List<TextureCompressorTempData> children = new ArrayList<>();

		public PaletteEntry resultPalEntry;

		public TextureCompressorTempData getTopmostParent() {
			if (parent == null) {
				return this;
			}
			return parent.getTopmostParent();
		}

		public ColorBlock.ColorSetResult getCSR(boolean fast) {
			return fast ? this.fast : full;
		}

		public void addChild(TextureCompressorTempData child) {
			if (!children.contains(child)) {
				children.add(child);
			}
		}

		public boolean canBeChildOf(TextureCompressorTempData another, boolean fastThis, boolean fastParent) {
			ColorBlock.ColorSetResult src = fastThis ? fast : full;
			if (src == null) {
				return false;
			}
			ColorBlock.ColorSetResult parentSrc = fastParent ? another.fast : another.full;
			if (parentSrc == null) {
				return false;
			}
			return parentSrc.contains(src);
		}

		public boolean wasFastDecidedAsBetterAnyway() {
			return full == null;
		}

		public int getFastFullDiff() {
			return fast.diffTotal - full.diffTotal;
		}

		@Override
		public int compareTo(TextureCompressorTempData o) {
			return getFastFullDiff() - o.getFastFullDiff();
		}
	}

	public static enum BlockColorMode {
		/*
		C = Color
		T = Transparent
		A = Average ((C1 + C2) / 2)
		I = Interpolated - (C1 * 5/8 + C2 * 3/8), then vice versa
		 */

		CCCT(3),
		CCAT(2),
		CCCC(4),
		CCII(2);

		public final int nRealColors;

		private BlockColorMode(int nRealColors) {
			this.nRealColors = nRealColors;
		}
	}
	
	public static class IndexData {
		public BlockColorMode mode;
		public PaletteEntry palEntry;
	}
	
	public static class TexCmprOutput implements IGETextureIntermediate {
		public byte[] texData;
		public IndexData[] indexData;
		public PaletteEntry[] cmprPalette;

		@Override
		public byte[] getTexData() {
			return texData;
		}
	}
}
