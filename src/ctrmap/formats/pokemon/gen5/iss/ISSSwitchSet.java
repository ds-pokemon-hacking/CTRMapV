
package ctrmap.formats.pokemon.gen5.iss;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISSSwitchSet {
	
	public int bgmId;
	
	public ISSSwitch[] switches = new ISSSwitch[9];
	public int switchFadeInterval;
	
	public List<Integer> zoneIDs = new ArrayList<>();
	
	public ISSSwitchSet(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();
			
			bgmId = in.readInt();
			for (int i = 0; i < 9; i++) {
				switches[i] = new ISSSwitch(in);
			}
			switchFadeInterval = in.readUnsignedShort();
			int zoneCount = in.readUnsignedByte();
			for (int i = 0; i < zoneCount; i++) {
				zoneIDs.add(in.readUnsignedShort());
			}
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(ISSSwitchSet.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static class ISSSwitch {
		public int trackMask;
		
		public ISSSwitch(DataInput in) throws IOException {
			trackMask = in.readUnsignedShort();
		}
	}
}
