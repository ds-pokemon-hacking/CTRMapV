package ctrmap.formats.ntr.nitroreader.nsbtx;

import ctrmap.formats.ntr.common.gfx.texture.GETexture;
import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.renderer.scene.texturing.Texture;

public class NSBTXTexture {
	public final String name;
	public final GETexture tex;
	
	public NSBTXTexture(String name, GETexture tex) {
		this.name = name;
		this.tex = tex;
	}
	
	public GETextureFormat getFormat() {
		return tex.format;
	}
	
	public Texture decode(short[] palette) {
		Texture t = tex.decode(palette);
		t.name = name;
		return t;
	}
}
