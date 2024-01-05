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
	
	private final boolean isHackedFormat; //flag for detecting FF3 windows port hacked NSBTXs

	public NSBTXDecoder(NTRDataIOStream data, int texBlockSize, NSBTXHeader textureInfo) throws IOException {
		if (textureInfo != null) {
			texOffset = textureInfo.texHeader.imageOffset;
			cmprTexOffset = textureInfo.compressedTexHeader.imageOffset;
			cmprIndexOffset = textureInfo.compressedTexHeader.idxDataOffset;
			int maxTexOffset = texBlockSize;
			if (cmprTexOffset > texOffset) {
				maxTexOffset = Math.min(maxTexOffset, cmprTexOffset);
			}
			if (textureInfo.paletteHeader.imageOffset > texOffset) {
				maxTexOffset = Math.min(maxTexOffset, textureInfo.paletteHeader.imageOffset);
			}
			isHackedFormat = maxTexOffset > (0xFFFF << 3);
		} else {
			cmprTexOffset = -1;
			texOffset = -1;
			cmprIndexOffset = -1;
			isHackedFormat = false;
		}
	}
	
	public NSBTXTexture readTexture(DataIOStream in, String name, int texImageParam) throws IOException {
		int format = (texImageParam >> 26) & 7;
		int height = (texImageParam >> 23) & 7;
		int width = (texImageParam >> 20) & 7;
		int offset = (texImageParam & 0xFFFF) << 3;
		int offsetFF3 = (texImageParam & 0xFFFFF) << 3;
		
		if (isHackedFormat) {
			//FF3 can have the texture image size larger than the DS hardware maximum, in which case it is
			//set to 0
			
			//workaround: FF3 steam release hacked the format to allow for
			//"hd" textures by using the (normally masked out by materials) texture wrap bits
			//for an offset. This wouldn't work very well on the DS hardware, but it does in the
			//windows port.
			offset = offsetFF3;
		}

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
