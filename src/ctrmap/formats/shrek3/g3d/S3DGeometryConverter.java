package ctrmap.formats.shrek3.g3d;

import ctrmap.formats.ntr.common.gfx.Nitroshader;
import ctrmap.formats.ntr.common.gfx.commands.AbstractGECommandProcessor;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEDisplayList;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatTexImageParamSet;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxMode;
import ctrmap.formats.ntr.common.gfx.commands.poly.PolyAttrSet;
import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.util.MaterialProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.joml.Matrix3f;
import org.joml.Matrix4x3f;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import xstandard.util.ArraysEx;

public class S3DGeometryConverter extends AbstractGECommandProcessor {

	private Material curMaterial;
	private Mesh curMesh;
	private int curBoneIndex = -1;

	private final Map<Integer, String> texOffsetToNameMap = new HashMap<>();
	private final Map<Integer, Joint> offsetToBoneMap = new HashMap<>();

	private boolean curBoneAcceptsTransforms = false;
	private Joint curBone = null;
	private String curParentBone = null;
	private final int[] boneIndexStack = new int[32];

	private final List<Mesh> meshes = new ArrayList<>();
	private final List<Material> materials = new ArrayList<>();

	public S3DGeometryConverter() {
		for (int i = 0; i < boneIndexStack.length; i++) {
			boneIndexStack[i] = -1;
		}
	}

	public void getResult(Model dest) {
		for (Mesh m : meshes) {
			dest.addMesh(m);
		}
		for (Material mat : materials) {
			dest.addMaterial(mat);
		}
	}

	public void registerTexture(int vramOffset, String textureName) {
		texOffsetToNameMap.put(vramOffset, textureName);
	}

	public void registerBone(int fifoOffset, Joint bone) {
		offsetToBoneMap.put(fifoOffset, bone);
	}

	public void convertDisplayList(GEDisplayList dl) {
		curMaterial = null;
		curMesh = null;
		curBoneIndex = -1;

		int pos = 0;
		int cmdIndex = 0;
		for (GECommand cmd : dl) {
			if ((cmdIndex & 3) == 0) {
				pos += 4; //packed command header
			}
			Joint bone = offsetToBoneMap.get(pos);
			if (bone != null && nowMatrixMode != MtxMode.GEMatrixMode.TEXTURE && cmd.getOpCode().argCount > 1) {
				//System.out.println("bone! " + bone.name + " parent " + curParentBone + " at cmd " + cmd);
				curBone = bone;
				if (!Objects.equals(curParentBone, curBone.name)) {
					curBone.parentName = curParentBone;
				}
				curBoneIndex = bone.getIndex();
				curBoneAcceptsTransforms = true;
				//System.out.println("BONE before cmd");
			}
			//System.out.println("cmd " + cmd + " at " + pos);
			cmd.process(this);
			pos += cmd.sizeOf() - Integer.BYTES;
			cmdIndex++;
		}
		if (curMaterial != null) {
			materials.add(curMaterial);
		}
	}

	private boolean isMvMode() {
		return (nowMatrixMode == MtxMode.GEMatrixMode.MODELVIEW || nowMatrixMode == MtxMode.GEMatrixMode.MODELVIEW_NORMAL);
	}

	@Override
	public void pushMatrix() {
		if (isMvMode()) {
			//System.out.println("push matrix");
			boneIndexStack[getCurMatrixStack().pos()] = curBoneIndex;
			if (curBone != null) {
				curParentBone = curBone.name;
				curBoneAcceptsTransforms = false;
			}
		}
		super.pushMatrix();
	}

	@Override
	public void popMatrix(int count) {
		super.popMatrix(count);
		if (isMvMode()) {
			//System.out.println("pop matrix " + count);
			curBoneIndex = boneIndexStack[getCurMatrixStack().pos()];
			if (curBone != null) {
				curBone = curBone.parentSkeleton.getJoint(curBoneIndex);
				curBoneAcceptsTransforms = false;
				if (curBone != null) {
					curParentBone = curBone.parentName;
				} else {
					curParentBone = null;
				}
			}
		}
	}

	@Override
	public void storeMatrix(int pos) {
		super.storeMatrix(pos);
		if (isMvMode()) {
			boneIndexStack[pos] = curBoneIndex;
		}
	}

	@Override
	public void loadMatrix(int pos) {
		super.loadMatrix(pos);
		if (isMvMode()) {
			curBoneIndex = boneIndexStack[pos];
			curBone = curBone.parentSkeleton.getJoint(curBoneIndex);
		}
	}

	private void deriveBoneMatrix() {
		if (curBone != null && isMvMode() && curBoneAcceptsTransforms) {
			Matrix4 srcMatrix = new Matrix4(getCurMatrixStack().cur());
			Joint parent = curBone.getParent();
			if (parent != null) {
				srcMatrix.multiplyRight(parent.parentSkeleton.getAbsoluteJointBindPoseMatrix(parent).invert());
			}
			srcMatrix.getTranslation(curBone.position);
			srcMatrix.getScale(curBone.scale);
			srcMatrix.getRotationTo(curBone.rotation);
		}
	}
	
	@Override
	public void multMatrix3x3(Matrix3f matrix) {
		/*if (isMvMode() && curBoneAcceptsTransforms) {
			//System.out.println("bone rotate");
			new Matrix4(matrix).getRotationTo(curBone.rotation);
		} else {

		}*/
		super.multMatrix3x3(matrix);
		deriveBoneMatrix();
	}

	@Override
	public void multMatrix4x3(Matrix4x3f matrix) {
		//if (isMvMode() && curBoneAcceptsTransforms) {
			//System.out.println("bone matrix for " + curBone.name);
			/*Matrix4 m4 = new Matrix4(matrix);
			m4.getScale(curBone.scale);
			m4.getRotationTo(curBone.rotation);
			m4.getTranslation(curBone.position);*/
			/*System.out.println("trans " + curBone.position);
			System.out.println("rot " + curBone.rotation);
			System.out.println("scale " + curBone.scale);*/
		/*} else {

		}*/
		super.multMatrix4x3(matrix);
		deriveBoneMatrix();
	}

	@Override
	public void scale(float x, float y, float z) {
		/*if (isMvMode() && curBoneAcceptsTransforms) {
			//System.out.println("bone scale");
			curBone.scale.set(x, y, z);
		} else {

		}*/
		super.scale(x, y, z);
		deriveBoneMatrix();
	}

	@Override
	public void translate(float x, float y, float z) {
		/*if (isMvMode() && curBoneAcceptsTransforms) {
			//System.out.println("bone trans");
			curBone.position.set(x, y, z);
		} else {

		}*/
		super.translate(x, y, z);
		deriveBoneMatrix();
	}

	@Override
	public void color(RGBA col) {
		super.color(col);
		if (!curMesh.hasNormal) {
			curMesh.hasColor = true;
		}
	}

	@Override
	public void normal(Vec3f normal) {
		super.normal(normal);
		curMesh.hasNormal = true;
		curMesh.hasColor = false;
	}

	@Override
	public void texCoord(Vec2f texcoord) {
		super.texCoord(texcoord);
		curMesh.hasUV[0] = true;
	}

	@Override
	public void vertexEx(Vec3f vertex) {
		Vertex v = new Vertex();
		mulVertex(vertex);
		v.position = vertex;
		//if (curMesh.hasColor) {
		v.color = currentColor.clone();
		//}
		//if (curMesh.hasNormal) {
		v.normal = currentNormal.clone();
		mulNormal(v.normal);
		//}
		//if (curMesh.hasUV(0)) {
		v.uv[0] = currentTexcoord.clone();
		absTexture(v.uv[0]);
		//}
		if (curBoneIndex != -1) {
			v.boneIndices.add(curBoneIndex);
			v.weights.add(1f);
		}
		curMesh.vertices.add(v);
	}

	private boolean hasTexturedMaterial() {
		return curMaterial != null && !curMaterial.textures.isEmpty();
	}

	@Override
	public void matSpecularEmissive(RGBA spec, RGBA emi) {
		if (curMaterial != null) {
			curMaterial.specular0Color = spec;
			curMaterial.emissionColor = emi;
		}
	}

	@Override
	public void texPaletteBase(int base) {

	}

	private Material findMaterialByTexName(String name) {
		for (Material m : materials) {
			if ((name == null && m.textures.isEmpty()) || (!m.textures.isEmpty() && Objects.equals(m.textures.get(0).textureName, name))) {
				return m;
			}
		}
		if (curMaterial != null && ((name == null && curMaterial.textures.isEmpty()) || (!curMaterial.textures.isEmpty() && Objects.equals(curMaterial.textures.get(0).textureName, name)))) {
			return curMaterial;
		}
		return null;
	}

	private void switchMaterial(String newTextureName) {
		ArraysEx.addIfNotNullOrContains(materials, curMaterial);
		curMaterial = findMaterialByTexName(newTextureName);
		if (curMaterial == null) {
			curMaterial = new Material();
			curMaterial.name = "Material" + materials.size();
			if (newTextureName != null) {
				TextureMapper mapper = new TextureMapper(newTextureName);
				mapper.textureMinFilter = MaterialParams.TextureMinFilter.NEAREST_NEIGHBOR;
				mapper.textureMagFilter = MaterialParams.TextureMagFilter.NEAREST_NEIGHBOR;
				curMaterial.textures.add(mapper);
				curMaterial.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.TEX0_VCOL);
			} else {
				curMaterial.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.VCOL);
			}
		}
	}

	@Override
	public void texImage2D(int width, int height, GETextureFormat format, int offset) {
		super.texImage2D(width, height, format, offset);

		if (format == GETextureFormat.NULL) {
			switchMaterial(null);
		} else {
			String texName = texOffsetToNameMap.get(offset);
			if (texName != null) {
				//System.out.println("texname " + texName);
				switchMaterial(texName);
				if (format == GETextureFormat.A3I5 || format == GETextureFormat.A5I3) {
					MaterialProcessor.setAlphaBlend(curMaterial);
				}
			} else {
				System.err.println("Could not map texture name for offset " + offset);
			}
		}
	}

	@Override
	public void texMap(boolean repeatU, boolean repeatV, boolean mirrorU, boolean mirrorV) {
		if (hasTexturedMaterial()) {
			curMaterial.textures.get(0).mapU = MaterialParams.TextureWrap.get(repeatU, mirrorU);
			curMaterial.textures.get(0).mapV = MaterialParams.TextureWrap.get(repeatV, mirrorV);
		}
	}

	@Override
	public void texGenMode(MatTexImageParamSet.GETexcoordGenMode mode) {
		if (hasTexturedMaterial()) {
			MaterialParams.TextureMapMode mapMode = MaterialParams.TextureMapMode.UV_MAP;
			switch (mode) {
				case NONE:
				case TEXCOORD:
					mapMode = MaterialParams.TextureMapMode.UV_MAP;
					break;
				case NORMAL:
					mapMode = MaterialParams.TextureMapMode.SPHERE_MAP;
					break;
				case POSITION:
					mapMode = MaterialParams.TextureMapMode.PROJECTION_MAP;
					break;
			}
			curMaterial.textures.get(0).mapMode = mapMode;
		}
	}

	@Override
	public void texColor0Transparent(boolean transparent) {
		curMaterial.alphaTest.enabled = transparent;
	}

	@Override
	public void begin(PrimitiveType primitiveMode) {
		curMesh = new Mesh();
		curMesh.name = "Mesh" + meshes.size();
		curMesh.materialName = curMaterial == null ? null : curMaterial.name;
		//curMesh.skinningType = Mesh.SkinningType.RIGID;
		curMesh.primitiveType = primitiveMode;
		curMesh.hasBoneIndices = true;
		curMesh.hasBoneWeights = true;
	}

	@Override
	public void end() {
		meshes.add(curMesh);
	}

	@Override
	public void polygonId(int polyId) {

	}

	@Override
	public void polygonAlpha(float alpha) {
		Nitroshader.setNshAlphaValue(curMaterial, (int) (alpha * 255f));
	}

	@Override
	public void setFogEnable(boolean enable) {

	}

	@Override
	public void setLightEnable(int index, boolean enable) {

	}

	@Override
	public void polygonMode(PolyAttrSet.GEPolygonMode mode) {

	}

	@Override
	public void cullFace(boolean drawFront, boolean drawBack) {
		curMaterial.faceCulling = MaterialParams.FaceCulling.get(!drawFront, !drawBack);
	}

	@Override
	public void depthFunc(PolyAttrSet.GEDepthFunction func) {
		curMaterial.depthColorMask.depthFunction = func == PolyAttrSet.GEDepthFunction.LESS ? MaterialParams.TestFunction.LESS : MaterialParams.TestFunction.EQ;
	}

	@Override
	public void dot1OverMode(PolyAttrSet.GE1DotOverMode mode) {

	}

	@Override
	public void farClipMode(PolyAttrSet.GEFarClipMode mode) {

	}

	@Override
	public void xluDepthMode(PolyAttrSet.GEXLUDepthMode mode) {

	}

}
