
package ctrmap.formats.pokemon.gen5.rail;

import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.formats.ntr.common.FXIO;

public class RailInfo {
	public float tileDim;
	
	public RailInfo(byte[] bytes){
		try {
			DataInStream dis = new DataInStream(bytes);
			tileDim = FXIO.readFX32(dis);
			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(RailInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public byte[] getBytes(){
		try {
			DataIOStream dos = new DataIOStream();
			FXIO.writeFX32(dos, tileDim);
			dos.close();
			return dos.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(RailInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
