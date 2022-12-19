package ctrmap.formats.pokemon.gen5.buildings;

import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import ctrmap.formats.ntr.common.FXIO;

/**
 *
 */
public class ChunkBuilding {

	public Vec3f position = new Vec3f();

	public float rotation;

	public int modelUID;

	public ChunkBuilding(DataInput in) throws IOException {
		position = FXIO.readVecFX32(in);

		rotation = FXIO.readAngleDeg16Unsigned(in);

		modelUID = Short.reverseBytes(in.readShort()) & 0xFFFF;
	}

	public ChunkBuilding() {

	}
	
	public ChunkBuilding(ChunkBuilding source){
		this.position = new Vec3f(source.position);
		rotation = source.rotation;
		modelUID = source.modelUID;
	}
	
	public void adjustPosToChunkPosAbs(Vec3f chunkPosAbs){
		position.x -= chunkPosAbs.x;
		position.z -= chunkPosAbs.z;
		position.z *= -1;
	}
	
	public void write(DataOutput out) throws IOException {
		FXIO.writeVecFX32(out, position);
		FXIO.writeAngleDeg16(out, rotation);
		out.writeShort(Short.reverseBytes((short)modelUID));
	}
}
