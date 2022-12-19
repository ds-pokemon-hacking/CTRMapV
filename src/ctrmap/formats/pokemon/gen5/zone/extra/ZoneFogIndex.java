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

public class ZoneFogIndex {

	private FSFile source;

	private Map<Integer, Integer> zoneFogMap = new HashMap<>();

	public ZoneFogIndex(FSFile fsf) {
		try (DataInStream in = fsf.getDataInputStream()) {
			int len = in.getLength() >> 2;
			for (int i = 0; i < len; i++) {
				zoneFogMap.put(in.readUnsignedShort(), in.readUnsignedShort());
			}

			source = fsf;
		} catch (IOException ex) {
			Logger.getLogger(ZoneFogIndex.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void write() {
		if (source != null) {
			try {
				DataOutStream out = source.getDataOutputStream();

				List<Map.Entry<Integer, Integer>> list = new ArrayList<>(zoneFogMap.entrySet());
				list.sort(Map.Entry.comparingByKey());

				for (Map.Entry<Integer, Integer> e : list) {
					out.writeShort(e.getKey());
					out.writeShort(e.getValue());
				}

				out.close();
			} catch (IOException ex) {
				Logger.getLogger(ZoneFogIndex.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public boolean setFogForZone(int zone, int fog) {
		int now = getFogForZone(zone);
		if (fog != now) {
			if (fog == -1) {
				zoneFogMap.remove(zone);
			} else {
				zoneFogMap.put(zone, fog);
			}
			return true;
		}
		return false;
	}

	public int getFogForZone(int zone) {
		return zoneFogMap.getOrDefault(zone, -1);
	}
}
