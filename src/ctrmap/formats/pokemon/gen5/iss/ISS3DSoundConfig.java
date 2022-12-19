
package ctrmap.formats.pokemon.gen5.iss;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISS3DSoundConfig {
	
	public int bgmId;
	public int trackMask;
	
	public ISS3DSoundConfig(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();
			
			bgmId = in.readInt();
			trackMask = in.readUnsignedShort();
			in.readUnsignedShort(); //garbo data
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(ISS3DSoundConfig.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
