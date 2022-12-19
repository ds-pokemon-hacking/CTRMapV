package ctrmap.formats.ntr.nitrowriter.nsbtx;

import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.math.MathEx;
import java.io.IOException;

public class PaletteResource extends NNSG3DResource {

	public static final boolean PALETTERESOURCE_COMPACT = false;

	public GETextureFormat format;

	public boolean color0IsTransparent = false;

	public short[] colors;

	public PaletteResource(String name, GETextureFormat texFmt, boolean isColor0Transparent, short[] colors) {
		this.format = texFmt;
		this.color0IsTransparent = isColor0Transparent;
		this.colors = colors;
		this.name = name;
	}

	private PaletteResource() {

	}

	public static PaletteResource createEmptyPalette(String texName, GETextureFormat format) {
		PaletteResource pal = new PaletteResource();
		pal.name = texName;
		pal.format = format;
		pal.color0IsTransparent = false;
		pal.colors = new short[4];
		return pal;
	}

	@Override
	public byte[] getBytes() throws IOException {
		DataIOStream out = new DataIOStream();

		int count2 = getColorCountByFormat(format);
		int count1 = Math.min(count2, colors.length);

		for (int i = 0; i < count1; i++) {
			out.writeShort(colors[i] & 0x7FFF);
		}

		count2 = MathEx.padInteger(count2, 8);
		for (int i = count1; i < count2; i++) {
			GXColor.WHITE.write(out);
		}

		out.close();
		return out.toByteArray();
	}

	public static int getColorCountByFormat(GETextureFormat format) {
		switch (format) {
			case IDX2:
				return 4;
			case IDX4:
				return 16;
			case IDX8:
				return 256;
			case A3I5:
				return 32;
			case A5I3:
				return 8;
		}
		return 0;
	}
}
