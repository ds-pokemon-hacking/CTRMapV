
package ctrmap.formats.ntr.nitrowriter.nsbtx;

import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;

public class CompressedTextureResource extends TextureResource {

	public GETextureCompressor.IndexData[] idxData;
	public GETextureCompressor.PaletteEntry[] palette;
	
	public CompressedTextureResource(String name, int w, int h, GETextureFormat format, byte[] data, GETextureCompressor.IndexData[] idxData, GETextureCompressor.PaletteEntry[] palette) {
		super(name, w, h, format, data);
		
		if (format != GETextureFormat.IDXCMPR) {
			throw new RuntimeException("Compressed texture bad format!");
		}
		
		this.idxData = idxData;
		this.palette = palette;
	}
	
	public int paletteEntryIndex(GETextureCompressor.PaletteEntry e) {
		for (int i = 0; i < palette.length; i++) {
			if (palette[i] == e) {
				return i;
			}
		}
		return -1;
	}
}
