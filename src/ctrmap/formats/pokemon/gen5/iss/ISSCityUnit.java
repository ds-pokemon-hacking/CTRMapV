
package ctrmap.formats.pokemon.gen5.iss;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISSCityUnit {
	
	public int zoneId;
	
	public int originGX;
	public int originGY;
	public int originGZ;
	
	public int[] volumeCurve = new int[6];
	
	public int[] curveKeysX = new int[6];
	public int[] curveKeysY = new int[6];
	public int[] curveKeysZ = new int[6];
	
	public ISSCityUnit(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();
			
			zoneId = in.readUnsignedShort();
			in.readShort();
			originGX = in.readInt();
			originGY = in.readInt();
			originGZ = in.readInt();
			
			readUnsigned6(in, volumeCurve);
			readUnsigned6(in, curveKeysX);
			readUnsigned6(in, curveKeysY);
			readUnsigned6(in, curveKeysZ);
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(ISSCityUnit.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private static void readUnsigned6(DataInput in, int[] dest) throws IOException {
		for (int i = 0; i < 6; i++) {
			dest[i] = in.readUnsignedByte();
		}
	}
}
