package ctrmap.formats.ntr.nitrowriter.nsbmd.dl;

import ctrmap.renderer.scene.model.Face;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import xstandard.math.vec.Vec3f;

public class SeparablePrimitive {

	public final PrimitiveType type;
	public final Mesh.SkinningType skinningType;
	public final int visGroup;

	public Vertex[] vertices;
	public JointBinding[] jointBindings;

	public Vec3f[] positionsAbs;
	public Vec3f[] normalsAbs;

	public SeparablePrimitive(PrimitiveType type, Mesh.SkinningType skinningType, int visGroup, Face face, int forcedJointId) {
		this.type = type;
		this.skinningType = skinningType;
		this.visGroup = visGroup;
		vertices = face.vertices;

		jointBindings = new JointBinding[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			if (forcedJointId == -1) {
				jointBindings[i] = new JointBinding(vertices[i]);
			} else {
				jointBindings[i] = new JointBinding(forcedJointId);
			}
		}
	}

	public int getFaceCount() {
		switch (type) {
			case QUADS:
			case TRIS:
				return 1;
			case QUADSTRIPS:
				return (vertices.length - 2) / 2;
			case TRISTRIPS:
				return vertices.length - 2;
		}
		return 0;
	}

	public boolean areJointBindingsConsistent() {
		JointBinding ref = jointBindings[0];
		for (int i = 1; i < jointBindings.length; i++) {
			if (!ref.equals(jointBindings[i])) {
				return false;
			}
		}
		return true;
	}
}
