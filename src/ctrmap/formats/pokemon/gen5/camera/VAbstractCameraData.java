package ctrmap.formats.pokemon.gen5.camera;

import ctrmap.formats.pokemon.WorldObject;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class VAbstractCameraData implements WorldObject {

	public static final int BYTES = 0x48;

	public int unk4; //always 0

	protected VCameraAreaType areaType;

	public int stayCalcFunc; //2,3,4 for PYTz only, 4,5,6 for all transforms
	public int enterCalcFunc; //they always seem to be 1 higher than the one before, but the increase does not have any real effect afaik
	public int exitCalcFunc;
	
	public VCameraAreaType getType() {
		return areaType;
	}

	protected void readCommon(DataInput in) throws IOException {
		unk4 = in.readInt();

		areaType = VCameraAreaType.VALUES[in.readUnsignedShort()];
		stayCalcFunc = in.readShort();
		enterCalcFunc = in.readShort();
		exitCalcFunc = in.readShort();
	}

	protected void writeCommon(DataOutput out) throws IOException {
		out.writeInt(unk4);
		
		out.writeShort(areaType.ordinal());
		out.writeShort(stayCalcFunc);
		out.writeShort(enterCalcFunc);
		out.writeShort(exitCalcFunc);
	}
	
	public abstract void write(DataOutput out) throws IOException;
}
