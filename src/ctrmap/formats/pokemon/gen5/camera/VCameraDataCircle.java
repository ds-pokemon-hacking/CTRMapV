package ctrmap.formats.pokemon.gen5.camera;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.pokemon.WorldObject;
import xstandard.math.vec.Vec3f;

public class VCameraDataCircle extends VAbstractCameraData implements WorldObject {

	public float angleStart;
	public float angleEnd;
	
	public float radiusStart;
	public float radiusEnd;
	
	//Aimed
	public Vec3f centre;

	public float pitch;
	public float tzDist;
	
	//Fixed
	public Vec3f camTgt;
	public Vec3f camPos;
	
	public VCameraDataCircle() {
		areaType = VCameraAreaType.CIRCLE;
	}

	public VCameraDataCircle(DataInput in) throws IOException {
		angleStart = FXIO.readAngleDeg32(in);
		angleEnd = FXIO.readAngleDeg32(in);
		
		radiusStart = FXIO.readFX32(in);
		radiusEnd = FXIO.readFX32(in);
		
		centre = FXIO.readVecFX32(in);
		
		pitch = FXIO.readAngleDeg32(in);
		tzDist = FXIO.readFX32(in);
		
		camTgt = FXIO.readVecFX32(in);
		camPos = FXIO.readVecFX32(in);

		readCommon(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		FXIO.writeAngleDeg32(out, angleStart);
		FXIO.writeAngleDeg32(out, angleEnd);
		
		FXIO.writeFX32(out, radiusStart);
		FXIO.writeFX32(out, radiusEnd);
		
		FXIO.writeVecFX32(out, centre);
		FXIO.writeAngleDeg32(out, pitch);
		FXIO.writeFX32(out, tzDist);
		
		FXIO.writeVecFX32(out, camTgt);
		FXIO.writeVecFX32(out, camPos);
		
		writeCommon(out);
	}

	@Override
	public Vec3f getWPos() {
		System.out.println("wpos " + centre + " dim " + getWDim() + " min " + getMinVector());
		return centre;
	}

	@Override
	public void setWPos(Vec3f vec) {
		centre.set(vec);
	}

	@Override
	public Vec3f getWDim() {
		return new Vec3f(radiusEnd * 2f, 32f, radiusEnd * 2f);
	}

	@Override
	public Vec3f getMinVector() {
		return new Vec3f(-radiusEnd, 0f, -radiusEnd);
	}
}
