package ctrmap.formats.ntr.nitroreader.nsbmd;

import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.common.gfx.Nitroshader;
import ctrmap.formats.ntr.common.gfx.commands.AbstractGECommandProcessor;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatTexImageParamSet;
import ctrmap.formats.ntr.common.gfx.commands.poly.PolyAttrSet;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.MeshVisibilityGroup;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.util.MaterialProcessor;
import ctrmap.renderer.util.MeshProcessor;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import xstandard.util.collections.FloatList;
import xstandard.util.collections.IntList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NSBMDModelConverter extends AbstractGECommandProcessor {

	public NSBMDModel mdl;

	private NSBMDMeshConverter meshConv;

	public List<Joint> joints = new ArrayList<>();
	public List<Mesh> meshes = new ArrayList<>();
	public List<Material> materials = new ArrayList<>();
	public List<MeshVisibilityGroup> visGroups = new ArrayList<>();

	public IntList currentJointIndices = new IntList();
	public FloatList currentJointWeights = new FloatList();

	private Map<Integer, MeshVisibilityGroup> visGroupMap = new HashMap<>();
	private MeshVisibilityGroup currentVisGroup;

	public int constantAlpha = 255;
	private Material currentMaterial;
	public boolean enableLighting;

	private float globalScale = 1f;
	private float globalScaleInv = 1f;

	public IntList[] jointIds = new IntList[32];
	private FloatList[] jointWeights = new FloatList[32];

	private NSBMDImportSettings settings;

	public NSBMDModelConverter(NSBMDModel mdl, NSBMDImportSettings settings) {
		this.mdl = mdl;
		this.settings = settings;

		this.globalScale = mdl.getModelHeader().posScale;
		this.globalScaleInv = mdl.getModelHeader().invPosScale;

		for (int i = 0; i < modelViewStack.size(); i++) {
			jointIds[i] = new IntList();
			jointWeights[i] = new FloatList();
		}

		Skeleton dummySkeleton = new Skeleton();
		for (NSBMDJoint n : mdl.joints) {
			Joint j = n.toGeneric();
			dummySkeleton.addJoint(j);
			joints.add(j);
		}
	}

	public String getModelName() {
		return mdl.name;
	}

	public Joint getJointById(int id) {
		return joints.get(id);
	}

	public void setVisGroup(int id, boolean defaultVisibility) {
		currentVisGroup = visGroupMap.get(id);
		if (currentVisGroup == null) {
			currentVisGroup = new MeshVisibilityGroup("VisGroup" + id);
			currentVisGroup.isVisible = defaultVisibility;
			visGroupMap.put(id, currentVisGroup);
			visGroups.add(currentVisGroup);
		}
	}

	public NSBMDMeshConverter getMeshConv() {
		return meshConv;
	}

	public void setJointMatrix(Matrix4 mtx, int jointIndex) {
		multMatrix4x4(mtx);
		currentJointIndices.clear();
		currentJointWeights.clear();
		currentJointIndices.add(jointIndex);
		currentJointWeights.add(1f);
	}

	@Override
	public void loadMatrix(int indexIntoCache) {
		super.loadMatrix(indexIntoCache);
		/*System.out.println("loadMatrix " + indexIntoCache);
		System.out.println("S " + matrix.getScale());
		System.out.println("T " + matrix.getTranslation());*/
		setJointDataFromMtxCache(indexIntoCache);
		scaleExternalReset();
	}

	@Override
	public void loadIdentity() {
		super.loadIdentity();
		currentJointIndices.clear();
		currentJointWeights.clear();
	}

	@Override
	public void storeMatrix(int index) {
		super.storeMatrix(index);
		jointIds[index] = new IntList(currentJointIndices);
		jointWeights[index] = new FloatList(currentJointWeights);
	}

	public void setJointDataFromMtxCache(int idx) {
		currentJointIndices.clear();
		currentJointWeights.clear();
		currentJointIndices.addAll(jointIds[idx]);
		currentJointWeights.addAll(jointWeights[idx]);
	}

	public void setMatrixCacheDataFromJoint(int indexIntoCache, Matrix4 data, int jointIndex) {
		loadMatrix4x4(data);
		storeMatrix(indexIntoCache);
		jointIds[indexIntoCache] = IntList.wrap(jointIndex);
		jointWeights[indexIntoCache] = FloatList.wrap(1f);
	}

	public void setMatrixCacheDataFromJoint(int indexIntoCache, Matrix4 data, IntList jointIndices, FloatList weights) {
		loadMatrix4x4(data);
		storeMatrix(indexIntoCache);
		jointIds[indexIntoCache] = jointIndices;
		jointWeights[indexIntoCache] = weights;
	}

	public Joint getCurrentSingleJoint() {
		if (currentJointIndices.size() == 1) {
			return getJointById(currentJointIndices.get(0));
		}
		return null;
	}

	Vec3f scaleExternal = new Vec3f(1f, 1f, 1f);

	public void setGlobalScale(boolean isInv) {
		float ps = isInv ? globalScaleInv : globalScale;
		scaleExternal.set(ps);
		super.scale(ps, ps, ps);
	}

	@Override
	public void scale(float x, float y, float z) {
		super.scale(x, y, z);
		scaleExternalApply(new Vec3f(x, y, z));
	}

	public void scaleExternalApply(Vec3f scale) {
		scaleExternal.mul(scale);
	}

	public void scaleExternalReset() {
		scaleExternal.set(1f);
	}

	public void applyMesh(int meshIdx) {
		NSBMDMesh bmdMesh = mdl.meshes.get(meshIdx);
		meshConv = new NSBMDMeshConverter(bmdMesh);

		for (GECommand cmd : bmdMesh.dl) {
			cmd.process(this);
		}

		List<Mesh> meshesFromPoly = meshConv.getMeshes(settings);

		if (currentVisGroup != null) {
			for (Mesh m : meshesFromPoly) {
				m.visGroupName = currentVisGroup.name;
			}
		}

		if (meshConv.forceConvertToSmoSk) {
			Skeleton dummySkl = new Skeleton();
			dummySkl.addJoints(joints);
			for (Mesh m : meshesFromPoly) {
				m.skinningType = Mesh.SkinningType.RIGID;
				MeshProcessor.transformRigidSkinningToSmooth(m, dummySkl, true, true);
			}
		}

		boolean isMaterialOccupied = false;
		boolean hasMaterialVCol = false;
		for (Mesh m : meshes) {
			if (m.getMaterial(materials) == currentMaterial) {
				isMaterialOccupied = true;
				hasMaterialVCol = m.hasColor;
				break;
			}
		}

		for (Mesh m : meshesFromPoly) {
			if (isMaterialOccupied) {
				if (!m.hasColor) {
					if (hasMaterialVCol) {
						MeshProcessor.clearVCol(m);
					}
				} else {
					if (!hasMaterialVCol) {
						for (Mesh m2 : meshes) {
							if (m2.getMaterial(materials) == currentMaterial) {
								MeshProcessor.clearVCol(m2);
							}
						}
					}
				}
			}

			if (currentMaterial != null) {
				if (!m.hasUV[0]) {
					if (!currentMaterial.textures.isEmpty()) {
						currentMaterial.textures.get(0).uvSetNo = -1;
					}
				}
				if (enableLighting) {
					m.hasColor = false;
					//will get set right back on ONLY if vertex alpha is used
				}
				m.materialName = currentMaterial.name;
				boolean hasVertexAlpha = constantAlpha != 255;

				if (!m.hasColor && hasVertexAlpha) {
					for (Vertex v : m.vertices) {
						v.color = new RGBA(255, 255, 255, constantAlpha);
					}
					m.hasColor = true;
				}
				if (m.hasColor) {
					currentMaterial.tevStages.stages[0].setTemplate(currentMaterial.textures.isEmpty() ? TexEnvStage.TexEnvTemplate.VCOL : TexEnvStage.TexEnvTemplate.TEX0_VCOL);
					if (hasVertexAlpha) {
						MaterialProcessor.setAlphaBlend(currentMaterial);
						m.renderLayer = 1;
					}
				} else {
					currentMaterial.tevStages.stages[0].setTemplate(TexEnvStage.TexEnvTemplate.TEX0);
				}

				Nitroshader.ensureNsh(currentMaterial);
				Nitroshader.setNshAlphaValue(currentMaterial, constantAlpha);
			}
		}

		meshes.addAll(meshesFromPoly);
	}

	private final Map<NSBMDMaterial, Material> matHistory = new HashMap<>();

	public void applyMaterial(int materialIdx) {
		NSBMDMaterial mat = mdl.materials.get(materialIdx);
		Material gm = matHistory.get(mat);
		if (gm == null) {
			gm = mat.toGeneric();
			matHistory.put(mat, gm);
		}

		if (!materials.contains(gm)) {
			materials.add(gm);
		}
		currentMaterial = gm;
		constantAlpha = mat.alpha;
		enableLighting = mat.lights != 0;
		matDiffuseAmbient(mat.diffuse, mat.ambient, mat.diffuseAsVCol);
		texImage2D(mat.texWidth, mat.texHeight, GETextureFormat.NULL, 0);
	}

	@Override
	public void matSpecularEmissive(RGBA spec, RGBA emi) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void texPaletteBase(int base) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void texMap(boolean repeatU, boolean repeatV, boolean mirrorU, boolean mirrorV) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void texGenMode(MatTexImageParamSet.GETexcoordGenMode mode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void texColor0Transparent(boolean transparent) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void begin(PrimitiveType primitiveMode) {
		getMeshConv().setNewMeshByPrimitive(primitiveMode);
	}

	@Override
	public void end() {

	}

	@Override
	public void polygonId(int polyId) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void polygonAlpha(float alpha) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setFogEnable(boolean enable) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setLightEnable(int index, boolean enable) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void polygonMode(PolyAttrSet.GEPolygonMode mode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void cullFace(boolean drawFront, boolean drawBack) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void depthFunc(PolyAttrSet.GEDepthFunction func) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void dot1OverMode(PolyAttrSet.GE1DotOverMode mode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void farClipMode(PolyAttrSet.GEFarClipMode mode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void xluDepthMode(PolyAttrSet.GEXLUDepthMode mode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void vertexEx(Vec3f vertex) {
		NSBMDMeshConverter.VertexConvState vtxConv = getMeshConv().getVertexConverter();
		vtxConv.color = currentColor;
		vtxConv.normal = currentNormal;
		if (getMeshConv().getCurrentMesh().hasUV(0)) {
			vtxConv.uv = currentTexcoord.clone();
			absTexture(vtxConv.uv);
		}
		getMeshConv().pushVertex(vertex, this);
	}

	public void setMaterialTexMap(int matIdx, MaterialParams.TextureMapMode textureMapMode) {
		Material mat = matHistory.get(mdl.materials.get(matIdx));
		if (!mat.textures.isEmpty()) {
			mat.textures.get(0).mapMode = textureMapMode;
		}
	}
}
