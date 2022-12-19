package ctrmap.formats.pokemon.gen5.zone.extra;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZoneMapEffSkillIndex {

	private FSFile source;

	private Map<Integer, ZoneMapEffSkillFlag> zoneMapEffSkillMap = new HashMap<>();

	public ZoneMapEffSkillIndex(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();

			ZoneMapEffSkillFlag[] values = ZoneMapEffSkillFlag.values();

			int len = in.getLength() / 4;
			for (int i = 0; i < len; i++) {
				int val = in.readUnsignedShort();
				if (val >= 0 && val < values.length) {
					zoneMapEffSkillMap.put(in.readUnsignedShort(), values[val]);
				}
			}

			in.close();
			source = fsf;
		} catch (IOException ex) {
			Logger.getLogger(ZoneMapEffSkillIndex.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void write() {
		if (source != null) {
			try {
				DataOutStream out = source.getDataOutputStream();

				List<Map.Entry<Integer, ZoneMapEffSkillFlag>> list = new ArrayList<>(zoneMapEffSkillMap.entrySet());
				list.sort(Map.Entry.comparingByKey());

				for (Map.Entry<Integer, ZoneMapEffSkillFlag> e : list) {
					out.writeShort(e.getKey());
					out.writeShort(e.getValue().ordinal());
				}

				out.close();
			} catch (IOException ex) {
				Logger.getLogger(ZoneMapEffSkillIndex.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public boolean setMapEffSkillForZone(int zone, ZoneMapEffSkillFlag newVal) {
		ZoneMapEffSkillFlag now = getMapEffSkillForZone(zone);
		if (newVal != now) {
			if (newVal == ZoneMapEffSkillFlag.NONE) {
				zoneMapEffSkillMap.remove(zone);
			} else {
				zoneMapEffSkillMap.put(zone, newVal);
			}
			return true;
		}
		return false;
	}

	public ZoneMapEffSkillFlag getMapEffSkillForZone(int zone) {
		return zoneMapEffSkillMap.getOrDefault(zone, ZoneMapEffSkillFlag.NONE);
	}

	public static enum ZoneMapEffSkillFlag {
		//Technically these are bitflags, but this is a bit more convenient
		NONE,
		FLASH_READY,
		FLASH_USED
	}
}
