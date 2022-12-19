
package ctrmap.formats.pokemon.gen5.rail;

import xstandard.io.base.iface.DataOutputEx;
import java.io.DataInput;
import java.io.IOException;
import ctrmap.formats.ntr.common.FXIO;

public class RailCamera extends RailEntry {
	public static final int BYTES = 0x24;
	
	public int curveFunc;
	
	public float pitch;
	public float yaw;
	public float distanceFromPlayer;
	public float yawShift;
	public float pitchShift;
	public float roll;
	
	public RailCamera(DataInput in, RailData rails) throws IOException {
		super(rails);
		curveFunc = in.readInt();
		pitch = FXIO.readFX32(in);
		yaw = FXIO.readFX32(in);
		distanceFromPlayer = FXIO.readFX32(in);
		yawShift = FXIO.readFX32(in);
		pitchShift = FXIO.readFX32(in);
		roll = FXIO.readFX32(in);
		in.skipBytes(8);
	}
	
	@Override
	public void write(DataOutputEx out) throws IOException {
		out.writeInt(curveFunc);
		FXIO.writeFX32(out, pitch, yaw, distanceFromPlayer, yawShift, pitchShift, roll);
		out.writeLong(0);
	}
}
