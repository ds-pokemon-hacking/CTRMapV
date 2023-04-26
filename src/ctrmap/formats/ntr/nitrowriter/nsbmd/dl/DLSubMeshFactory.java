package ctrmap.formats.ntr.nitrowriter.nsbmd.dl;

import ctrmap.formats.ntr.nitrowriter.nsbmd.NSBMDExportSettings;
import ctrmap.formats.ntr.nitrowriter.nsbmd.mat.NitroMaterialResource;
import ctrmap.renderer.scene.model.Face;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.util.PrimitiveConverter;
import xstandard.math.vec.Vec3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DLSubMeshFactory {

	//private final Mesh mesh;
	public final String meshName;

	private MeshAttributes meshAttributes;

	private NitroMaterialResource material;

	private List<SeparablePrimitive> separablePrimitives = new ArrayList<>();

	private Map<JointBinding, List<SeparablePrimitive>> primitivesByJointConsistent = new HashMap<>();
	private List<SeparablePrimitive> inconsistentJointPrimitives = new ArrayList<>();

	private DLSubMeshManager subMeshes;

	private NSBMDExportSettings settings;

	public DLSubMeshFactory(Model mdl, List<Mesh> meshes, NSBMDExportSettings settings) {
		this(mdl, meshes, settings, -1);
	}

	public DLSubMeshFactory(Model mdl, List<Mesh> meshes, NSBMDExportSettings settings, int forcedJointIdx) {
		this.settings = settings;
		for (int i = 0; i < meshes.size(); i++) {
			Mesh mesh = meshes.get(i);

			switch (mesh.primitiveType) {
				case LINES:
					//blank mesh - line primitives are not supported
					meshes.remove(i);
					i--;
					break;
				case TRIFANS:
					//triangle fan primitives are not supported -> convert them to normal triangles
					//theoretically we could tristrip those further, but there as far as I know noone will be using trifans anyway, so...
					meshes.set(i, PrimitiveConverter.getTriMesh(mesh));
					break;
				default:
					break;
			}
		}

		Mesh m = meshes.get(0); //for reference

		meshName = m.name;
		meshAttributes = new MeshAttributes(m);

		if (settings.preferVColOverNormal) {
			if (meshAttributes.hasColor && meshAttributes.hasNormal) {
				meshAttributes.hasNormal = false;
			}
		}

		subMeshes = new DLSubMeshManager(meshAttributes);

		for (Mesh mesh : meshes) {
			for (Face face : mesh.faces()) {
				int visGroupIndex = mdl.visGroups.indexOf(mdl.getVisGroupByName(mesh.visGroupName));
				if (visGroupIndex == -1) {
					visGroupIndex = 0;
				}
				separablePrimitives.add(new SeparablePrimitive(mesh.primitiveType, mesh.skinningType, visGroupIndex, face, forcedJointIdx));
			}
		}

		sortPrimitivesByJoint();
	}

	public NitroMaterialResource getMaterial() {
		return material;
	}

	public void attachMaterial(NitroMaterialResource mat) {
		this.material = mat;

		if (mat.hasLighting() && meshAttributes.hasColor && settings.preferVColOverNormal) {
			mat.disableLighting();
		}

		meshAttributes.hasNormal &= mat.hasLighting();
		meshAttributes.hasColor &= !meshAttributes.hasNormal;
	}

	private void sortPrimitivesByJoint() {
		for (SeparablePrimitive p : separablePrimitives) {
			//MTX_RESTORE takes A LOT of cycles (36 - making it the 3rd most demanding operation along with MTX_POP)
			//For this reason, I deemed it better to sort primitives per joint wherever possible, reducing the number of MTX_RESTORE commands
			//The only disadvantage of this is that we may have to issue more color/normal/UV commands instead of using the cache

			if (p.areJointBindingsConsistent()) {
				Map<JointBinding, List<SeparablePrimitive>> map = primitivesByJointConsistent;
				JointBinding jbKey = p.jointBindings[0];
				List<SeparablePrimitive> l = map.get(jbKey);
				if (l == null) {
					l = new ArrayList<>();
					map.put(jbKey, l);
				}
				l.add(p);
			} else {
				//These require their own MTX_RESTORE for more than one vertex - VERY demanding
				inconsistentJointPrimitives.add(p);
			}
		}
	}

	public void applyInvGlobalScale(float invGlobalScale) {
		for (DLSubMesh sm : subMeshes) {
			sm.applyInvGlobalScale(invGlobalScale);
		}
	}

	private void createSubMeshes(Skeleton skl) {
		for (Map.Entry<JointBinding, List<SeparablePrimitive>> e : primitivesByJointConsistent.entrySet()) {
			subMeshes.notifyJoints(skl, e.getKey());
			subMeshes.getCurrentSubMesh().addPrimitives(e.getKey(), e.getValue());
		}

		for (SeparablePrimitive p : inconsistentJointPrimitives) {
			subMeshes.notifyJoints(skl, p.jointBindings);
			subMeshes.getCurrentSubMesh().addUnboundPrimitive(p);
		}
	}

	public int getSubMeshCount() {
		return subMeshes.getSubMeshCount();
	}

	public void removeOverSubMeshes(int surplus) {
		if (surplus != 0) {
			subMeshes.removeOverSubMeshes(surplus);
		}
	}

	public List<NitroMeshResource> getMeshResources() {
		List<NitroMeshResource> l = new ArrayList<>();
		boolean multiNames = subMeshes.getSubMeshCount() > 1;
		int subMeshNo = 1;
		for (DLSubMesh sm : subMeshes) {
			l.add(new NitroMeshResource(this, sm, multiNames ? meshName + "_" + subMeshNo : meshName));
			subMeshNo++;
		}
		return l;
	}

	private List<SeparablePrimitive> getAllPrimitives() {
		List<SeparablePrimitive> ps = new ArrayList<>(inconsistentJointPrimitives);
		for (Map.Entry<JointBinding, List<SeparablePrimitive>> jointMesh : primitivesByJointConsistent.entrySet()) {
			ps.addAll(jointMesh.getValue());
		}
		return ps;
	}

	public int getAvgVertexAlpha() {
		long a = 255;
		if (meshAttributes.hasColor) {
			List<SeparablePrimitive> primitives = getAllPrimitives();
			if (!primitives.isEmpty()) {
				a = 0;
				int vc = 0;
				for (SeparablePrimitive p : primitives) {
					for (Vertex v : p.vertices) {
						a += v.color.a;
						vc++;
					}
				}
				a /= vc;
			}
		}
		return (int) a;
	}

	public Vec3f getAllSubMeshesBBoxMin() {
		Vec3f bboxMin = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		for (DLSubMesh sm : subMeshes) {
			bboxMin.min(sm.bboxMin);
		}
		return bboxMin;
	}

	public Vec3f getAllSubMeshesBBoxMax() {
		Vec3f bboxMax = new Vec3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		for (DLSubMesh sm : subMeshes) {
			bboxMax.max(sm.bboxMax);
		}
		return bboxMax;
	}

	public void prepareSubMeshesForConversion(Skeleton skeleton, Texture texture) {
		createSubMeshes(skeleton);

		for (DLSubMesh sm : subMeshes) {
			sm.prepareForConversion(skeleton, texture);
		}
	}

}
