package ctrmap.formats.shrek3.g3d;

import ctrmap.formats.ntr.common.NDSDeserializer;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatTexImageParamSet;
import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.common.gfx.texture.GETextureIndexed;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scenegraph.G3DResource;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.BinarySerializer;
import xstandard.io.serialization.ICustomSerialization;
import xstandard.io.serialization.annotations.Define;
import xstandard.io.serialization.annotations.DefinedArraySize;
import xstandard.io.serialization.annotations.Ignore;
import xstandard.io.serialization.annotations.Inline;
import xstandard.io.serialization.annotations.LengthPos;
import xstandard.io.serialization.annotations.Size;

public class S3DTextureInfo implements ICustomSerialization {
	
	public static ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("S3D Texture info", "*.textureinfo.bin");

	@Size(Short.BYTES)
	@Define("textureCount")
	public int textureCount;
	@Size(Short.BYTES)
	@Define("paletteCount")
	public int paletteCount;
	public int paletteBlockSize;
	@Inline
	@DefinedArraySize("textureCount")
	public S3DTexture[] textures;
	@Inline
	@DefinedArraySize("paletteCount")
	public S3DPaletteInfo[] paletteInfo;
	
	public static void main(String[] args) {
		S3DTextureInfo ti = new NDSDeserializer().deserializeFile(S3DTextureInfo.class, new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data_uncomp\\gob\\0ae43448.textureinfo.bin"));
		YamlReflectUtil.serializeObjectAsYml(ti).writeToFile(new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data_uncomp\\gob\\0ae43448.textureinfo.yml"));
	}
	
	public static S3DTextureInfo fromFile(FSFile fsf) {
		return new NDSDeserializer().deserializeFile(S3DTextureInfo.class, fsf);
	}
	
	public G3DResource toGeneric(FSFile texRoot) {
		G3DResource out = new G3DResource();
		for (int texIndex = 0; texIndex < textureCount; texIndex++) {
			S3DTextureInfo.S3DTexture tex = textures[texIndex];
			FSFile texFile = texRoot.getChild(tex.getFileName());
			MatTexImageParamSet teximg = new MatTexImageParamSet(tex.texImageBits);			
			DataInStream in = texFile.getDataInputStream();
			
			try {
				GETextureIndexed convTex = new GETextureIndexed(in, teximg.format, teximg.width, teximg.height, teximg.palCol0IsTransparent);
				Texture outTex = convTex.decode(paletteInfo[texIndex]._palette.colors);
				outTex.name = tex.getName();
				out.addTexture(outTex);
			} catch (IOException ex) {
				Logger.getLogger(S3DTextureInfo.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return out;
	}

	private int getPaletteBlockStart() {
		int sizeof_S3DTexture = 0x1C;
		int sizeof_S3DPaletteInfo = 0x10;

		return 8 + textureCount * sizeof_S3DTexture + paletteCount * sizeof_S3DPaletteInfo;
	}

	@Override
	public void deserialize(BinaryDeserializer deserializer) throws IOException {
		for (S3DPaletteInfo pi : paletteInfo) {
			deserializer.baseStream.seek(getPaletteBlockStart() + (pi.paletteOffset << 1));
			pi._palette = deserializer.deserialize(S3DPalette.class);
		}
	}

	@Override
	public boolean preSerialize(BinarySerializer serializer) throws IOException {
		int paletteOffset = 0;

		for (S3DPaletteInfo pi : paletteInfo) {
			pi.paletteOffset = paletteOffset;
			paletteOffset += (4 + pi._palette.colors.length * Short.BYTES) >> 1;
		}

		paletteBlockSize = paletteOffset;
		return false;
	}

	@Override
	public void postSerialize(BinarySerializer serializer) throws IOException {
		for (S3DPaletteInfo pi : paletteInfo) {
			serializer.serialize(pi._palette);
		}
	}

	public static class S3DTexture {

		public int hash;
		public int dataSize;
		public int texImageBits;
		public int _rtRawData;
		public int _rtVRAMAddress;
		public int _rtRefCount;
		public int _rtPalette;
		
		public String getName() {
			return String.format("%08x", hash);
		}
		
		public String getFileName() {
			return getName() + ".texture.bin";
		}
	}

	public static class S3DPaletteInfo {

		@Size(Integer.BYTES)
		public GETextureFormat format;
		public int paletteOffset;
		public int _rtVRAMAddress;
		public int _rtPalette;
		@Ignore
		public S3DPalette _palette;
	}

	@Inline
	public static class S3DPalette {

		@Inline
		@LengthPos(LengthPos.LengthPosType.BEFORE_PTR)
		public short[] colors;
	}
}
