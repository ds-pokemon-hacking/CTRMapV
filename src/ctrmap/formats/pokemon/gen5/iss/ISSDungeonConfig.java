
package ctrmap.formats.pokemon.gen5.iss;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISSDungeonConfig {
	
	public int zoneId;
	
	public ISSSeasonMap bitMask;
	public ISSSeasonMap pitch;
	
	public int unused_14;
	public int unused_18;
	
	public ISSDungeonConfig(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();
			
			zoneId = in.readUnsignedShort();
			in.readShort();
			bitMask = new ISSSeasonMap(in, false);
			pitch = new ISSSeasonMap(in, true);
			unused_14 = in.readInt();
			unused_18 = in.readInt();
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(ISSDungeonConfig.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static class ISSSeasonMap {
		public int valueSpring;
		public int valueSummer;
		public int valueAutumn;
		public int valueWinter;
		
		public ISSSeasonMap(DataInput in, boolean signed) throws IOException {
			valueSpring = readValue(in, signed);
			valueSummer = readValue(in, signed);
			valueAutumn = readValue(in, signed);
			valueWinter = readValue(in, signed);
		}
		
		private int readValue(DataInput in, boolean signed) throws IOException {
			return signed ? in.readShort() : in.readUnsignedShort();
		}
	}
}
