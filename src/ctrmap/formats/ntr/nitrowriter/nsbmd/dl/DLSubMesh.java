package ctrmap.formats.ntr.nitrowriter.nsbmd.dl;

import ctrmap.formats.ntr.common.gfx.commands.GEDisplayList;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxLoadIdentity;
import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxScale;
import ctrmap.formats.ntr.common.gfx.commands.mtx.stack.MtxStkLoad;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxListEnd;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Texture;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Matrix3f;

public class DLSubMesh {

	public static final int SUBMESH_STACK_MAX = 32;

	public static final boolean DLSUBMESH_DEBUG = false;

	private float subMeshScaleX = 1f;
	private float subMeshScaleY = 1f;
	private float subMeshScaleZ = 1f;

	private float modelPosScale;

	public Vec3f bboxMin;
	public Vec3f bboxMax;

	public MeshAttributes meshAttributes;

	Map<JointBinding, Integer> bindings = new HashMap<>();
	public Vec2f texDim;

	private int mtxStackMax = 0;

	private Map<JointBinding, List<SeparablePrimitive>> primitivesByJointConsistent = new HashMap<>();
	private List<SeparablePrimitive> inconsistentJointPrimitives = new ArrayList<>();

	public DLSubMesh(MeshAttributes meshAttributes) {
		this.meshAttributes = meshAttributes;
	}

	public void makePositionsAbs(Skeleton skeleton) {
		for (Map.Entry<JointBinding, List<SeparablePrimitive>> jointMesh : primitivesByJointConsistent.entrySet()) {
			JointBinding binding = jointMesh.getKey();

			boolean useJointMtx = false;

			Matrix4 jointMtx;
			Matrix3f normalMtx;
			if (binding.isNoJoint() || binding.isSmoSk()) {
				jointMtx = null;
				normalMtx = null;
			} else {
				//Rigid skinned vertices are pre-multiplied with the inverse bind pose matrix, unless the mesh already uses rigid skinning
				useJointMtx = true;
				jointMtx = skeleton.bindTransforms.get(binding.jointIds[0]).getInverseMatrix();
				normalMtx = jointMtx.normal(new Matrix3f());
			}

			for (SeparablePrimitive p : jointMesh.getValue()) {
				p.positionsAbs = new Vec3f[p.vertices.length];
				if (meshAttributes.hasNormal) {
					p.normalsAbs = new Vec3f[p.vertices.length];
				}
				int i = 0;
				for (Vertex vtx : p.vertices) {
					Vec3f tPosition = new Vec3f(vtx.position);
					Vec3f tNormal = null;
					if (meshAttributes.hasNormal && vtx.normal != null) {
						tNormal = new Vec3f(vtx.normal);
					}
					if (useJointMtx && p.skinningType != Mesh.SkinningType.RIGID) {
						tPosition.mulPosition(jointMtx);
						if (tNormal != null) {
							tNormal.mul(normalMtx);
						}
					}
					p.positionsAbs[i] = tPosition;
					if (meshAttributes.hasNormal) {
						p.normalsAbs[i] = tNormal;
					}
					i++;
				}
			}
		}

		for (SeparablePrimitive p : inconsistentJointPrimitives) {
			p.positionsAbs = new Vec3f[p.vertices.length];
			if (meshAttributes.hasNormal) {
				p.normalsAbs = new Vec3f[p.vertices.length];
			}
			for (int i = 0; i < p.vertices.length; i++) {
				Vertex vtx = p.vertices[i];

				Vec3f tPosition = new Vec3f(vtx.position);
				Vec3f tNormal = vtx.normal == null || !meshAttributes.hasNormal ? null : new Vec3f(vtx.normal);

				if (p.jointBindings[i].isRgdSk()) {
					Matrix4 jointMtx = skeleton.bindTransforms.get(p.jointBindings[i].jointIds[0]).getInverseMatrix();
					tPosition.mulPosition(jointMtx);
					if (tNormal != null) {
						tNormal.mul(jointMtx.normal(new Matrix3f()));
					}
				}

				p.positionsAbs[i] = tPosition;
				if (tNormal != null) {
					p.normalsAbs[i] = tNormal;
				}
			}
		}
	}

	public void applyInvGlobalScale(float invPosScale) {
		modelPosScale = 1f / invPosScale;
		for (SeparablePrimitive p : getAllPrimitives()) {
			for (Vec3f pos : p.positionsAbs) {
				pos.mul(invPosScale);
			}
		}

		createAbsoluteBoundingBox();
		//The bounding box now has posScale applied

		float maxX = Math.max(Math.abs(bboxMin.x), Math.abs(bboxMax.x));
		float maxY = Math.max(Math.abs(bboxMin.y), Math.abs(bboxMax.y));
		float maxZ = Math.max(Math.abs(bboxMin.z), Math.abs(bboxMax.z));
		if (maxX > FX.FX16_MAX) {
			subMeshScaleX = (maxX + 4) / FX.FX16_MAX;
		}
		if (maxY > FX.FX16_MAX) {
			subMeshScaleY = (maxY + 4) / FX.FX16_MAX;
		}
		if (maxZ > FX.FX16_MAX) {
			subMeshScaleZ = (maxZ + 4) / FX.FX16_MAX;
		}
		if (subMeshScaleX != 1f || subMeshScaleY != 1f || subMeshScaleZ != 1f) {
			Vec3f subMeshScaleInv = new Vec3f(1f / subMeshScaleX, 1f / subMeshScaleY, 1f / subMeshScaleZ);
			for (SeparablePrimitive p : getAllPrimitives()) {
				for (int i = 0; i < p.positionsAbs.length; i++) {
					p.positionsAbs[i].mul(subMeshScaleInv);
				}
			}
		}
	}

	public void createAbsoluteBoundingBox() {
		List<SeparablePrimitive> ps = getAllPrimitives();

		bboxMin = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		bboxMax = new Vec3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

		for (SeparablePrimitive p : ps) {
			for (Vec3f ap : p.positionsAbs) {
				bboxMin.min(ap);
				bboxMax.max(ap);
			}
		}
	}

	public List<SeparablePrimitive> getAllPrimitives() {
		List<SeparablePrimitive> ps = new ArrayList<>(inconsistentJointPrimitives);
		for (Map.Entry<JointBinding, List<SeparablePrimitive>> jointMesh : primitivesByJointConsistent.entrySet()) {
			ps.addAll(jointMesh.getValue());
		}
		return ps;
	}

	public void prepareForConversion(Skeleton skeleton, Texture texture) {
		skeleton.buildTransforms();
		makePositionsAbs(skeleton);
		createAbsoluteBoundingBox();
		texDim = texture != null ? new Vec2f(texture.width, texture.height) : null;
		meshAttributes.hasUV &= texture != null;
	}

	public boolean isSingleJointRgdSk() {
		if (bindings.size() == 1) {
			for (JointBinding jb : bindings.keySet()) {
				return jb.isRgdSk();
			}
		}
		return false;
	}
	
	private static void addScaleCmd(GEDisplayList dl, Vec3f vec) {
		if (vec.x != 1f || vec.y != 1f || vec.z != 1f) {
			dl.addCommand(new MtxScale(vec));
		}
	}

	public GEDisplayList createDisplayList() {
		GEDisplayList dl = new GEDisplayList();

		Vec3f posScaleVec = new Vec3f(modelPosScale);
		Vec3f subMeshScaleVec = new Vec3f(subMeshScaleX, subMeshScaleY, subMeshScaleZ);
		Vec3f omniScaleVec = new Vec3f(posScaleVec).mul(subMeshScaleVec);

		DisplayListPrimitiveFactory primitiveFactory = new DisplayListPrimitiveFactory(this, dl);

		for (Map.Entry<JointBinding, List<SeparablePrimitive>> jointMesh : primitivesByJointConsistent.entrySet()) {
			JointBinding binding = jointMesh.getKey();

			if (!binding.isNoJoint()) {
				if (!isSingleJointRgdSk()) {
					//If the submesh IS composed of only a single node, we can call MTX_RESTORE using the SBC instead
					if (DLSUBMESH_DEBUG) {
						System.out.println("loading matrix for joint " + binding.jointIds[0] + " from " + getJointMtxId(binding));
					}
					dl.addCommand(new MtxStkLoad(getJointMtxId(binding)));
					addScaleCmd(dl, omniScaleVec);
				} else {
					getJointMtxId(binding); //only register the matrix in the stack - is assigned thru SBC
					addScaleCmd(dl, subMeshScaleVec);
				}
			}
			else {
				dl.addCommand(new MtxLoadIdentity());
				addScaleCmd(dl, omniScaleVec);
			}

			for (SeparablePrimitive p : jointMesh.getValue()) {
				primitiveFactory.addPrimitive(p);
			}
		}

		JointBinding currentJointBinding = null;

		for (SeparablePrimitive p : inconsistentJointPrimitives) {
			primitiveFactory.setChangePrimitiveMode(p.type);
			for (int i = 0; i < p.vertices.length; i++) {
				if (!p.jointBindings[i].equals(currentJointBinding)) {
					currentJointBinding = p.jointBindings[i];
					dl.addCommand(new MtxStkLoad(getJointMtxId(currentJointBinding)));
					dl.addCommand(new MtxScale(omniScaleVec));
				}
				primitiveFactory.addVertex(p, i);
			}
		}

		dl.addCommand(new VtxListEnd());

		return dl;
	}

	public void addPrimitives(JointBinding b, List<SeparablePrimitive> primitives) {
		if (b == null) {
			inconsistentJointPrimitives.addAll(primitives);
		} else {
			primitivesByJointConsistent.put(b, primitives);
		}
	}

	public void addUnboundPrimitive(SeparablePrimitive p) {
		inconsistentJointPrimitives.add(p);
	}

	public int getJointMtxIdExistOnly(JointBinding binding) {
		return bindings.getOrDefault(binding, -1);
	}

	public int getJointMtxId(JointBinding binding) {
		int idx = bindings.getOrDefault(binding, -1);
		if (idx != -1) {
			return idx;
		}
		if (mtxStackMax == SUBMESH_STACK_MAX) { //leave out some of the stack just to be sure
			return -1;
		}
		bindings.put(binding, mtxStackMax);
		return mtxStackMax++;
	}

	public boolean hasMtxForIndex(int jntIdx) {
		for (JointBinding bind : bindings.keySet()) {
			if (bind.isRgdSk()) {
				if (bind.jointIds[0] == jntIdx) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean canFitIndicesCount(int count) {
		return bindings.size() + count < SUBMESH_STACK_MAX;
	}
}
