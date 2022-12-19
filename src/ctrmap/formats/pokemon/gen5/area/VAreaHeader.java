
package ctrmap.formats.pokemon.gen5.area;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class VAreaHeader {
	public static final int BYTES = 10;
	
	public int buildingsId; //short
	public int texturesId; //short
	public int srtAnimeIdx;
	public int patAnimeIdx;
	public boolean isExterior; //byte
	public int lightIndex;
	public int outlineType;
	public int unknown3; //byte
	
	public VAreaHeader(DataInput in) throws IOException{
		buildingsId = in.readUnsignedShort();
		texturesId = in.readUnsignedShort();
		srtAnimeIdx = in.readUnsignedByte();
		patAnimeIdx = in.readUnsignedByte();
		isExterior = in.readBoolean();
		lightIndex = in.readUnsignedByte();
		outlineType = in.readUnsignedByte();
		unknown3 = in.readUnsignedByte();
	}
	
	public void write(DataOutput out) throws IOException {
		out.writeShort(buildingsId);
		out.writeShort(texturesId);
		out.write(srtAnimeIdx);
		out.write(patAnimeIdx);
		out.write(isExterior ? 1 : 0);
		out.write(lightIndex);
		out.write(outlineType);
		out.write(unknown3);
	}
}
