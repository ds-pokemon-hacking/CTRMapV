package ctrmap.formats.qos.g3d;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.common.gfx.Nitroshader;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GECommandDecoder;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxMode;
import ctrmap.formats.ntr.common.gfx.texture.GETextureIndexed;
import ctrmap.formats.qos.GeminiDeserializer;
import ctrmap.formats.qos.GeminiObject;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.MaterialProcessor;
import ctrmap.renderer.util.ModelProcessor;
import ctrmap.renderer.util.PrimitiveConverter;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.serialization.annotations.Inline;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeminiModel extends GeminiModelStub {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Gemini Model", "*.model");

	@Inline
	public GeminiMatrix4x3 boundingTransform;
	public int hash;
	public transient byte[] commandBuffer;
	public List<GeminiTextureInstance> textures;
	public List<Integer> polygonAttrFixups;
	public List<GeminiAnimatedValue> animatedValues;
	public List<GeminiAttachment> attachments;
	public GeminiObject extra;

	public static GeminiModel fromFile(FSFile fsf) {
		return GeminiDeserializer.deserializeFileStatic(GeminiModel.class, fsf);
	}

	public G3DResource toGeneric(FSFile rootPath) {
		G3DResource res = new G3DResource();

		Model model = new Model();
		model.name = name;
		Map<Mesh, Integer> polygonAlphas = new HashMap<>();

		try {
			GeminiModelConverter conv = new GeminiModelConverter(new GeminiModelConverter.Listener() {
				@Override
				public void onPolygonFinished(GeminiModelConverter conv, PrimitiveType primitiveType, List<Vertex> polygon) {
					Mesh m = new Mesh();
					conv.setMeshAttribs(m);
					String nameBase = null;
					if (m.hasUV(0)) {
						nameBase = FSUtil.getFileNameWithoutExtension(conv.getNowMaterial().texture.imageData.filename);
						int vIdx = 0;
						for (Vertex v : polygon) {
							if (v.uv[0] == null) {
								System.out.println("ERROR: Vertex " + vIdx + " / " + polygon.size() + " does not have UVs despite mesh attributes requiring them! @ " + name);
							}
							vIdx++;
						}
					}
					polygonAlphas.put(m, (int)(conv.getNowPolygonAlpha() * 255f));
					m.name = (nameBase == null ? "Color" : nameBase) + "-mesh";
					m.materialName = nameBase;
					m.primitiveType = primitiveType;
					m.vertices.addAll(polygon);
					if (extra != null && extra instanceof GeminiExtraData) {
						m.renderLayer = ((GeminiExtraData) extra).transparent ? 1 : 0;
					}
					model.addMesh(m);
				}
			});

			if (textures != null) {
				for (GeminiTextureInstance i : textures) {
					conv.notifyTextureInstance(i);

					Material mat = new Material();
					mat.name = FSUtil.getFileNameWithoutExtension(i.texture.imageData.filename);
					TextureMapper mapper = new TextureMapper(FSUtil.getFileName(i.texture.imageData.filename));
					mapper.textureMagFilter = MaterialParams.TextureMagFilter.NEAREST_NEIGHBOR;
					mapper.textureMinFilter = MaterialParams.TextureMinFilter.NEAREST_NEIGHBOR;
					mapper.mapU = MaterialParams.TextureWrap.get(
						i.repeat == GeminiGXTexRepeat.REPEAT_S | i.repeat == GeminiGXTexRepeat.REPEAT_ST,
						i.flip == GeminiGXTexFlip.FLIP_S | i.flip == GeminiGXTexFlip.FLIP_ST
					);
					mapper.mapV = MaterialParams.TextureWrap.get(
						i.repeat == GeminiGXTexRepeat.REPEAT_T | i.repeat == GeminiGXTexRepeat.REPEAT_ST,
						i.flip == GeminiGXTexFlip.FLIP_T | i.flip == GeminiGXTexFlip.FLIP_ST
					);
					mat.textures.add(mapper);
					mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.TEX0_VCOL);
					model.addMaterial(mat);

					FSFile texFile = rootPath.getChild(i.texture.imageData.filename);
					if (texFile.exists()) {
						try (DataIOStream in = new NTRDataIOStream(texFile)) {
							int w = 8 << i.texture.sizeS.ordinal();
							int h = 8 << i.texture.sizeT.ordinal();
							Texture convTex = new GETextureIndexed(in, GETextureFormat.values()[i.texture.format.ordinal()], w, h, i.color0 == GeminiGXTexPlttColor0.TRANSPARENT).decode(i.texture.paletteData);
							convTex.name = mapper.textureName;
							res.addTexture(convTex);
						}
					}
				}
			}

			conv.matrixMode(MtxMode.GEMatrixMode.MODELVIEW_NORMAL); //The game issues these 2 command before each model
			conv.pushMatrix();

			DataInputEx cmdStream = new DataIOStream(commandBuffer);
			int cmdStreamPos = 0;
			int[] ops = new int[4];
			while ((cmdStreamPos = cmdStream.getPosition()) < commandBuffer.length) {
				GECommandDecoder.getPackedOpcodes(cmdStream, ops);
				for (int i = 0; i < 4; i++) {
					cmdStreamPos = cmdStream.getPosition();
					int opCode = ops[i];
					GECommand cmd = GECommandDecoder.decode(opCode, cmdStream);
					if (cmd.getOpCode() != GEOpCode.NOP) {
						conv.setNowCmdOffset(cmdStreamPos);
						cmd.process(conv);
					}
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(GeminiModel.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		boolean isNullMaterialExists = false;
		for (Mesh mesh : model.meshes) {
			if (mesh.materialName == null) {
				isNullMaterialExists = true;
				mesh.materialName = "Blank";
			}
		}
		if (isNullMaterialExists) {
			Material blank = new Material();
			blank.name = "Blank";
			blank.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.VCOL);
			model.addMaterial(blank);
		}

		//Merge meshes by material
		Map<String, List<Mesh>> meshMatMap = new LinkedHashMap<>();
		for (Mesh mesh : model.meshes) {
			String key = mesh.materialName + "$" + polygonAlphas.get(mesh);
			List<Mesh> l = meshMatMap.get(key);
			if (l == null) {
				l = new ArrayList<>();
				meshMatMap.put(key, l);
			}
			l.add(mesh);
		}
		model.meshes.clear();
		for (List<Mesh> meshList : meshMatMap.values()) {
			Mesh combMesh = new Mesh();
			combMesh.materialName = meshList.get(0).materialName;
			combMesh.name = combMesh.materialName + "-mesh";
			combMesh.primitiveType = PrimitiveType.TRIS;
			combMesh.renderLayer = meshList.get(0).renderLayer;
			
			Material mat = model.getMaterialByName(combMesh.materialName);
			if (mat == null) {
				throw new RuntimeException("Material not converted: " + combMesh.materialName);
			}
			int polyAlpha = polygonAlphas.get(meshList.get(0));
			if (Nitroshader.isNshReady(mat)) {
				if (Nitroshader.getNshAlphaValue255(mat) != polyAlpha) {
					mat = new Material(mat);
					mat.name += "_alpha" + polyAlpha;
					combMesh.materialName = mat.name;
					model.addMaterial(mat);
				}
			}
			Nitroshader.setNshAlphaValue(mat, polyAlpha);
			if (polyAlpha != 255) {
				MaterialProcessor.setAlphaBlend(mat);
				combMesh.renderLayer = 1;
			}
			
			for (Mesh mesh : meshList) {
				combMesh.hasColor |= mesh.hasColor;
				combMesh.hasUV[0] |= mesh.hasUV[0];
				combMesh.hasNormal |= mesh.hasNormal;
			}
			
			for (Mesh mesh : meshList) {
				PrimitiveConverter.triangulate(mesh);
				for (Vertex vtx : mesh.vertices) {
					if (vtx.normal == null && mesh.hasNormal) {
						vtx.normal = new Vec3f(0f, 1f, 0f);
					}
					if (vtx.color == null && mesh.hasColor) {
						vtx.color = RGBA.WHITE.clone();
					}
					if (vtx.uv[0] == null && mesh.hasUV(0)) {
						vtx.uv[0] = new Vec2f();
					}
					combMesh.vertices.add(vtx);
				}
			}
			model.addMesh(combMesh);
		}

		ModelProcessor.upZtoY(model, true);

		res.addModel(model);
		MaterialProcessor.setAutoAlphaBlendByTexture(res);

		return res;
	}
}
