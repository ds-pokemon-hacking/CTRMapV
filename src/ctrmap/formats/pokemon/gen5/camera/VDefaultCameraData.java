package ctrmap.formats.pokemon.gen5.camera;

import xstandard.math.MathEx;
import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import ctrmap.formats.ntr.common.FXIO;

public class VDefaultCameraData {

	public static final int BYTES = 0x2C;

	public boolean enablePlayerTarget;

	public int tzDist;
	public Vec3f rotation = new Vec3f();
	public Vec3f targetTranslation = new Vec3f();

	public int field_10;
	public boolean useNPCDepth;

	public float FOV;
	public float zNear;
	public float zFar;

	public VDefaultCameraData(DataInput in) throws IOException {
		tzDist = in.readInt();
		rotation.x = readAngle(in);
		rotation.y = readAngle(in);
		rotation.z = readAngle(in);
		field_10 = in.readUnsignedByte();
		useNPCDepth = in.readBoolean();
		FOV = MathEx.toDegreesf(FXIO.readFX16(in));
		zNear = FXIO.readFX32(in);
		zFar = FXIO.readFX32(in);
		enablePlayerTarget = in.readInt() == 1;
		targetTranslation = FXIO.readVecFX32(in);
	}

	public void write(DataOutput out) throws IOException {
		out.writeInt(tzDist);
		writeAngle(rotation.x, out);
		writeAngle(rotation.y, out);
		writeAngle(rotation.z, out);
		out.write(field_10);
		out.writeBoolean(useNPCDepth);
		FXIO.writeFX16(out, MathEx.toRadiansf(FOV));
		FXIO.writeFX32(out, zNear);
		FXIO.writeFX32(out, zFar);
		out.writeInt(enablePlayerTarget ? 1 : 0);
		FXIO.writeVecFX32(out, targetTranslation);
	}

	private static final float FX_ANGLE_FULLRES = 180f / (8f * 4096f);
	private static final float FX_ANGLE_FULLRES_INV = 1f / FX_ANGLE_FULLRES;

	public static float readAngle(DataInput in) throws IOException {
		return in.readInt() * FX_ANGLE_FULLRES;
	}

	public static void writeAngle(float angle, DataOutput out) throws IOException {
		out.writeInt((int) (angle * FX_ANGLE_FULLRES_INV));
	}
}
