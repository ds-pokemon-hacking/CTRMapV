
package ctrmap.formats.pokemon.gen5.zone;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class VZoneTable {
	private FSFile source;
	
	public List<VZoneHeader> headers = new ArrayList<>();
	
	public VZoneTable(FSFile fsf){
		try {
			this.source = fsf;
			
			DataIOStream io = fsf.getDataIOStream();
			
			int count = io.getLength()/ VZoneHeader.BYTES;
			for (int i = 0; i < count; i++){
				headers.add(new VZoneHeader(io));
			}
			
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(VZoneTable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public VZoneHeader getHeader(int zoneID){
		return headers.get(zoneID);
	}
	
	public void saveHeader(int zoneID){
		try {			
			DataIOStream io = source.getDataIOStream();
			
			io.seek(zoneID * VZoneHeader.BYTES);
			io.write(getHeader(zoneID).getBytes());
			
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(VZoneTable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public int getZoneCount(){
		return headers.size();
	}
}
