package ctrmap.formats.ntr.nitroreader.nsbtx;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.common.gfx.texture.GETexture;
import ctrmap.formats.ntr.common.gfx.texture.GETextureCompressed;
import ctrmap.formats.ntr.common.gfx.texture.GETextureDirect;
import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.common.gfx.texture.GETextureIndexed;
import ctrmap.formats.ntr.nitroreader.nsbtx.info.NSBTXHeader;
import java.io.IOException;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.math.BitMath;

public class NSBTXDecoder {

	private final int cmprTexOffset;
	private final int texOffset;
	private final int cmprIndexOffset;

	public NSBTXDecoder(NTRDataIOStream data, NSBTXHeader textureInfo) throws IOException {
		if (textureInfo != null) {
			texOffset = textureInfo.texHeader.imageOffset;
			cmprTexOffset = textureInfo.compressedTexHeader.imageOffset;
			cmprIndexOffset = textureInfo.compressedTexHeader.idxDataOffset;
		} else {
			cmprTexOffset = -1;
			texOffset = -1;
			cmprIndexOffset = -1;
		}
	}
	
	public NSBTXTexture readTexture(DataIOStream in, String name, int texImageParam) throws IOException {
		int format = (texImageParam >> 26) & 7;
		int height = (texImageParam >> 23) & 7;
		int width = (texImageParam >> 20) & 7;
		int offset = (texImageParam & 0xFFFF) << 3;

		//System.out.println("tex " + name + " fmt " + format);
		boolean hasAlpha = BitMath.checkIntegerBit(texImageParam, 29);
		width = 8 << width;
		height = 8 << height;
		GETextureFormat geFormat = GETextureFormat.values()[format];
		if (geFormat == GETextureFormat.IDXCMPR) {
			in.seek(cmprTexOffset + offset);
		} else {
			in.seek(texOffset + offset);
		}
		GETexture geTex = null;
		switch (geFormat) {
			case IDXCMPR:
				in.checkpoint();
				in.seek(cmprIndexOffset + (offset >> 1));
				byte[] indexData = in.readBytes((width * height * 2) >> 4);
				in.resetCheckpoint();
				geTex = new GETextureCompressed(in, width, height, indexData);
				break;
			case A3I5:
			case A5I3:
			case IDX2:
			case IDX4:
			case IDX8:
				geTex = new GETextureIndexed(in, geFormat, width, height, hasAlpha);
				break;
			case RGB5A1:
				geTex = new GETextureDirect(in, width, height);
				break;
		}
		NSBTXTexture tex = new NSBTXTexture(name, geTex);
		return tex;
	}
}
