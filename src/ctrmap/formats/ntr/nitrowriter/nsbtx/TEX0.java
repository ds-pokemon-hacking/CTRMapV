package ctrmap.formats.ntr.nitrowriter.nsbtx;

import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatTexImageParamSet;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DDataBlock;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResourceTree;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSPatriciaTreeWriter;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.texturing.Texture;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.structs.TemporaryOffsetShort;
import xstandard.io.structs.TemporaryValue;
import xstandard.io.structs.TemporaryValueShort;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TEX0 implements NNSG3DDataBlock {

	private Map<String, String> newTextureNames = new HashMap<>();
	private Map<String, String> newPaletteNames = new HashMap<>();

	private List<TextureResource> allTextures = new ArrayList<>();
	
	private List<TextureResource> textures = new ArrayList<>();
	private List<CompressedTextureResource> compressedTextures = new ArrayList<>();
	private List<PaletteResource> palettes = new ArrayList<>();
	private Map<TextureResource, PaletteResource> texPaletteMap = new HashMap<>();

	public TEX0(List<Texture> textures, NNSWriterLogger log) {
		if (log == null) {
			log = new NNSWriterLogger.DummyLogger();
		}
		int totalTextureImageSize = 0;
		int totalCmprTextureImageSize = 0;

		for (Texture tex : textures) {
			int hobW = Integer.highestOneBit(tex.width);
			int hobH = Integer.highestOneBit(tex.height);
			if (hobW != tex.width || hobH != tex.height || tex.width > 1024 || tex.height > 1024 || tex.width < 8 || tex.height < 8) {
				log.err("WARNING: Invalid texture size - " + tex.width + "x" + tex.height + ". Skipping. (" + tex.name + ")");
				continue;
			}

			GETextureConverter.GETCOutput convertedData = GETextureConverter.convertTextureGenericFormat(tex, log);

			if (convertedData.intermediate != null) {
				boolean isCmpTex = convertedData.format == GETextureFormat.IDXCMPR;

				int sizeAddend = convertedData.intermediate.getTexData().length;

				if (isCmpTex) {
					totalCmprTextureImageSize += sizeAddend;
				} else {
					totalTextureImageSize += sizeAddend;
				}

				if ((isCmpTex ? totalCmprTextureImageSize : totalTextureImageSize) > OFF16_3_MAX) {
					log.err("TEXTURE CAN NOT FIT IN DATA IMAGE! Omitting... (" + tex.name + ")");
				} else {
					if (isCmpTex) {
						GETextureCompressor.TexCmprOutput compOut = (GETextureCompressor.TexCmprOutput) convertedData.intermediate;
						CompressedTextureResource texRes = new CompressedTextureResource(
							tex.name,
							tex.width,
							tex.height,
							convertedData.format,
							compOut.texData,
							compOut.indexData,
							compOut.cmprPalette
						);
						PaletteResource dmyPal = PaletteResource.createEmptyPalette(ReservedMetaData.getIdxTexPalName(tex), convertedData.format);
						texPaletteMap.put(texRes, dmyPal);
						compressedTextures.add(texRes);
						palettes.add(dmyPal);
						allTextures.add(texRes);
					} else if (convertedData.format == GETextureFormat.RGB5A1) {
						GETextureConverter.IntermediateRaw raw = (GETextureConverter.IntermediateRaw) convertedData.intermediate;
						TextureResource texRes = new TextureResource(tex.name, tex.width, tex.height, GETextureFormat.RGB5A1, raw.texData);
						this.textures.add(texRes);
						allTextures.add(texRes);
					} else {
						GETextureConverter.IntermediateIndexed indexed = (GETextureConverter.IntermediateIndexed) convertedData.intermediate;
						TextureResource texRes = new TextureResource(
							tex.name,
							tex.width, 
							tex.height, 
							convertedData.format,
							indexed.texData
						);
						PaletteResource palRes = new PaletteResource(ReservedMetaData.getIdxTexPalName(tex), convertedData.format, indexed.isPalCol0Transparent, indexed.palette);
						texPaletteMap.put(texRes, palRes);
						this.textures.add(texRes);
						this.palettes.add(palRes);
						allTextures.add(texRes);
					}
				}
			}
		}
		
		for (int i = 0; i < this.palettes.size(); i++) {
			PaletteResource p = this.palettes.get(i);
			if (p.format == GETextureFormat.IDX2) {
				//sort palettes so that 2bpp indexed go first since these have a lower palette base step
				this.palettes.add(0, p);
				this.palettes.remove(p);
			}
		}

		NNSG3DResourceTree.renameDuplicates(this.textures, newTextureNames);
		NNSG3DResourceTree.renameDuplicates(this.compressedTextures, newTextureNames);
		NNSG3DResourceTree.renameDuplicates(palettes, newPaletteNames);
	}

	public String getRenamedTexName(String origName) {
		return newTextureNames.get(origName);
	}

	public String getRenamedPaletteName(String origPalName) {
		return newPaletteNames.get(origPalName);
	}

	private static final int[] CEILDIV_TABLE_01234 = new int[]{0, 1, 1, 2, 2};

	@Override
	public byte[] getData() throws IOException {
		DataIOStream out = new DataIOStream();
		out.writeStringUnterminated("TEX0");
		TemporaryValue size = new TemporaryValue(out);

		//Texture header
		out.writeInt(0); //constant ??
		TemporaryValueShort textureImageSize = new TemporaryValueShort(out);
		TemporaryOffsetShort textureDictOffset = new TemporaryOffsetShort(out);
		out.writeInt(0);
		TemporaryOffset textureImageOffset = new TemporaryOffset(out);

		//Compressed texture header
		out.writeInt(0);
		TemporaryValueShort cmprTextureImageSize = new TemporaryValueShort(out);
		TemporaryOffsetShort cmprTextureDictOffset = new TemporaryOffsetShort(out);
		out.writeInt(0);
		//equal to the normal texture dict offset
		TemporaryOffset cmprTextureImageOffset = new TemporaryOffset(out);
		TemporaryOffset cmprTextureIndexOffset = new TemporaryOffset(out);

		//Palette header
		out.writeInt(0);
		TemporaryValueShort paletteImageSize = new TemporaryValueShort(out);
		out.writeShort(0);
		TemporaryOffsetShort paletteDictOffset = new TemporaryOffsetShort(out);
		out.writeShort(0);
		TemporaryOffset paletteImageOffset = new TemporaryOffset(out);

		//Texture dict
		TemporaryValueShort[] textureOffsets = new TemporaryValueShort[textures.size()];
		TemporaryValueShort[] textureCmprOffsets = new TemporaryValueShort[compressedTextures.size()];

		//overlay 36 : 0x21BCD48 in Pokémon White 2 USA
		//the texture dict MUST be right after the headers, as the game code ignores the offset and reads straight from here
		textureDictOffset.setHere();
		cmprTextureDictOffset.setHere();

		/*List<TextureResource> allTextures = new ArrayList<>();
		allTextures.addAll(textures);
		allTextures.addAll(compressedTextures);*/

		NNSPatriciaTreeWriter.writeNNSPATRICIATree(out, allTextures, 8);

		for (int i = 0, cmpIdx = 0, nonCmpIdx = 0; i < allTextures.size(); i++) {
			TextureResource tex = allTextures.get(i);
			PaletteResource pal = texPaletteMap.get(tex);

			//TEXIMAGE_PARAM
			MatTexImageParamSet texImageParam = new MatTexImageParamSet();
			texImageParam.format = tex.format;
			texImageParam.width = tex.width;
			texImageParam.height = tex.height;
			texImageParam.palCol0IsTransparent = pal != null && pal.color0IsTransparent;
			texImageParam.writeParams(out);
			if (tex.format == GETextureFormat.IDXCMPR) {
				textureCmprOffsets[cmpIdx] = texImageParam.getTexVRAMOffsTempOffs();
				cmpIdx++;
			} else {
				textureOffsets[nonCmpIdx] = texImageParam.getTexVRAMOffsTempOffs();
				nonCmpIdx++;
			}

			//parameter specifying the image w/h again ????
			//Last bit seems to be constant
			out.writeInt(tex.width | (tex.height << 11) | (1 << 31));
		}
		NNSPatriciaTreeWriter.writeNNSPATRICIATreeNames(out, allTextures);

		//Palette dict
		TemporaryValueShort[] paletteOffsets = new TemporaryValueShort[palettes.size()];
		List<TemporaryValueShort> paletteCmprOffsets = new ArrayList<>();

		paletteDictOffset.setHere();

		NNSPatriciaTreeWriter.writeNNSPATRICIATree(out, palettes, 4);
		for (int i = 0; i < palettes.size(); i++) {
			if (palettes.get(i).format == GETextureFormat.IDXCMPR) {
				paletteCmprOffsets.add(new TemporaryValueShort(out));
			} else {
				paletteOffsets[i] = new TemporaryValueShort(out);
			}
			out.writeShort(palettes.get(i).format == GETextureFormat.IDX2 ? 1 : 0); //all textures with format 2 have this bit set
		}
		NNSPatriciaTreeWriter.writeNNSPATRICIATreeNames(out, palettes);

		out.pad(0x10);

		//Texture image
		writeImageData(out, textures, textureImageOffset, textureImageSize, textureOffsets);

		//Compressed texture image - setup
		cmprTextureImageOffset.setHere();
		int cmprTexStart = out.getPosition();

		out.pad(16);

		//Write compressed texture image
		for (int i = 0; i < compressedTextures.size(); i++) {
			CompressedTextureResource ct = compressedTextures.get(i);

			textureCmprOffsets[i].set((out.getPosition() - cmprTexStart) >> 3);
			out.write(ct.getBytes());
			out.pad(16);
		}

		cmprTextureImageSize.set((out.getPosition() - cmprTexStart) >> 3);

		boolean alreadyWarnedOverIdx = false;

		//Write compressed texture index data
		cmprTextureIndexOffset.setHere();
		for (int i = 0; i < compressedTextures.size(); i++) {
			CompressedTextureResource ct = compressedTextures.get(i);

			int[] paletteIndexTable = new int[ct.palette.length];
			int paletteIndex = 0;
			for (int j = 0; j < paletteIndexTable.length; j++) {
				GETextureCompressor.PaletteEntry e = ct.palette[j];
				paletteIndexTable[j] = paletteIndex;
				paletteIndex += CEILDIV_TABLE_01234[e.getExistingColorLength()];
			}

			for (GETextureCompressor.IndexData id : ct.idxData) {
				int idx = paletteIndexTable[ct.paletteEntryIndex(id.palEntry)];
				if (idx < 0 || idx > 16383) {
					if (!alreadyWarnedOverIdx) {
						System.err.println("Compressed palette index " + idx + " out of range! Output will be broken.");
						alreadyWarnedOverIdx = true;
					}
				}
				out.writeShort(idx | (id.mode.ordinal() << 14));
			}
			out.pad(16);
		}

		//Palette image
		int palImgStart = out.getPosition();
		writeImageData(out, palettes, paletteImageOffset, paletteImageSize, paletteOffsets);

		int cmprPalIdx = 0;

		//Compressed palette
		for (CompressedTextureResource cmprTex : compressedTextures) {
			out.pad(8);
			paletteCmprOffsets.get(cmprPalIdx++).set((out.getPosition() - palImgStart) >> 3);
			for (GETextureCompressor.PaletteEntry pe : cmprTex.palette) {
				int existColorNo = pe.getExistingColorLength();
				for (int i = 0; i < existColorNo; i++) {
					out.writeShort(pe.colors[i]);
				}
				if ((existColorNo & 1) == 1) {
					out.writeShort(0);
				}
			}
		}

		out.pad(16);
		paletteImageSize.set((out.getPosition() - palImgStart) >> 3);

		size.set(out.getLength());
		out.close();
		return out.toByteArray();
	}

	private static final int OFF16_3_MAX = 0xFFFF << 3;

	private static void writeImageData(
		DataIOStream out,
		List<? extends NNSG3DResource> resources,
		TemporaryOffset offsetTo,
		TemporaryValue size,
		TemporaryValueShort[] elemOffsets
	) throws IOException {
		int start = out.getPosition();
		offsetTo.setHere();

		for (int i = 0; i < elemOffsets.length; i++) {
			if (elemOffsets[i] != null) {
				out.pad(16); //the padding is actually to 16, otherwise weird stuff happens. Wonder why nintendo didn't use one more shift.
				int off = (out.getPosition() - start);
				if (off > OFF16_3_MAX) {
					System.err.println("MAXIMUM DATA IMAGE CAPACITY EXCEEDED ! OMITTING ALL SUBSEQUENT RESOURCES.");
					for (int res = i; res < resources.size(); res++) {
						System.err.println(resources.get(res));
					}
					break;
				}
				elemOffsets[i].set(off >> 3);
				out.write(resources.get(i).getBytes());
			}
		}
		out.pad(16);
		size.set((out.getPosition() - start) >> 3);
	}
}
