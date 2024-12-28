package ctrmap.formats.ntr.nitrowriter.nsbmd.mat;

import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;
import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.gfx.Nitroshader;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatColDifAmbSet;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatColSpcEmiSet;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatTexImageParamSet;
import ctrmap.formats.ntr.common.gfx.commands.poly.PolyAttrSet;
import ctrmap.formats.ntr.nitrowriter.nsbtx.TEX0;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.util.MaterialProcessor;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryValueShort;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class NitroMaterialResource extends NNSG3DResource {

	public int index;

	public String textureName;
	public String paletteName;

	public Texture texture;
	public MaterialParams.TextureWrap textureWrapU = MaterialParams.TextureWrap.CLAMP_TO_EDGE;
	public MaterialParams.TextureWrap textureWrapV = MaterialParams.TextureWrap.CLAMP_TO_EDGE;

	public Vec2f texScale = new Vec2f(1f, 1f);
	public float texRotate = 0f;
	public Vec2f texTranslate = new Vec2f(0f, 0f);

	public RGBA diffuseColor;
	public RGBA ambientColor;
	public RGBA specularColor;
	public RGBA emissionColor;

	public int vertexAlpha; //range 0 to 255 !
	public boolean isXLU;

	public boolean enableFog;

	public final boolean[] lights = new boolean[4];

	public MaterialParams.TestFunction depthFunction;
	public MaterialParams.FaceCulling faceCulling;

	public int polygonID;

	public NitroMaterialResource(int index, Material mat, List<Texture> textures, int vertexAlpha) {
		name = mat.name;
		this.index = index;

		String mapTexName = null;

		if (!mat.textures.isEmpty()) {
			TextureMapper tm = mat.textures.get(0);
			mapTexName = tm.textureName;
			texture = Scene.getNamedObject(mapTexName, textures);
			if (texture == null) {
				System.out.println("Could not find texture " + tm.textureName);
			}
			textureWrapU = tm.mapU;
			textureWrapV = tm.mapV;
			texScale = tm.bindScale;
			texRotate = tm.bindRotation;
			texTranslate = tm.bindTranslation;
		}
		isXLU = MaterialProcessor.isAlphaBlendUsed(mat);
		enableFog = mat.fogEnabled;
		diffuseColor = mat.diffuseColor;
		ambientColor = mat.ambientColor;
		specularColor = mat.specular0Color;
		emissionColor = mat.emissionColor;
		this.vertexAlpha = vertexAlpha;

		if (MaterialProcessor.isLightingUsed(mat)) {
			if (mat.lightSetIndex < 4) {
				lights[mat.lightSetIndex] = true;
			}
		}

		depthFunction = mat.depthColorMask.depthFunction;
		faceCulling = mat.faceCulling;

		if (Nitroshader.isNshStencilSchemeUsed(mat)) {
			polygonID = mat.stencilTest.reference;
		} else {
			if (isXLU) {
				polygonID = index & 31;
			}
		}

		textureName = mapTexName;
		paletteName = textureName;
		if (texture != null) {
			paletteName = ReservedMetaData.getIdxTexPalName(texture);
		}
	}

	public void syncTEX0(TEX0 tex0) {
		if (tex0 != null && texture != null) {
			String ogName = textureName;
			textureName = tex0.getRenamedTexName(textureName);
			System.out.println(ogName + " -> " + textureName);
			if (textureName == null) {
				textureName = ogName; //fallback
			}
		}
		if (textureName != null) {
			if (tex0 != null) {
				String ogName = paletteName;
				paletteName = tex0.getRenamedPaletteName(paletteName);
				if (paletteName == null) {
					paletteName = ogName;
				}
			}
		}
	}

	public boolean hasLighting() {
		for (boolean b : lights) {
			if (b) {
				return b;
			}
		}
		return false;
	}

	public void disableLighting() {
		for (int i = 0; i < lights.length; i++) {
			lights[i] = false;
		}
	}

	@Override
	public byte[] getBytes() throws IOException {
		DataIOStream out = new DataIOStream();

		out.writeShort(0); //constant ??
		TemporaryValueShort size = new TemporaryValueShort(out);

		new MatColDifAmbSet(diffuseColor, ambientColor).writeParams(out);
		new MatColSpcEmiSet(specularColor, emissionColor).writeParams(out);

		PolyAttrSet polyAttr = new PolyAttrSet();
		polyAttr.constVertexAlpha = isXLU ? vertexAlpha : 255; //force to 255 if the material does not use alpha blending

		for (int i = 0; i < 4; i++) {
			polyAttr.lightsEnabled[i] = lights[i];
		}

		polyAttr.depthFunc = depthFunction == MaterialParams.TestFunction.EQ ? PolyAttrSet.GEDepthFunction.EQUAL : PolyAttrSet.GEDepthFunction.LESS;
		polyAttr.drawFrontFace = faceCulling == MaterialParams.FaceCulling.NEVER || faceCulling == MaterialParams.FaceCulling.BACK_FACE;
		polyAttr.drawBackFace = faceCulling == MaterialParams.FaceCulling.NEVER || faceCulling == MaterialParams.FaceCulling.FRONT_FACE;
		polyAttr.enableFog = enableFog;

		polyAttr.polygonId = polygonID;

		polyAttr.writeParams(out);

		out.writeInt(0x3F1FF8FF); //constant again - exactly matches the unused bits in POLYGON_ATTR at GBATek

		MatTexImageParamSet texImg = new MatTexImageParamSet();
		texImg.repeatU = textureWrapU == MaterialParams.TextureWrap.REPEAT || textureWrapU == MaterialParams.TextureWrap.MIRRORED_REPEAT;
		texImg.repeatV = textureWrapV == MaterialParams.TextureWrap.REPEAT || textureWrapV == MaterialParams.TextureWrap.MIRRORED_REPEAT;
		texImg.mirrorU = textureWrapU == MaterialParams.TextureWrap.MIRRORED_REPEAT;
		texImg.mirrorV = textureWrapV == MaterialParams.TextureWrap.MIRRORED_REPEAT;
		if (textureName != null) {
			texImg.texGenMode = MatTexImageParamSet.GETexcoordGenMode.TEXCOORD;
		} else {
			texImg.texGenMode = MatTexImageParamSet.GETexcoordGenMode.NONE;
		}
		texImg.writeParams(out); //only these two params are set from the material - rest is from the TEX0

		out.writeInt(0xFFFFFFFF); //TEXIMAGE_PARAM has no unused bits

		out.writeShort(0);

		int flags = 0b1111111000001;
		//default flags for 99% of cases - we just have to set the transform stuff
		//we set the texture matrix to always be enabled since we can't possibly know if the user decides to use SRT animations later

		FX.Vec2FX32 scaleVec = new FX.Vec2FX32(texScale);
		FX.Vec2FX32 translateVec = new FX.Vec2FX32(texTranslate);

		boolean scale1 = scaleVec.equals(FX.ONE_VEC2);
		boolean translate0 = translateVec.equals(FX.ZERO_VEC2);
		boolean rotate0 = texRotate == 0f;

		if (scale1) {
			flags |= 2;
		}
		if (rotate0) {
			flags |= 4;
		}
		if (translate0) {
			flags |= 8;
		}

		out.writeShort(flags);
		out.writeShort(texture == null ? 0 : texture.width);
		out.writeShort(texture == null ? 0 : texture.height);

		out.writeInt(FX.fx32(1f));
		out.writeInt(FX.fx32(1f)); //no clue what these do but they seem to be constant

		if (!scale1) {
			scaleVec.write(out);
		}
		if (!rotate0) {
			out.writeInt(FX.fx32((float) Math.sin(texRotate)));
			out.writeInt(FX.fx32((float) Math.cos(texRotate)));
		}
		if (!translate0) {
			translateVec.write(out);
		}

		size.set(out.getLength());

		out.close();
		return out.toByteArray();
	}
}
