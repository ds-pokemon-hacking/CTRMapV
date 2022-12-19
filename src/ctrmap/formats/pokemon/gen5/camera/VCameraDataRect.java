package ctrmap.formats.pokemon.gen5.camera;

import ctrmap.formats.pokemon.gen5.zone.entities.VGridObject;
import xstandard.math.MathEx;
import java.awt.Dimension;
import java.awt.Point;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import ctrmap.formats.ntr.common.FXIO;

public class VCameraDataRect extends VAbstractCameraData implements VGridObject {

	public int gridX;
	public int gridZ;
	
	public boolean horizontal;
	
	public int gridW;
	public int gridH;
	
	public int unk2;

	public VCameraCoordinates coords1 = new VCameraCoordinates();
	public VCameraCoordinates coords2 = new VCameraCoordinates();

	public boolean disableDelayManager; //value 0 or 1, 1 on cams with disabled FOV/target

	public VCameraDataRect() {
		gridW = 4;
		gridH = 4;
		horizontal = false;
		stayCalcFunc = 5;
		enterCalcFunc = 6;
		exitCalcFunc = 7;
		areaType = VCameraAreaType.RECTANGLE;
	}

	public VCameraDataRect(DataInput in) throws IOException {
		gridX = in.readUnsignedShort();
		gridZ = in.readUnsignedShort();
		int test = in.readUnsignedShort();
		horizontal = test == 1;
		
		if (horizontal) {
			gridH = in.readUnsignedShort();
			gridW = in.readUnsignedShort();
		}
		else {
			gridW = test;
			in.readShort();
			gridH = in.readUnsignedShort();
		}
		
		unk2 = in.readUnsignedShort();

		coords1.pitch = FXIO.readAngleDeg16Unsigned(in);
		coords1.yaw = FXIO.readAngleDeg16Unsigned(in);
		coords1.tz = FXIO.readFX32(in);

		coords2.pitch = FXIO.readAngleDeg16Unsigned(in);
		coords2.yaw = FXIO.readAngleDeg16Unsigned(in);
		coords2.tz = FXIO.readFX32(in);

		disableDelayManager = in.readInt() == 1;

		coords1.targetOffset = FXIO.readVecFX32(in);
		coords2.targetOffset = FXIO.readVecFX32(in);

		coords1.FOV = MathEx.toDegreesf(FXIO.readFX16(in));
		coords2.FOV = MathEx.toDegreesf(FXIO.readFX16(in));

		readCommon(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeShort(gridX);
		out.writeShort(gridZ);
		
		if (horizontal) {
			out.writeShort(1);
			out.writeShort(gridH);
			out.writeShort(gridW);
		}
		else {
			out.writeShort(gridW);
			out.writeShort(0);
			out.writeShort(gridH);
		}
		
		out.writeShort(unk2);
		
		FXIO.writeAngleDeg16(out, coords1.pitch);
		FXIO.writeAngleDeg16(out, coords1.yaw);
		FXIO.writeFX32(out, coords1.tz);
		
		FXIO.writeAngleDeg16(out, coords2.pitch);
		FXIO.writeAngleDeg16(out, coords2.yaw);
		FXIO.writeFX32(out, coords2.tz);
		
		out.writeInt(disableDelayManager ? 1 : 0);
		
		FXIO.writeVecFX32(out, coords1.targetOffset);
		FXIO.writeVecFX32(out, coords2.targetOffset);
		
		FXIO.writeFX16Round(out, MathEx.toRadiansf(coords1.FOV));
		FXIO.writeFX16Round(out, MathEx.toRadiansf(coords2.FOV));
		
		writeCommon(out);
	}

	@Override
	public Point getGPos() {
		return new Point(gridX, gridZ);
	}

	@Override
	public void setGPos(Point p) {
		gridX = p.x;
		gridZ = p.y;
	}

	@Override
	public boolean getIsDimensionsCentered() {
		return false;
	}

	@Override
	public Dimension getGDimensions() {
		return new Dimension(gridW, gridH);
	}

	@Override
	public void setGDimensions(Dimension dim) {
		gridW = dim.width;
		gridH = dim.height;
	}

	@Override
	public float getDimHeight() {
		return 50f;
	}

	@Override
	public float getAltitude() {
		return 0f;
	}
}
