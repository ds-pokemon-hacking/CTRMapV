package ctrmap.missioncontrol_ntr.field;

import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.VBOProcessor;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;
import java.util.HashMap;
import java.util.Map;

public class FieldOmnimodel extends G3DResource {

	Map<String, Mesh> meshesByTexName = new HashMap<>();
	Map<String, Material> materialMap = new HashMap<>();

	public void mergeModelInst(ModelInstance instance) {
		for (MaterialAnimation mta : instance.getAllMaterialAnimations()) {
			addMatAnime(mta);
		}
		Vec3f instancePos = instance.getPosition();
		Vec3f instanceRot = instance.getRotation();
		Matrix4 mtx = null;
		if (!instanceRot.equals(Vec3f.ZERO())) {
			mtx = instance.getTransformMatrix();
		}

		for (Model mdl : instance.resource.models) {
			for (Mesh mesh : mdl.meshes) {
				/*Material mat = mesh.getMaterial(mdl);
				if (mat != null && !mat.textures.isEmpty()) {*/
					String texName = mesh.materialName;
					Mesh dst = meshesByTexName.get(texName);
					if (dst == null) {
						dst = new Mesh(mesh);
						dst.vertices.clear();
						meshesByTexName.put(texName, dst);
					}
					for (Vertex vtx : mesh.vertices) {
						Vertex newVTX = new Vertex(vtx, dst);
						if (mtx == null) {
							newVTX.position.x += instancePos.x;
							newVTX.position.y += instancePos.y;
							newVTX.position.z += instancePos.z;
						} else {
							newVTX.position.mulPosition(mtx);
						}
						dst.vertices.add(newVTX);
					}
				/*} else {
					meshesByTexName.put(mesh.name + "_" + mesh.materialName, mesh);
				}*/
			}
			for (Material mat : mdl.materials) {
				//if (!mat.textures.isEmpty()) {
					String texName = mat.name;
					if (!materialMap.containsKey(texName)) {
						materialMap.put(texName, mat);
					}
				//}
			}
		}
	}

	public void finish() {
		models.clear();
		Model mdl = new Model();
		mdl.name = "FieldOmnimodel";
		for (Mesh m : meshesByTexName.values()) {
			VBOProcessor.makeIndexed(m, false);
			mdl.addMesh(m);
		}
		for (Material m : materialMap.values()) {
			mdl.addMaterial(m);
		}
		addModel(mdl);
	}
}
