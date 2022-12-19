
package ctrmap.formats.pokemon.gen5.iss;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISSZoneConfig {
	
	public int zoneId;
	
	public int trackMaskIn;
	public int trackMaskOut;
	
	public int fadeTime;
	
	public ISSZoneConfig(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();
			
			zoneId = in.readUnsignedShort();
			trackMaskIn = in.readUnsignedShort();
			trackMaskOut = in.readUnsignedShort();
			fadeTime = in.readUnsignedShort();
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(ISSZoneConfig.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
