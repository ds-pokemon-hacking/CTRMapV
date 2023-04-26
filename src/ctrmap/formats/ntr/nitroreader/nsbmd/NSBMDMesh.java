package ctrmap.formats.ntr.nitroreader.nsbmd;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEDisplayList;
import ctrmap.renderer.scene.model.Mesh;
import java.io.IOException;
import xstandard.math.BitMath;

public class NSBMDMesh {

	public final boolean hasNormal;
	public final boolean hasVCol;
	public final boolean hasUV;

	public String name;
	GEDisplayList dl;

	public NSBMDMesh(NTRDataIOStream data, String name) throws IOException {
		data.setBaseHere();
		this.name = name;
		int unknown = data.readUnsignedShort();
		int size = data.readUnsignedShort();

		int flags = data.readInt();
		hasNormal = BitMath.checkIntegerBit(flags, 0);
		hasVCol = BitMath.checkIntegerBit(flags, 1);
		hasUV = BitMath.checkIntegerBit(flags, 2);

		int dlOffset = data.readInt();
		int dlSize = data.readInt();

		data.seek(dlOffset);
		dl = new GEDisplayList(data, dlSize);

		data.resetBase();
	}

	public void setMeshProperty(Mesh mesh) {
		mesh.name = name;
		mesh.hasColor = hasVCol & !hasNormal; 
		//some meshes have both colors and normals
		//it's unknown how that actually came to be, but it's not supported on DS hardware,
		//so enabling it often causes bugs
		mesh.hasNormal = hasNormal;
		mesh.hasUV[0] = hasUV;
	}
}
