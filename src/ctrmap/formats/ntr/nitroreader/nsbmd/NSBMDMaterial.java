package ctrmap.formats.ntr.nitroreader.nsbmd;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.common.gfx.Nitroshader;
import ctrmap.formats.ntr.nitroreader.common.Utils;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.util.MaterialProcessor;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import java.io.IOException;
import java.util.Arrays;
import xstandard.math.BitMath;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;
import xstandard.util.EnumBitflagsInt;

public class NSBMDMaterial {

	public static final String NNS_METADATA_PALETTE_NAME_FORMAT = "%s_Palette";

	public String name;
	public String textureName;
	public String paletteName;

	public int texHeight;
	public int texWidth;

	public boolean repeatU;
	public boolean repeatV;
	public boolean mirrorU;
	public boolean mirrorV;

	public float transU = 0.0f;
	public float transV = 0.0f;
	public float rotation = 0.0f;
	public float scaleU = 1.0f;
	public float scaleV = 1.0f;

	public boolean enableFog;
	public int lights;

	public int alpha;

	public boolean hasDifColor;
	public boolean hasAmbColor;
	public boolean hasSpcColor;
	public boolean hasEmiColor;
	public boolean diffuseAsVCol;
	public boolean shininess;

	public RGBA ambient;
	public RGBA diffuse;
	public RGBA emission;
	public RGBA specular;

	public int polygonID;
	public int polygonMode;
	public boolean renderBackFace;
	public boolean renderFrontFace;

	private int polyAttr;
	public int texImageParam;
	private EnumBitflagsInt<NSBMDMaterialFlag> flags;

	//These coordinates multiply ALL texture matrix translations/scales. I haven't yet
	//seen them have different values than 1.0 though.
	public float transformPreScaleU;
	public float transformPreScaleV;

	public Matrix4 textureMappingMatrix;

	public NSBMDMaterial(NTRDataIOStream data, String name) throws IOException {
		this.name = name;
		int type = data.readUnsignedShort(); //this tells the game which rendering path to use. However, there is only one.
		int size = data.readUnsignedShort();
		int diffAmb = data.readInt();
		int specEmi = data.readInt();
		polyAttr = data.readInt();
		int polyAttrMask = data.readInt();
		texImageParam = data.readInt();
		long texImageParamMask = data.readInt();
		int _rtPaletteBase = data.readUnsignedShort();
		flags = new EnumBitflagsInt<>(NSBMDMaterialFlag.class, data.readUnsignedShort());
		texWidth = data.readUnsignedShort();
		texHeight = data.readUnsignedShort();
		transformPreScaleU = data.readFX32();
		transformPreScaleV = data.readFX32();

		//Decompose teximage param
		repeatU = BitMath.checkIntegerBit(texImageParam, 16);
		repeatV = BitMath.checkIntegerBit(texImageParam, 17);
		mirrorU = BitMath.checkIntegerBit(texImageParam, 18);
		mirrorV = BitMath.checkIntegerBit(texImageParam, 19);

		//Decompose material colors
		diffuse = RGBA.fromARGB(Utils.GXColorToRGB((short) (diffAmb & 0xFFFF)));
		ambient = RGBA.fromARGB(Utils.GXColorToRGB((short) (diffAmb >> 16)));
		diffuseAsVCol = flags.isSet(NSBMDMaterialFlag.SHADING_VCOL_FROM_AMBIENT);
		specular = RGBA.fromARGB(Utils.GXColorToRGB((short) (specEmi & 0xFFFF)));
		emission = RGBA.fromARGB(Utils.GXColorToRGB((short) (specEmi >> 16)));
		shininess = flags.isSet(NSBMDMaterialFlag.SHADING_SHININESS);

		hasDifColor = flags.isSet(NSBMDMaterialFlag.SHADING_DIFFUSE);
		hasAmbColor = flags.isSet(NSBMDMaterialFlag.SHADING_AMBIENT);
		hasSpcColor = flags.isSet(NSBMDMaterialFlag.SHADING_SPECULAR);
		hasEmiColor = flags.isSet(NSBMDMaterialFlag.SHADING_EMISSION);
		
		//Decompose POLYGON_ATTR
		lights = (int) (polyAttr & 0xf);
		polygonMode = BitMath.getIntegerBits(polyAttr, 4, 2);
		renderBackFace = BitMath.checkIntegerBit(polyAttr, 6);
		renderFrontFace = BitMath.checkIntegerBit(polyAttr, 7);
		boolean xluDepthReplace = BitMath.checkIntegerBit(polyAttr, 11);
		enableFog = BitMath.checkIntegerBit(polyAttr, 15);
		alpha = GXColor.bit5to8(BitMath.getIntegerBits(polyAttr, 16, 5));
		polygonID = BitMath.getIntegerBits(polyAttr, 24, 6);

		if (!flags.isSet(NSBMDMaterialFlag.TEXMTX_IDENTITY_S)) {
			scaleU = data.readFX32();
			scaleV = data.readFX32();
		}
		if (!flags.isSet(NSBMDMaterialFlag.TEXMTX_IDENTITY_R)) {
			rotation = (float) Math.toDegrees(Math.atan2(data.readFX32(), data.readFX32()));
		}
		if (!flags.isSet(NSBMDMaterialFlag.TEXMTX_IDENTITY_T)) {
			transU = data.readFX32();
			transV = data.readFX32();
		}
		if (flags.isSet(NSBMDMaterialFlag.TEXMAP_MATRIX_EXISTS)) {
			/*
			If the material uses projection or sphere mapping, the calculation functions
			pre-multiply the texture transform matrix by the entirety of this 4x4 matrix as follows:
			resMatFlags = resMat->Flags;
			if ( (resMatFlags & 0x2000) != 0 )
			{
			  Transform = resMat->Transform;
			  if ( (resMatFlags & 2) == 0 )
			  {
				Transform += 8;
			  }
			  if ( (resMatFlags & 4) == 0 )
			  {
				Transform += 4;
			  }
			  if ( (resMatFlags & 8) == 0 )
			  {
				Transform += 8;
			  }
			  NNS_G3DExecOp(GX_MTX_MULT_4X4, Transform, 16);
			}
			
			(Pokemon White 2 / NNS_G3DRenderFuncApplySphereMap @ 0x20676D8)
			 */
			textureMappingMatrix = new Matrix4();
			float[] m = data.readFX32Array(4 * 4);
			textureMappingMatrix.set(m);
			System.err.println("WARNING: Material " + name + " uses extended texture mapping.");
			System.err.println("It will be emulated using texture transforms, but the output may be broken.");
			Vec3f extT = textureMappingMatrix.getTranslation();
			Vec3f extS = textureMappingMatrix.getScale();
			Vec3f extR = textureMappingMatrix.getRotation();
			System.err.println("Scale:       " + extS);
			System.err.println("Translation: " + extT);
			System.err.println("Rotation:    " + extR);
			transU = 0f; //the matrix content will actually be *overwritten* by the texcoord, so the correct value is 0
			transV = 0f;
			scaleU = m[0]; //this is by FAR not the correct behavior, as it's only 1/3 of the dot product, but it's the best we can do
			scaleV = m[5];
		}
	}

	public Material toGeneric() {
		Material mat = new Material();
		mat.name = name;
		mat.diffuseColor = new RGBA(diffuse);
		mat.ambientColor = new RGBA(ambient);
		mat.specular0Color = new RGBA(specular);
		mat.emissionColor = new RGBA(emission);

		int faceCulling = (!renderBackFace ? 2 : 0) | (!renderFrontFace ? 1 : 0);
		mat.faceCulling = MaterialParams.FaceCulling.values()[faceCulling];

		mat.fogEnabled = enableFog;
		
		Nitroshader.setNshStencilScheme(mat, polygonID);

		if (textureName != null) {
			mat.metaData.putValue(String.format(NNS_METADATA_PALETTE_NAME_FORMAT, textureName), paletteName);
			TextureMapper mapper = new TextureMapper(textureName);
			mapper.bindRotation = rotation;
			mapper.bindScale = new Vec2f(scaleU, scaleV);
			mapper.bindTranslation = new Vec2f(transU, transV);
			mapper.mapMode = MaterialParams.TextureMapMode.UV_MAP;
			mapper.uvSetNo = 0;
			mapper.textureMagFilter = MaterialParams.TextureMagFilter.NEAREST_NEIGHBOR;
			mapper.textureMinFilter = MaterialParams.TextureMinFilter.NEAREST_NEIGHBOR; //DS does not have linear interp at all

			mapper.mapU = mirrorU ? MaterialParams.TextureWrap.MIRRORED_REPEAT : MaterialParams.TextureWrap.REPEAT;
			mapper.mapV = mirrorV ? MaterialParams.TextureWrap.MIRRORED_REPEAT : MaterialParams.TextureWrap.REPEAT;

			mat.textures.add(mapper);
		}

		if (lights != 0) {
			MaterialProcessor.enableFragmentLighting(mat);
			mat.lightSetIndex = (int) (Math.log(lights) / Math.log(2));
			//0 if lights 1, 1 if lights 2 etc... We can't precisely reproduce the DS lighting with lights sets unfortunately.
		} else {
			mat.lightSetIndex = -1;
		}

		return mat;
	}

	public enum NSBMDMaterialFlag {
		TEXMTX_EXISTS,
		TEXMTX_IDENTITY_S,
		TEXMTX_IDENTITY_R,
		TEXMTX_IDENTITY_T,
		FLAG_0x10,
		POLYGON_WIREFRAME,
		SHADING_DIFFUSE,
		SHADING_AMBIENT,
		SHADING_VCOL_FROM_AMBIENT,
		SHADING_SPECULAR,
		SHADING_EMISSION,
		SHADING_SHININESS,
		FLAG_0x1000,
		TEXMAP_MATRIX_EXISTS;
	}
}
