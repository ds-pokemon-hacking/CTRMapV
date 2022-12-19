
package ctrmap.formats.ntr.nitrowriter.nsbtx;

import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;

public class TextureResource extends NNSG3DResource {

	public int width;
	public int height;
	
	public GETextureFormat format;
	
	private final byte[] data;
	
	public TextureResource(String name, int w, int h, GETextureFormat format, byte[] data){
		this.name = name;
		this.width = w;
		this.height = h;
		this.format = format;
		this.data = data;
	}
	
	@Override
	public byte[] getBytes() {
		return data;
	}
}
