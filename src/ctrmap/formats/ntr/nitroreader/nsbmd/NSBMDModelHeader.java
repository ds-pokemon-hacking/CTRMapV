package ctrmap.formats.ntr.nitroreader.nsbmd;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import xstandard.math.vec.Vec3f;
import java.io.IOException;

public class NSBMDModelHeader {

	public int unknown0;
	public int unknown1;
	public int unknown2;

	public int jointCount;
	public int materialCount;
	public int meshCount;

	public int firstUnusedMtxStackID;

	public float posScale;
	public float invPosScale;

	public int vtxCount;
	public int polyCount;
	public int triCount;
	public int quadCount;

	public Vec3f bboxMin;
	public Vec3f bboxDim;
	
	public float boxPosScale;
	public float boxInvPosScale;

	public NSBMDModelHeader(NTRDataIOStream data) throws IOException { //sizeof 44 (0x2C)
		unknown0 = data.read();
		unknown1 = data.read();
		unknown2 = data.read();
		jointCount = data.read();
		materialCount = data.read();
		meshCount = data.read();
		firstUnusedMtxStackID = data.read();
		data.skipBytes(1);
		posScale = ((float) data.readInt()) / 4096.0f;
		invPosScale = ((float) data.readInt()) / 4096.0f;
		vtxCount = data.readUnsignedShort();
		polyCount = data.readUnsignedShort();
		triCount = data.readUnsignedShort();
		quadCount = data.readUnsignedShort();
		bboxMin = data.readVecFX16();
		bboxDim = data.readVecFX16();
		boxPosScale = data.readFX32();
		boxInvPosScale = data.readFX32();
	}
}
