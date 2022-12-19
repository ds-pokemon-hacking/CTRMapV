package ctrmap.formats.ntr.nitrowriter.nsbmd.dl;

import ctrmap.formats.ntr.common.gfx.commands.GEDisplayList;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.nitrowriter.common.resources.PatriciaTreeNode;
import ctrmap.formats.ntr.nitrowriter.nsbmd.mat.NitroMaterialResource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NitroMeshResource extends PatriciaTreeNode {

	public static final int HEADER_BYTES = 0x10;

	public NitroMaterialResource material;
	public int visGroup;

	public Map<JointBinding, Integer> jointMtxBindings = new HashMap<>();
	
	public final int vertexCount;
	public final int faceCount;
	public final int triCount;
	public final int quadCount;
	
	public GEDisplayList displayList;

	public NitroMeshResource(DLSubMeshFactory factory, DLSubMesh subMesh, String name) {
		this.name = name;
		jointMtxBindings = subMesh.bindings;
		//System.out.println("dl for sm " + name);
		displayList = subMesh.createDisplayList();
		material = factory.getMaterial();
		
		List<SeparablePrimitive> primitives = subMesh.getAllPrimitives();
		visGroup = primitives.get(0).visGroup; 
		//technically this could result in some visgroups not being covered,
		//but as far as the DS goes this is the best we can get
		
		int vcount = 0;
		int fcount = 0;
		int tcount = 0;
		int qcount = 0;
		for (SeparablePrimitive p : primitives){
			vcount += p.vertices.length;
			int pfcount = p.getFaceCount();
			switch (p.type){
				case QUADS:
				case QUADSTRIPS:
					qcount += pfcount;
					break;
				case TRIS:
				case TRISTRIPS:
					tcount += pfcount;
					break;
			}
			fcount += pfcount;
		}
		
		vertexCount = vcount;
		faceCount = fcount;
		triCount = tcount;
		quadCount = qcount;
	}

	public boolean isSingleJointRgdSk() {
		if (jointMtxBindings.size() == 1) {
			for (JointBinding jb : jointMtxBindings.keySet()) {
				return jb.isRgdSk();
			}
		}
		return false;
	}

	public int getSingleJointRgdSkJntNo() {
		for (JointBinding jb : jointMtxBindings.keySet()) {
			return jb.jointIds[0];
		}
		return 0;
	}

	public int getDLFlags() {
		int flags = 0;

		for (GECommand cmd : displayList) {
			switch (cmd.getOpCode()) {
				case NORMAL:
					flags |= 1;
					break;
				case COLOR:
					flags |= 2;
					break;
				case TEXCOORD:
					flags |= 4;
					break;
				case MTX_RESTORE:
					flags |= 8;
					break;
			}
			if (flags > 12) {
				// 13 = 8/4/1
				// 14 = 8/4/2
				// normal and color can't be together
				// ~~ just like me and [REDACTED] ~~
				break;
			}
		}

		return flags;
	}
}
