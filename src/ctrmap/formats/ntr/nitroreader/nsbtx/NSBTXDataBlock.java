package ctrmap.formats.ntr.nitroreader.nsbtx;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDMaterial;
import java.util.ArrayList;
import java.util.List;
import ctrmap.formats.ntr.nitroreader.nsbtx.info.NSBTXHeader;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scenegraph.G3DResource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSBTXDataBlock {

	public List<NSBTXPalette> palettes;
	public List<NSBTXTexture> textures;

	private short[] compPalette;

	public NSBTXDataBlock(byte[] data) {
		this(new NTRDataIOStream(data));
	}

	public NSBTXDataBlock(NTRDataIOStream in) {
		try {
			in.setBaseHere();
			String magic = in.readPaddedString(4);
			int size = in.readInt();

			NSBTXHeader info = new NSBTXHeader(in);
			in.seek(info.texHeader.entriesOffset);
			NNSPatriciaTreeReader.Entry[] texHeaders = NNSPatriciaTreeReader.readTree(in);
			in.seek(info.paletteHeader.entriesOffset);
			NNSPatriciaTreeReader.Entry[] paletteHeaders = NNSPatriciaTreeReader.readTree(in);

			textures = new ArrayList(texHeaders.length);
			palettes = new ArrayList(paletteHeaders.length);

			in.seek(info.paletteHeader.imageOffset);
			compPalette = new short[info.paletteHeader.imageSize >> 1];
			for (int i = 0; i < compPalette.length; i++) {
				compPalette[i] = in.readShort(); //the compressed palette often overflows, needs to be read as a whole
			}

			NSBTXDecoder decoder = new NSBTXDecoder(in, size, info);
			for (NNSPatriciaTreeReader.Entry texHeader : texHeaders) {
				textures.add(decoder.readTexture(in, texHeader.getName(), texHeader.getParam(0)));
			}

			in.seek(info.paletteHeader.imageOffset);
			in.setBaseHere();
			for (NNSPatriciaTreeReader.Entry e : paletteHeaders) {
				int offset = (e.getParam(0) & 0xFFFF) << 3;

				int nextOffset = info.paletteHeader.imageSize;
				for (NNSPatriciaTreeReader.Entry e2 : paletteHeaders) {
					if (e2 != e) {
						int otherOffset = (e2.getParam(0) & 0xFFFF) << 3;
						if (otherOffset < nextOffset && otherOffset > offset) {
							nextOffset = otherOffset;
						}
					}
				}
				in.seek(offset);
				NSBTXPalette pal = new NSBTXPalette(in, e.getName(), nextOffset - offset);
				palettes.add(pal);
			}
			in.resetBase();

			in.resetBase();
		} catch (IOException ex) {
			Logger.getLogger(NSBTXDataBlock.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public G3DResource toGeneric(List<Material> genericMaterials) {
		G3DResource res = new G3DResource();
		Map<String, Texture> outTextures = new HashMap<>();
		for (Material mat : genericMaterials) {
			if (!mat.textures.isEmpty()) {
				String pltMetaName = String.format(NSBMDMaterial.NNS_METADATA_PALETTE_NAME_FORMAT, mat.textures.get(0).textureName);
				if (mat.metaData.hasValue(pltMetaName)) {
					Texture tex = getConvTexture(mat.textures.get(0).textureName, mat.metaData.getValue(pltMetaName).stringValue());
					if (tex != null) {
						outTextures.put(tex.name, tex);
					}
				}
			}
		}
		
		//Preserve order in NSBTX as well as the output resource
		for (int i = 0; i < textures.size(); i++) {
			Texture tex = outTextures.get(textures.get(i).name);
			if (tex != null) {
				res.addTexture(tex);
			}
		}

		toGeneric(res);

		return res;
	}

	public G3DResource toGeneric() {
		return toGeneric(new G3DResource());
	}

	public G3DResource toGeneric(G3DResource dest) {
		for (NSBTXTexture tex : textures) {
			if (Scene.getNamedObject(tex.name, dest.textures) == null) {
				if (tex.getFormat() == GETextureFormat.RGB5A1) {
					dest.addTexture(tex.decode(null));
				} else {
					for (NSBTXPalette pal : palettes) {
						if (pal.name.equals(tex.name) || pal.name.equals(tex.name + "_pl")) {
							dest.addTexture(decodeTexture(tex, pal));
							break;
						}
					}
				}
			}
		}

		return dest;
	}

	public NSBTXTexture getTexture(String texName) {
		NSBTXTexture tex = null;
		for (NSBTXTexture texture : textures) {
			if (texName.equals(texture.name)) {
				tex = texture;
				break;
			}
		}
		return tex;
	}

	public NSBTXPalette getPalette(String palName) {
		for (NSBTXPalette palette : palettes) {
			if (palName.equals(palette.name)) {
				return palette;
			}
		}
		return null;
	}

	public Texture getConvTexture(String texName, String palName) {
		NSBTXTexture tex = null;
		NSBTXPalette pal = null;
		if (texName != null) {
			tex = getTexture(texName);
		}
		if (palName != null) {
			pal = getPalette(palName);
		}
		if (tex != null && (pal != null || tex.getFormat() == GETextureFormat.RGB5A1)) {
			Texture result = decodeTexture(tex, pal);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private Texture decodeTexture(NSBTXTexture tex, NSBTXPalette palette) {
		short[] paletteColors = null;
		if (tex.getFormat() == GETextureFormat.IDXCMPR) {
			paletteColors = Arrays.copyOfRange(compPalette, palette == null ? 0 : (palette.originalOffset >> 1), compPalette.length);
		}
		else if (palette != null) {
			paletteColors = palette.colors;
		}
		Texture out = tex.decode(paletteColors);
		if (palette != null) {
			out.metaData.putValue(ReservedMetaData.IDX_FORMAT_PALETTE_NAME, palette.name);
		}
		return out;
	}
}
