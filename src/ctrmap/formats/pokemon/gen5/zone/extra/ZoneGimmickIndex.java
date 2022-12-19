
package ctrmap.formats.pokemon.gen5.zone.extra;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ZoneGimmickIndex {
	
	private FSFile source;
	
	private Map<Integer, Integer> zoneGimmickMap = new LinkedHashMap<>();
	
	public ZoneGimmickIndex(FSFile fsf){
		source = fsf;
		try {
			DataInStream in = fsf.getDataInputStream();
			
			int count = in.getLength() / 8;
			for (int i = 0; i < count; i++){
				zoneGimmickMap.put(in.readInt(), in.readInt());
			}
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(ZoneGimmickIndex.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public int getZoneGimmick(int zoneId){
		return zoneGimmickMap.getOrDefault(zoneId, -1);
	}
	
	public boolean setZoneGimmick(int zoneId, int gimmickId){
		if (gimmickId == -1){
			return zoneGimmickMap.remove(zoneId) != null;
		}
		else {
			return !((Integer)gimmickId).equals(zoneGimmickMap.put(zoneId, gimmickId));
		}
	}
	
	public void write(){
		try {
			DataOutStream out = source.getDataOutputStream();
			
			for (Map.Entry<Integer, Integer> e : zoneGimmickMap.entrySet()){
				out.writeInts(e.getKey(), e.getValue());
			}
			
			out.close();
		} catch (IOException ex) {
			Logger.getLogger(ZoneGimmickIndex.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
