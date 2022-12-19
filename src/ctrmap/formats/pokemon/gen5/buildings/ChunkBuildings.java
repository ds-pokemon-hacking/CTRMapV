
package ctrmap.formats.pokemon.gen5.buildings;

import ctrmap.formats.pokemon.containers.GFContainer;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.missioncontrol_ntr.field.structs.VMap;

/**
 *
 */
public class ChunkBuildings {
	public List<ChunkBuilding> buildings = new ArrayList<>();
	
	public ChunkBuildings(byte[] bytes){
		try {
			DataInStream dis = new DataInStream(bytes);
			
			int count = dis.readInt();
			for (int i = 0; i < count; i++){
				buildings.add(new ChunkBuilding(dis));
			}
			
			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(ChunkBuildings.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public ChunkBuildings(){
		
	}
	
	public void write(VMap map){
		Map<Vec3f, GFContainer> chunkContainers = new HashMap<>();
		Map<GFContainer, ChunkBuildings> buildingMap = new HashMap<>();
		
		for (int x = 0; x < map.chunks.getWidth(); x++){
			for (int y = 0; y < map.chunks.getHeight(); y++){
				GFContainer chk = map.chunks.get(x, y);
				if (chk != null){
					chunkContainers.put(new Vec3f((x + 0.5f) * map.chunkSpan, 0f, (y + 0.5f) * map.chunkSpan), chk);
					buildingMap.put(chk, new ChunkBuildings());
				}
			}
		}
		
		for (ChunkBuilding b : buildings){
			float minDist = Float.MAX_VALUE;
			Vec3f minDistChunkPos = null;
			for (Map.Entry<Vec3f, GFContainer> chk : chunkContainers.entrySet()){
				Vec3f chkPos = chk.getKey();
				float dist = chkPos.dist(b.position);
				if (dist < minDist){
					minDist = dist;
					minDistChunkPos = chkPos;
				}
			}
			if (minDistChunkPos != null){
				ChunkBuilding b2 = new ChunkBuilding(b);
				b2.adjustPosToChunkPosAbs(minDistChunkPos);
				buildingMap.get(chunkContainers.get(minDistChunkPos)).buildings.add(b2);
			}
		}
		
		for (Map.Entry<GFContainer, ChunkBuildings> bie : buildingMap.entrySet()){
			bie.getKey().storeFile(bie.getKey().getFileCount() - 1, bie.getValue().getBytes());
		}
	}
	
	public byte[] getBytes(){
		try {
			DataIOStream out = new DataIOStream();
			
			out.writeInt(buildings.size());
			for (ChunkBuilding bld : buildings){
				bld.write(out);
			}
			
			out.close();
			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(ChunkBuildings.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
