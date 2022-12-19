package ctrmap.formats.ntr.nitroreader.nsbmd;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.util.PrimitiveConverter;
import xstandard.text.FormattingUtils;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NSBMDMeshConverter {

	private VertexConvState vtxConv = new VertexConvState();

	private Map<PrimitiveType, Mesh> polyMeshes = new HashMap<>();
	private List<Mesh> polyStripMeshes = new ArrayList<>();
	private Mesh currentMesh = null;

	private NSBMDMesh bmdMesh;

	public boolean forceConvertToSmoSk = false;

	public NSBMDMeshConverter(NSBMDMesh bmdMesh) {
		this.bmdMesh = bmdMesh;
		for (PrimitiveType t : PrimitiveType.values()) {
			Mesh m = new Mesh();
			m.skinningType = Mesh.SkinningType.NONE;
			m.primitiveType = t;
			bmdMesh.setMeshProperty(m);
			polyMeshes.put(t, m);
		}
	}

	public Mesh getCurrentMesh() {
		return currentMesh;
	}

	public void setNewMeshByPrimitive(PrimitiveType pt) {
		switch (pt) {
			case QUADS:
			case TRIS:
				currentMesh = polyMeshes.get(pt);
				break;
			case QUADSTRIPS:
			case TRISTRIPS:
				currentMesh = new Mesh();
				currentMesh.skinningType = Mesh.SkinningType.NONE;
				currentMesh.primitiveType = pt;
				bmdMesh.setMeshProperty(currentMesh);
				polyStripMeshes.add(currentMesh);
				break;
		}
	}

	public void pushVertex(Vec3f position, NSBMDModelConverter mc) {
		Mesh m = getCurrentMesh();
		Vertex vtx = vtxConv.getVertex(position);

		if (!mc.currentJointIndices.isEmpty()) {
			vtx.boneIndices.addAll(mc.currentJointIndices);
			vtx.weights.addAll(mc.currentJointWeights);
			//The position scale is NOT applied to the joints themselves
			//In turn, to use this system on modern hardware, we need to premultiply the vertices with the position scale before applying rigid skinning
			m.hasBoneIndices = true;
			m.hasBoneWeights = true;

			if (mc.currentJointIndices.size() > 1) {
				mc.mulVertex(vtx.position);
				if (m.hasNormal) {
					mc.mulNormal(vtx.normal);
				}
			} else {
				vtx.position.mul(mc.scaleExternal);
			}

			if (!forceConvertToSmoSk) {
				boolean hasSmoSk = vtx.boneIndices.size() > 1;

				if (m.skinningType == Mesh.SkinningType.NONE) {
					if (!hasSmoSk) {
						m.skinningType = Mesh.SkinningType.RIGID;
					} else {
						forceConvertToSmoSk = true;
					}
				} else if (m.skinningType == Mesh.SkinningType.RIGID && hasSmoSk) {
					forceConvertToSmoSk = true;
					//On the DS, vertices with more than one joint are always smooth skinned
					//However, the same mesh can have rigid skinned vertices at the same time
					//We fix this by converting the vertices to smooth skinning if they have <= 1 joints
				}
			}
		} else {
			mc.mulVertex(vtx.position);
			if (m.hasNormal) {
				mc.mulNormal(vtx.normal);
			}
			/*if (mc.currentJointIndices.size() > 1) {
				System.out.println("SMOSK " + vtx.position);
			} else {
				System.out.println(vtx.position);
			}*/
		}

		m.vertices.add(vtx);
	}

	public VertexConvState getVertexConverter() {
		return vtxConv;
	}

	private boolean getShouldSeparatePrimitives() {
		PrimitiveType t = null;
		List<Mesh> l = new ArrayList<>();
		l.addAll(polyMeshes.values());
		l.addAll(polyStripMeshes);
		for (Mesh mesh : l) {
			if (!mesh.vertices.isEmpty()) {
				if (t == null) {
					t = mesh.primitiveType;
				} else {
					if (t != mesh.primitiveType) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public List<Mesh> getMeshes(NSBMDImportSettings settings) {
		List<Mesh> l = new ArrayList<>();

		if (settings != null) {
			if (settings.triangulate) {
				Mesh triMesh = polyMeshes.get(PrimitiveType.TRIS);
				Mesh quadMesh = polyMeshes.get(PrimitiveType.QUADS);

				polyStripMeshes.add(triMesh);
				polyStripMeshes.add(quadMesh);

				Map<Integer, Mesh> baseMeshes = new HashMap<>();

				for (Mesh psm : polyStripMeshes) {
					Mesh baseMesh = baseMeshes.get(psm.renderLayer);
					if (baseMesh == null) {
						baseMesh = psm;
						baseMeshes.put(psm.renderLayer, psm);
					}
					PrimitiveConverter.triangulate(psm);
					if (psm.skinningType != Mesh.SkinningType.NONE) {
						baseMesh.skinningType = psm.skinningType;
					}
					baseMesh.hasBoneIndices |= psm.hasBoneIndices;
					baseMesh.hasBoneWeights |= psm.hasBoneWeights;
					if (baseMesh != psm) {
						baseMesh.vertices.addAll(psm.vertices);
					}
				}

				for (Mesh m : baseMeshes.values()) {
					if (!m.vertices.isEmpty()) {
						l.add(m);
					}
				}

				if (l.size() > 1) {
					for (Mesh m : l) {
						m.name += "_lay" + m.renderLayer;
					}
				}

				polyStripMeshes.clear();
				polyMeshes.clear();
			} else if (settings.eliminateStrips) {
				Mesh triMesh = polyMeshes.get(PrimitiveType.TRIS);
				Mesh quadMesh = polyMeshes.get(PrimitiveType.QUADS);

				for (int i = 0; i < polyStripMeshes.size(); i++) {
					Mesh psm = polyStripMeshes.get(i);
					if (!psm.vertices.isEmpty()) {
						PrimitiveType pt = psm.primitiveType;
						PrimitiveConverter.stripsToNormal(psm);
						Mesh refMesh = pt == PrimitiveType.QUADSTRIPS ? quadMesh : triMesh;

						//Vertex refVtx = refMesh.vertices.isEmpty() ? null : refMesh.vertices.get(0);
						//if ((psm.skinningType == refMesh.skinningType || refMesh.vertices.isEmpty()) && (psm.skinningType != Mesh.SkinningType.RIGID || (refVtx == null || refVtx.boneIndices.get(0).equals(psm.vertices.get(0).boneIndices.get(0))))) {
						refMesh.vertices.addAll(psm.vertices);
						polyStripMeshes.remove(i);
						i--;

						refMesh.hasBoneIndices |= psm.hasBoneIndices;
						refMesh.hasBoneWeights |= psm.hasBoneWeights;
						if (refMesh.skinningType != Mesh.SkinningType.SMOOTH) {
							if (psm.skinningType != Mesh.SkinningType.NONE) {
								refMesh.skinningType = psm.skinningType;
							}
						}
						//} else {
						//	l.add(psm);
						//}
					}
				}

				polyStripMeshes.clear();
			} else {
				l.addAll(polyStripMeshes);
			}
		} else {
			l.addAll(polyStripMeshes);
		}

		for (Mesh mesh : polyMeshes.values()) {
			if (!mesh.vertices.isEmpty()) {
				l.add(mesh);
			}
		}

		if (getShouldSeparatePrimitives()) {
			for (Mesh m : l) {
				m.name += "_" + FormattingUtils.getFriendlyEnum(m.primitiveType);
			}
		}

		if (polyStripMeshes.size() > 1) {
			for (int i = 1; i < polyStripMeshes.size(); i++) {
				polyStripMeshes.get(i).name += "_" + (i + 1);
			}
		}

		return l;
	}

	public class VertexConvState {

		public RGBA color = new RGBA();
		public Vec3f normal = new Vec3f();
		public Vec2f uv = new Vec2f();

		public Vertex getVertex(Vec3f position) {
			Vertex v = new Vertex();
			v.position = position;

			if (getCurrentMesh().hasColor) {
				v.color = new RGBA(color);
			}
			if (getCurrentMesh().hasNormal) {
				v.normal = new Vec3f(normal);
			}
			if (getCurrentMesh().hasUV(0)) {
				v.uv[0] = new Vec2f(uv);
			}

			return v;
		}
	}
}
