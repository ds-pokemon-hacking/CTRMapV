
package ctrmap.formats.pokemon.gen5.iss;

import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISSBGMInfo {
	
	public final List<ISSBGMEntry> entries = new ArrayList<>();
	
	public ISSBGMInfo(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();
			
			int entryCount = in.read();
			for (int i = 0; i < entryCount; i++) {
				entries.add(new ISSBGMEntry(in));
			}
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(ISSBGMInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static class ISSBGMEntry {
		public int bgmId;
		public ISSSubsystemType issType;
		
		public ISSBGMEntry(DataInput in) throws IOException {
			bgmId = in.readUnsignedShort();
			issType = ISSSubsystemType.values()[in.readUnsignedByte()];
		}
	}
	
	public static enum ISSSubsystemType {
		NULL,
		DUMMY1,
		ROAD,
		CITY,
		SOUND3D,
		DUNGEON,
		DUMMY6,
		SWITCH,
		ZONE
	}
}
