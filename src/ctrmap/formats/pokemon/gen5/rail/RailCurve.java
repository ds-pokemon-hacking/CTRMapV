
package ctrmap.formats.pokemon.gen5.rail;

import ctrmap.formats.pokemon.WorldObject;
import xstandard.io.base.iface.DataOutputEx;
import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.IOException;
import ctrmap.formats.ntr.common.FXIO;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;

public class RailCurve extends RailEntry implements WorldObject {
	public static final int BYTES = 0x24;
	
	public RailCurveType curveType;
	
	public Vec3f position;
	
	public RailCurve(DataInput in, RailData rails) throws IOException {
		super(rails);
		curveType = RailCurveType.values()[in.readInt()];
		position = FXIO.readVecFX32(in);
		in.skipBytes(20);
	}

	@Override
	public Vec3f getWPos() {
		return position;
	}

	@Override
	public void setWPos(Vec3f vec) {
		position.x = vec.x;
		position.z = vec.z;
	}

	@Override
	public Vec3f getWDim() {
		return new Vec3f(VFieldConstants.TILE_REAL_SIZE);
	}

	@Override
	public Vec3f getMinVector() {
		return getWDim().mul(-0.5f);
	}

	@Override
	public void write(DataOutputEx out) throws IOException {
		out.writeEnum(curveType);
		FXIO.writeVecFX32(out, position);
		out.writeLong(0);
		out.writeLong(0);
		out.writeInt(0);
	}
	
	public static enum RailCurveType {
		LERP,
		SLERP_XZ,
		SLERP_XYZ
	}
}
