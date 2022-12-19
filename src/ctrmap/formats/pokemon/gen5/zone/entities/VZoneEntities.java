
package ctrmap.formats.pokemon.gen5.zone.entities;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.util.ListenableList;
import xstandard.util.ReflectionHash;
import xstandard.util.ReflectionHashIgnore;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class VZoneEntities {
	
	public static final int VZE_SCROBJ_TYPEID_NPC = 1;
	public static final int VZE_SCROBJ_TYPEID_FURNITURE = 2;
	public static final int VZE_SCROBJ_TYPEID_TRIGGER = 3;
	
	public ReflectionHash monitor;
	
	@ReflectionHashIgnore
	private FSFile source;
	
	public ListenableList<VFurniture> furniture = new ListenableList<>();
	public ListenableList<VNPC> NPCs = new ListenableList<>();
	public ListenableList<VWarp> warps = new ListenableList<>();
	public ListenableList<VTrigger> triggers = new ListenableList<>();
	
	@ReflectionHashIgnore
	public VZoneInitScriptDispatcher initScr;
	
	public VZoneEntities(FSFile f){
		source = f;
		try {
			DataIOStream in = f.getDataIOStream();
			
			int readLen = in.readInt();
			int furnitureCount = in.read();
			int npcCount = in.read();
			int warpCount = in.read();
			int triggerCount = in.read();
			
			for (int i = 0; i < furnitureCount; i++){
				furniture.add(new VFurniture(in));
			}
			for (int i = 0; i < npcCount; i++){
				NPCs.add(new VNPC(in));
			}
			for (int i = 0; i < warpCount; i++){
				warps.add(new VWarp(in));
			}
			for (int i = 0; i < triggerCount; i++){
				triggers.add(new VTrigger(in));
			}
			initScr = new VZoneInitScriptDispatcher(in);
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(VZoneEntities.class.getName()).log(Level.SEVERE, null, ex);
		}
		monitor = new ReflectionHash(this);
	}
	
	public void write(){
		if (hasChanged()){
			monitor.resetChangedFlag();
			try (DataIOStream dos = source.getDataIOStream()){
				dos.setLength(0);
				int len = 4 + furniture.size() * VFurniture.BYTES + NPCs.size() * VNPC.BYTES + warps.size() * VWarp.BYTES + triggers.size() * VTrigger.BYTES;
				dos.writeInt(len);
				dos.write(furniture.size());
				dos.write(NPCs.size());
				dos.write(warps.size());
				dos.write(triggers.size());
				
				for (VFurniture f : furniture){
					f.write(dos);
				}
				for (VNPC n : NPCs){
					n.write(dos);
				}
				for (VWarp w : warps){
					w.write(dos);
				}
				for (VTrigger t : triggers){
					t.write(dos);
				}
				initScr.write(dos);
			} catch (IOException ex) {
				Logger.getLogger(VZoneEntities.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	public boolean hasChanged(){
		return monitor.getChangeFlagRecalcIfNeeded();
	}
}
