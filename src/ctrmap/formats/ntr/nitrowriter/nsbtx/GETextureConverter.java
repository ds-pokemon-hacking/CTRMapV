package ctrmap.formats.ntr.nitrowriter.nsbtx;

import ctrmap.formats.generic.interchange.CMIFTextureFormat;
import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.util.texture.TextureProcessor;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.util.BitWriter;
import xstandard.math.vec.RGBA;
import xstandard.util.ArraysEx;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.io.util.BitConverter;

public class GETextureConverter {

	public static final boolean DBG_ENABLE_CMPR_FORMAT = true;

	public static GETCOutput convertTextureGenericFormat(Texture tex, NNSWriterLogger log) {
		CMIFTextureFormat genFmt = ReservedMetaData.getDesiredTextureFormat(tex.metaData, CMIFTextureFormat.AUTO);

		switch (genFmt) {
			case AUTO:
				return convertTextureAutoFormat(tex, log);
			case COMPRESSED:
				return convertTexture(new GETCInput(tex, GETextureFormat.IDXCMPR), log);
			case FULL_COLOR:
				return convertTexture(new GETCInput(tex, GETextureFormat.RGB5A1), log);
			case REDUCED_COLOR:
				GETCInput in = new GETCInput(tex, GETextureFormat.NULL);
				short[] palette = getSortedUniqueColors5A1(in.colors, in.alphaChannel);
				int[] alphas = TextureProcessor.getUniqueAlphaValues(in.alphaChannel);
				in.outFormat = decideIndexedTexFormat(palette, alphas);

				return convertTexture(in, log);
		}

		return null;
	}

	public static GETCOutput convertTextureAutoFormat(Texture tex, NNSWriterLogger log) {
		byte[] rgba = tex.format.getRGBA(tex);

		short[] colors = createRGB5A1(rgba);
		short[] alphaChannel = TextureProcessor.getAlphaChannel(rgba);
		short[] uniqueColors = getSortedUniqueColors5A1(colors, alphaChannel);

		int[] alphas = TextureProcessor.getUniqueAlphaValues(alphaChannel);

		boolean isOpaque = alphas.length == 1 && alphas[0] == 255;

		int width = tex.width;
		int height = tex.height;
		GETextureFormat format = GETextureFormat.NULL;

		/*
		Format decision:
		
		SWITCH [MAIN]
		
		Does the texture have non-bipolar alpha?
			
			- Yes -> SWITCH[A#I#]
			- No  -> SWITCH[NonAlpha]
		
		SWITCH[A#I#]
		
		Does the texture color count fit the range of...
		
			- [1 ...  8] -> A5I3
			- [9 ... 32] -> A3I5
			- [32 ++   ] -> A3I5, reduced palette
		
		SWITCH[NonAlpha]
		
			* TF: The transparency factor is a value of 0 to 1, determining if the 0th palette index is a fully transparent value
		
		How many unique colors?
		
			- [1  ...   4 - TF] -> I2
			- [5  ...  16 - TF] -> I4
			- [17 ... 256 - TF] -> SWITCH[Under256]
			- [256 ++         ] -> SWITCH[Over256]
		
		SWITCH[Under256]
		
		Decide base on (R)esolution (x/y = horizontal/vertical) and (C)olor count (lower number takes priority)
		
			1) (Rx * Ry) + (C * 2) > Rx * Ry * 2 -> RGB5A1
			2)           (Rx + Ry) < 256         -> I8
			3)                   0 = 0           -> CMPR
		
		SWITCH[Over256]
		
			1) (Rx + Ry) < 256 -> RGB5A1
			2)         0 = 0   -> CMPR
		
		 */
		int TF = !isOpaque ? 1 : 0;

		if (uniqueColors.length <= 256 - TF) {
			format = decideIndexedTexFormat(uniqueColors, TextureProcessor.getUniqueAlphaValues(alphaChannel));

			if (format == GETextureFormat.IDX8) {
				if (width * height * 2 < width * height + (uniqueColors.length * 2 + TF)) {
					//If the raw size of the RGB5A1 data is lower than the raw size of a 8bpp-indexed texture + its palette,
					//it's more efficient to just write out the raw data
					//Needless to say, this scenario is rather unlikely
					format = GETextureFormat.RGB5A1;
				} else if (width + height < 256 || !DBG_ENABLE_CMPR_FORMAT) {
					//The resolution is lower than 128x128

					format = GETextureFormat.IDX8;
				} else {
					format = GETextureFormat.IDXCMPR;
				}
			}
		} else {
			if (width + height < 256 || !DBG_ENABLE_CMPR_FORMAT) {
				//I think 128x128 is the minimum resolution for which the highly lossy compression algorithm makes sense
				//For this reason, textures with a lower resolution are left as RGB5A1

				format = GETextureFormat.RGB5A1;
			} else {
				format = GETextureFormat.IDXCMPR;
			}
		}

		GETCInput convInput = new GETCInput();
		convInput.texName = tex.name;
		convInput.palName = getPalName(tex);
		convInput.outFormat = format;
		convInput.width = width;
		convInput.height = height;
		convInput.colors = colors;
		convInput.alphaChannel = alphaChannel;

		return convertTexture(convInput, log);
	}

	private static String getPalName(Texture tex) {
		return ReservedMetaData.getIdxTexPalName(tex);
	}

	public static GETextureFormat decideIndexedTexFormat(short[] palette, int[] alphas) {
		boolean isAlphaBipolar = alphas.length == 2 && ((alphas[0] == 255 && alphas[1] == 0) || (alphas[0] == 0 && alphas[1] == 255));
		boolean isOpaque = alphas.length == 1 && alphas[0] == 255;

		int uniqueColorsCount = palette.length;

		GETextureFormat format = GETextureFormat.IDX8;
		if (!isOpaque && !isAlphaBipolar) {
			if (uniqueColorsCount <= 4) {
				format = GETextureFormat.A5I3;
			} else {
				format = GETextureFormat.A3I5;
			}
		} else {
			int TF = !isOpaque ? 1 : 0;

			if (uniqueColorsCount <= 4 - TF) {
				format = GETextureFormat.IDX2;
			} else if (uniqueColorsCount <= 16 - TF) {
				format = GETextureFormat.IDX4;
			} else if (uniqueColorsCount <= 256 - TF) {
				format = GETextureFormat.IDX8;
			}
		}
		return format;
	}

	public static GETCOutput convertTexture(GETCInput input, NNSWriterLogger log) {
		GETCOutput out = new GETCOutput();

		String name = input.texName;

		GETextureFormat format = input.outFormat;
		out.format = format;

		int width = input.width;
		int height = input.height;

		byte[] outData = null;

		short[] colors = input.colors;

		short[] palette = null;
		boolean isPaletteTransparent = false;

		switch (format) {
			case NULL:
				break;
			case A3I5:
			case A5I3:
			case IDX2:
			case IDX4:
			case IDX8:
				if (input.alphaChannel != null && !(input.outFormat == GETextureFormat.A3I5 || input.outFormat == GETextureFormat.A5I3)) {
					int[] uniqueAlphaValues = TextureProcessor.getUniqueAlphaValues(input.alphaChannel);
					isPaletteTransparent = !(uniqueAlphaValues.length == 1 && uniqueAlphaValues[0] == 255);
				}
				palette = makePaletteForZeroIdxTransparency(getSortedUniqueColors5A1(colors, input.alphaChannel), isPaletteTransparent);
				if (palette.length > format.indexMax) {
					if (format == GETextureFormat.A3I5) {
						log.err("Can not export a non-bipolar alpha texture that has more than 32 colors ! ! - " + name + ", limiting palette...");
					} else if (format == GETextureFormat.IDX8) {
						log.err("Can not export an indexed texture with more than 256 colors ! ! - " + name + ", limiting palette...");
					} else {
						log.err("Encoding this format with reduced palette is not supported. (Texture: " + name + ", Format: " + format + ")");
						return out;
					}
				}

				outData = encodeIndexed(colors, input.alphaChannel, palette, format);

				/*BufferedImage testOut = new BufferedImage(input.width, input.height, BufferedImage.TYPE_INT_RGB);

				if (format == GETextureFormat.IDX8) {
					int i = 0;
					for (int y = 0; y < input.height; y++) {
						for (int x = 0; x < input.width; x++) {
							testOut.setRGB(x, y, rgba5a1ToArgb(palette[outData[i] & 0xFF]));
							i++;
						}
					}
					{
						try {
							ImageIO.write(testOut, "png", new File("D:\\_REWorkspace\\pokescript_genv\\_TEX\\dbgout.png"));
						} catch (IOException ex) {
							Logger.getLogger(GETextureConverter.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}*/
				//Resize palette array after encoding did its job
				if (palette.length > format.indexMax) {
					palette = Arrays.copyOf(palette, format.indexMax);
				}
				break;

			case RGB5A1:
				outData = encodeRGB5A1(colors);
				break;
			case IDXCMPR:
				out.intermediate = GETextureCompressor.encodeCompressedTexture(name, width, height, colors);
				break;
		}

		if (outData != null) {
			if (out.intermediate == null) {
				if (palette != null) {
					out.intermediate = new IntermediateIndexed(outData, palette, isPaletteTransparent);
				} else {
					out.intermediate = new IntermediateRaw(outData);
				}
			}
		}

		return out;
	}

	private static int rgba5a1ToArgb(short rgba5a1) {
		int r = rgba5a1 & 31;
		int g = (rgba5a1 >> 5) & 31;
		int b = (rgba5a1 >> 10) & 31;
		int a = rgba5a1 >>> 16;
		return (255 << 24) | (r << 19) | (g << 11) | (b << 3);
	}

	public static byte[] encodeIndexed(short[] colors, short[] alphaChannel, short[] palette, GETextureFormat format) {
		byte[] outData = null;
		switch (format) {
			case A3I5:
				if (palette.length > GETextureFormat.A3I5.indexMax) {
					outData = encodeA3I5Over32(colors, alphaChannel, palette);
				} else {
					outData = encodeA3I5(colors, alphaChannel, palette);
				}
				break;
			case A5I3:
				outData = encodeA5I3(colors, alphaChannel, palette);
				break;
			case IDX2:
				outData = encodeIdx2(colors, palette);
				break;
			case IDX4:
				outData = encodeIdx4(colors, palette);
				break;
			case IDX8:
				if (palette.length > 256) {
					outData = encodeIdx8Over256(colors, palette);
				} else {
					outData = encodeIdx8(colors, palette);
				}
				break;
		}
		return outData;
	}

	public static boolean rangeCheckPalette(String name, short[] palette, int maximum, NNSWriterLogger log) {
		if (palette.length > maximum) {
			log.err("Texture " + name + " has more than " + maximum + " unique colors allowed by format! Skipping.");
			return false;
		}
		return true;
	}

	public static short[] makePaletteForZeroIdxTransparency(short[] palette, boolean hasTransparency) {
		if (!hasTransparency) {
			return palette;
		}
		short[] newPalette = new short[palette.length + 1];
		System.arraycopy(palette, 0, newPalette, 1, palette.length);
		return newPalette;
	}

	public static byte[] encodeIdx2(short[] colors, short[] palette) {
		return encodeIdxNon8Impl(colors, palette, 2);
	}

	public static byte[] encodeIdx4(short[] colors, short[] palette) {
		return encodeIdxNon8Impl(colors, palette, 4);
	}

	private static byte[] encodeIdxNon8Impl(short[] colors, short[] palette, int bpp) {
		try {
			DataIOStream out = new DataIOStream();
			BitWriter writer = new BitWriter(out);

			short col;
			for (int i = 0; i < colors.length; i++) {
				col = colors[i];
				if ((col & 0x8000) == 0) {
					//transparent
					writer.writeBits(0, bpp); //index 0 is used for transparency
				} else {
					writer.writeBits(Arrays.binarySearch(palette, (short) (col & 0x7FFF)), bpp);
				}
			}
			writer.flush();

			out.close();
			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(GETextureConverter.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static byte[] encodeIdx8(short[] colors, short[] palette) {
		byte[] out = new byte[colors.length];

		short col;
		for (int i = 0; i < colors.length; i++) {
			col = colors[i];
			if ((col & 0x8000) == 0) {
				out[i] = 0;
			} else {
				out[i] = (byte) Arrays.binarySearch(palette, (short) (colors[i] & 0x7FFF));
			}
		}

		return out;
	}

	public static void makeLimitedPaletteFromColors(short[] colors, short[] palette, int limit) {
		short[] in = colors.clone();
		limitPalette(in, limit);
		System.arraycopy(in, 0, palette, 0, Math.min(palette.length, limit));
	}

	public static void makeLimitedPaletteFromColors(short[] colors, List<Short> palette, int limit) {
		palette.clear();
		for (short col : colors) {
			palette.add(col);
		}
		limitPalette(palette, limit);
	}

	public static byte[] encodeIdx8Over256(short[] colors, short[] palette) {
		makeLimitedPaletteFromColors(colors, palette, 256);

		byte[] out = new byte[colors.length];

		short col;
		int bestDiff;
		int bestMatchIdx;
		for (int i = 0; i < out.length; i++) {
			col = colors[i];
			if ((col & 0x8000) == 0) {
				out[i] = 0;
			} else {
				bestDiff = Integer.MAX_VALUE;
				bestMatchIdx = -1;

				for (int j = 0; j < palette.length; j++) {
					int diff = getColorDiff(palette[j], col);
					if (diff < bestDiff) {
						bestMatchIdx = j;
						bestDiff = diff;
					}
				}

				out[i] = (byte) (bestMatchIdx);
			}
		}

		return out;
	}

	public static byte[] encodeRGB5A1(short[] rgb5a1) {
		byte[] out = new byte[rgb5a1.length * 2];

		for (int i = 0, j = 0; i < rgb5a1.length; i++, j += 2) {
			out[j + 0] = (byte) (rgb5a1[i] & 0xFF);
			out[j + 1] = (byte) ((rgb5a1[i] >> 8) & 0xFF);
		}

		return out;
	}

	public static byte[] encodeA5I3(short[] colors, short[] alphaChannel, short[] palette) {
		if (palette.length > 4) {
			throw new IllegalArgumentException("'palette' length (" + palette.length + ") is out of range for A5I3 format!");
		}
		byte[] out = new byte[colors.length];

		for (int i = 0; i < out.length; i++) {
			out[i] = (byte) (Arrays.binarySearch(palette, (short) (colors[i] & 0x7FFF)) | (alphaChannel[i] & 0b11111000));
		}

		return out;
	}

	public static void limitPalette(short[] palette, int limit) {
		List<Short> pal = new ArrayList<>(palette.length);
		for (short c : palette) {
			pal.add(c);
		}
		limitPalette(pal, limit);
		for (int i = 0; i < pal.size(); i++) {
			palette[i] = pal.get(i);
		}
	}

	public static void limitPalette(List<Short> pal, int limit) {
		if (pal.size() <= limit) {
			return;
		}
		pal.sort(new Comparator<Short>() {
			@Override
			public int compare(Short o1, Short o2) {
				RGBA c1 = getRGBA(o1);
				RGBA c2 = getRGBA(o2);
				float[] hsb1 = new float[3];
				float[] hsb2 = new float[3];
				Color.RGBtoHSB(c1.r, c1.g, c1.b, hsb1);
				Color.RGBtoHSB(c2.r, c2.g, c2.b, hsb2);
				return (int) (getHSBCmpInfluence(hsb1) - getHSBCmpInfluence(hsb2));
			}
		});
		for (int i = 0; i < limit; i++) {
			pal.set(i, pal.get(i * pal.size() / limit));
		}
	}

	private static int getHSBCmpInfluence(float[] hsb) {
		return (int) (65536f * (hsb[0]) * hsb[2]);
	}

	public static int getColorDiff(short c1, short c2) {
		return Math.abs((c1 & 31) - (c2 & 31)) + Math.abs(((c1 >> 5) & 31) - ((c2 >> 5) & 31)) + Math.abs(((c1 >> 10) & 31) - ((c2 >> 10) & 31));
	}

	public static byte[] encodeA3I5Over32(short[] colors, short[] alphaChannel, short[] palette) {
		makeLimitedPaletteFromColors(colors, palette, 32);

		byte[] out = new byte[colors.length];

		for (int i = 0; i < out.length; i++) {
			int bestDiff = Integer.MAX_VALUE;
			int bestMatchIdx = -1;

			for (int j = 0; j < 32; j++) {
				int diff = getColorDiff(palette[j], colors[i]);
				if (diff < bestDiff) {
					bestMatchIdx = j;
					bestDiff = diff;
				}
			}

			out[i] = (byte) (bestMatchIdx | (alphaChannel[i] & 0b11100000));
		}

		return out;
	}

	public static byte[] encodeA3I5(short[] colors, short[] alphaChannel, short[] palette) {
		if (palette.length > 32) {
			throw new IllegalArgumentException("'palette' length (" + palette.length + ") is out of range for A3I5 format!");
		}
		byte[] out = new byte[colors.length];

		for (int i = 0; i < out.length; i++) {
			out[i] = (byte) (Arrays.binarySearch(palette, (short) (colors[i] & 0x7FFF)) | (alphaChannel[i] & 0b11100000));
		}

		return out;
	}

	public static RGBA getRGBA(short color) {
		return new RGBA(color & 31, (color >> 5) & 31, (color >> 10) & 31, 255);
	}

	public static short[] createRGB5A1(byte[] rgba) {
		short[] out = new short[rgba.length / 4];

		int dec;
		short enc;
		for (int i = 0, j = 0; i < rgba.length; i += 4, j++) {
			dec = BitConverter.toInt32LE(rgba, i);

			//0xFF000000 is faster than shifting
			enc = (short) (((dec >> 3) & 0x1F)
				| ((dec >> 6) & 0x3E0)
				| ((dec >> 9) & 0x7C00)
				| ((dec & 0xFF000000) == 0xFF000000 ? 0x8000 : 0));

			out[j] = enc;
		}

		return out;
	}

	public static int getUniqueColorCount(short[] colors, short[] alphaChannel) {
		return getUniqueColors5A1(colors, alphaChannel).length;
	}

	public static short[] getUniqueColors5A1(short[] rgb5a1, short[] alphaChannel) {
		HashSet<Short> colors = new HashSet<>();

		short rgb;
		for (int i = 0; i < rgb5a1.length; i++) {
			if (alphaChannel != null && (alphaChannel[i] == 0)) {
				continue; //need precise alpha value to skip this
			}
			rgb = (short) (rgb5a1[i] & 0x7FFF);
			if (!colors.contains(rgb)) {
				colors.add(rgb);
			}
		}

		short[] arr = ArraysEx.asArrayS(new ArrayList<>(colors));
		return arr;
	}
	
	public static short[] getSortedUniqueColors5A1(short[] rgb5a1, short[] alphaChannel) {
		short[] arr = getUniqueColors5A1(rgb5a1, alphaChannel);
		Arrays.sort(arr);
		return arr;
	}

	public static class GETCOutput {

		public GETextureFormat format;

		public IGETextureIntermediate intermediate;
	}

	public static class IntermediateIndexed implements IGETextureIntermediate {

		public byte[] texData;
		public short[] palette;
		public boolean isPalCol0Transparent;

		private IntermediateIndexed(byte[] texData, short[] palette, boolean isPalCol0Transparent) {
			this.texData = texData;
			this.palette = palette;
			this.isPalCol0Transparent = isPalCol0Transparent;
		}

		@Override
		public byte[] getTexData() {
			return texData;
		}
	}

	public static class IntermediateRaw implements IGETextureIntermediate {

		public byte[] texData;

		private IntermediateRaw(byte[] data) {
			this.texData = data;
		}
		
		@Override
		public byte[] getTexData() {
			return texData;
		}
	}

	public static class GETCInput {

		public String texName;
		public String palName;

		public int width;
		public int height;

		public GETextureFormat outFormat;

		public short[] colors;
		public short[] alphaChannel;

		public GETCInput() {

		}

		public GETCInput(Texture tex, GETextureFormat format) {
			texName = tex.name;
			palName = getPalName(tex);
			width = tex.width;
			height = tex.height;
			outFormat = format;

			byte[] rgba = tex.format.getRGBA(tex);
			colors = createRGB5A1(rgba);
			alphaChannel = TextureProcessor.getAlphaChannel(rgba);
		}
	}
}
