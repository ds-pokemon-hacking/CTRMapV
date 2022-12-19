package ctrmap.formats.shrek3.g3d;

import ctrmap.formats.ntr.common.NDSDeserializer;
import ctrmap.formats.ntr.common.gfx.commands.GEDisplayList;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.ModelProcessor;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.BinarySerializer;
import xstandard.io.serialization.ICustomSerialization;
import xstandard.io.serialization.annotations.Define;
import xstandard.io.serialization.annotations.DefinedArraySize;
import xstandard.io.serialization.annotations.Ignore;
import xstandard.io.serialization.annotations.Inline;
import xstandard.io.serialization.annotations.LengthPos;
import xstandard.io.serialization.annotations.PointerBase;
import xstandard.io.serialization.annotations.PointerValue;

public class S3DGeometry implements ICustomSerialization {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("S3D Geometry", "*.geometry.bin");

	public int magic;
	@Inline
	public S3DMatrix4x3 boundingTransform;
	public int field_34;
	@LengthPos(LengthPos.LengthPosType.BEFORE_PTR)
	public S3DBone[] bones;
	@LengthPos(LengthPos.LengthPosType.BEFORE_PTR)
	public S3DTextureRef[] textureRefs;
	public S3DPolygon polygon;

	@Inline
	public S3DDisplayList _dl;

	@Ignore
	private byte[] commandBuffer;
	@Ignore
	private GEDisplayList decodedDL;

	public static S3DGeometry fromFile(FSFile fsf) {
		return new NDSDeserializer().deserializeFile(S3DGeometry.class, fsf);
	}

	public G3DResource toGeneric(FSFile originalFile) {
		FSFile parentDir = originalFile.getParent();

		String baseName = originalFile.getName();
		baseName = baseName.substring(0, baseName.indexOf('.'));
		S3DTextureInfo texInfo = new NDSDeserializer().deserializeFile(S3DTextureInfo.class, parentDir.getChild(baseName + ".textureinfo.bin"));

		G3DResource res = new G3DResource(toGeneric(originalFile.getNameWithoutExtension(), texInfo));
		res.merge(texInfo.toGeneric(parentDir));

		return res;
	}

	private void decodeDL() {
		try {
			DataIOStream io = new DataIOStream(commandBuffer);
			decodedDL = new GEDisplayList(io, commandBuffer.length);
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(S3DGeometry.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public Model toGeneric(String name, S3DTextureInfo texInfo) {
		DataIOStream dl = new DataIOStream(commandBuffer);

		Model mdl = new Model();
		mdl.name = name;

		S3DGeometryConverter conv = new S3DGeometryConverter();
		int boneIndex = 0;
		for (S3DBone b : bones) {
			Joint j = new Joint();
			j.name = "Joint" + boneIndex;
			for (Fixup f : b.fixups) {
				conv.registerBone(f.pointer, j);
			}
			mdl.skeleton.addJoint(j);
			boneIndex++;
		}
		int texVRAMOffset = 0;
		for (S3DTextureRef texRef : textureRefs) {
			S3DTextureInfo.S3DTexture tex = texInfo.textures[texRef.textureIndex];
			conv.registerTexture(texVRAMOffset, tex.getName());
			for (Fixup f : texRef.fixups) {
				try {
					dl.seek(f.pointer);
					dl.writeInt(tex.texImageBits | texVRAMOffset);
				} catch (IOException ex) {
					Logger.getLogger(S3DGeometry.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			texVRAMOffset++;
		}

		decodeDL();
		conv.convertDisplayList(decodedDL);

		conv.getResult(mdl);
		ModelProcessor.upZtoY(mdl, false);
		ModelProcessor.smoothSkinningToRigid(mdl, false);
		ModelProcessor.mergeMeshesByMaterials(mdl);

		return mdl;
	}

	public static void main(String[] args) {
		/*for (FSFile f : new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data_uncomp\\gob").listFiles()) {
			if (f.getName().endsWith(".geometry.bin")) {
				S3DGeometry geo = S3DGeometry.fromFile(f);
				if (geo.bones.length > 20) {
					System.out.println(f);
				}
			}
		}*/
		FSFile geoFile = new DiskFile(
			"D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data_uncomp\\gob\\d29c06e6.d29c06e6.geometry.bin"
		);
		S3DGeometry geo = new NDSDeserializer().deserializeFile(S3DGeometry.class, geoFile);
		geo.decodeDL();
		geo.toGeneric(geoFile);
		YamlReflectUtil.serializeObjectAsYml(geo).writeToFile(new DiskFile(
			"D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data_uncomp\\gob\\d29c06e6.d29c06e6.geometry.yml"
		));
	}

	private void makeFixupsRelative(Fixup[] fixups) {
		for (Fixup f : fixups) {
			f.pointer -= _dl.dlOffset;
		}
	}

	@Override
	public void deserialize(BinaryDeserializer deserializer) throws IOException {
		for (S3DBone b : bones) {
			makeFixupsRelative(b.fixups);
		}
		for (S3DTextureRef tr : textureRefs) {
			makeFixupsRelative(tr.fixups);
		}
		makeFixupsRelative(polygon.fixups);
		deserializer.baseStream.seek(_dl.dlOffset);
		commandBuffer = deserializer.baseStream.readBytes(_dl.dlSize);
	}

	@Override
	public boolean preSerialize(BinarySerializer serializer) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void postSerialize(BinarySerializer serializer) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@PointerBase
	public static class S3DBone {

		@Define("fixupCount")
		public short fixupCount;
		public short flags;
		@Inline
		@DefinedArraySize("fixupCount")
		public Fixup[] fixups;
	}

	@PointerBase
	public static class S3DTextureRef {

		public int _rtOldRefValue;
		public int textureIndex;
		@Inline
		@LengthPos(LengthPos.LengthPosType.BEFORE_PTR)
		public Fixup[] fixups;
	}

	@PointerBase
	public static class S3DPolygon {

		@Inline
		@LengthPos(LengthPos.LengthPosType.BEFORE_PTR)
		public Fixup[] fixups;
	}

	@PointerBase
	public static class S3DDisplayList {

		@PointerValue
		public int dlOffset;
		public int dlSize;
	}

	@Inline
	public static class Fixup {

		@PointerValue
		public int pointer;
	}
}
