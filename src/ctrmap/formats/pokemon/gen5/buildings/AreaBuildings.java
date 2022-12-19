package ctrmap.formats.pokemon.gen5.buildings;

import ctrmap.formats.pokemon.containers.GFContainer;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMD;
import xstandard.util.ListenableList;
import java.util.HashMap;
import java.util.Map;

public class AreaBuildings {

	private GFContainer source;

	public ListenableList<AreaBuildingResource> buildings = new ListenableList<>();
	
	private Map<AreaBuildingResource, byte[]> modelQueue = new HashMap<>();
	
	public AreaBuildings(GFContainer ab) {
		source = ab;

		ab.makeMemoryHandle();

		int bldCount = ab.getFileCount() / 2;

		for (int i = 0; i < bldCount; i++) {
			//System.out.println("read file " + i);
			buildings.add(new AreaBuildingResource(ab.getFile(i)));
		}
		for (int i = bldCount, j = 0; i < bldCount * 2; i++, j++) {
			//System.out.println("read file " + i);
			buildings.get(j).convertNitroResources(new NSBMD(ab.getFile(i)).toGeneric());
		}

		ab.deleteMemoryHandle();
	}
	
	public void queueModel(AreaBuildingResource rsc, byte[] mdl) {
		modelQueue.put(rsc, mdl);
	}
	
	private byte[] getMqForIdx(int idx) {
		if (idx < buildings.size() && idx >= 0) {
			byte[] data = modelQueue.get(buildings.get(idx));
			if (data != null) {
				return data;
			}
		}
		return new byte[0];
	}
	
	private boolean hasMqForIdx(int idx) {
		if (idx < buildings.size() && idx >= 0) {
			return modelQueue.containsKey(buildings.get(idx));
		}
		return false;
	}

	public void write() {
		int bmCount = buildings.size();

		source.makeMemoryHandle();
		int contBMCount = source.getFileCount() >> 1;

		if (bmCount != contBMCount) {
			//resize
			byte[][] transferMdlData = new byte[contBMCount][];

			for (int i = 0; i < contBMCount; i++) {
				transferMdlData[i] = hasMqForIdx(i) ? getMqForIdx(i) : source.getFile(i + contBMCount);
			}

			for (int i = 0; i < bmCount; i++) {
				int datId = i + bmCount;
				if (i < transferMdlData.length) {
					source.storeFile(datId, transferMdlData[i]);
				}
				else {
					source.storeFile(datId, getMqForIdx(i));
				}
			}
		}
		if (bmCount < contBMCount) {
			source.setFileCount(bmCount << 1);
		}
		
		for (Map.Entry<AreaBuildingResource, byte[]> mq : modelQueue.entrySet()) {
			int idx = buildings.indexOf(mq.getKey());
			if (idx != -1) {
				source.storeFile(bmCount + idx, mq.getValue());
			}
		}
		
		modelQueue.clear();
		
		for (int i = 0; i < buildings.size(); i++) {
			source.storeFile(i, buildings.get(i).getBytes());
		}

		source.flushMemoryHandle();
		source.deleteMemoryHandle();
	}

	public void merge(AreaBuildings other) {
		for (AreaBuildingResource rsc : other.buildings) {
			if (getResourceByUniqueID(rsc.uid) == null) {
				buildings.add(rsc);
			}
		}
	}

	public AreaBuildingResource getResourceByUniqueID(int uid) {
		for (AreaBuildingResource res : buildings) {
			if (res.uid == uid) {
				return res;
			}
		}
		return null;
	}
}
