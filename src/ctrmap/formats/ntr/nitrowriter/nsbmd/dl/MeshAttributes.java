
package ctrmap.formats.ntr.nitrowriter.nsbmd.dl;

import ctrmap.renderer.scene.model.Mesh;

public class MeshAttributes {
	public boolean hasColor;
	public boolean hasNormal;
	public boolean hasUV;
	
	public MeshAttributes(Mesh mesh){
		hasColor = mesh.hasColor;
		hasNormal = mesh.hasNormal;
		hasUV = mesh.hasUV(0);
	}
}
