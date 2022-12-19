package ctrmap.formats.pokemon.gen5.area;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.formats.pokemon.gen5.zone.VZoneTable;

/**
 *
 */
public class AreaTable {

	private FSFile source;

	public List<VAreaHeader> headers = new ArrayList<>();

	public AreaTable(FSFile fsf) {
		try {
			this.source = fsf;

			DataIOStream io = fsf.getDataIOStream();

			int count = io.getLength() / VAreaHeader.BYTES;
			for (int i = 0; i < count; i++) {
				headers.add(new VAreaHeader(io));
			}
			
			int max = 0;
			for (VAreaHeader ah : headers){
				max = Math.max(ah.texturesId, max);
			}

			if (io.getPosition() != io.getLength()) {
				System.err.println("Area Data file size is over !!");
			}

			io.close();
		} catch (IOException ex) {
			Logger.getLogger(VZoneTable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void saveHeader(int idx) {
		if (source != null && idx < headers.size() && idx >= 0) {
			try {
				DataIOStream io = source.getDataIOStream();
				io.seek(idx * VAreaHeader.BYTES);
				headers.get(idx).write(io);
				io.close();
			} catch (IOException ex) {
				Logger.getLogger(AreaTable.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public VAreaHeader getHeader(int areaID) {
		if (areaID < 0 || areaID >= headers.size()) {
			return null;
		}
		return headers.get(areaID);
	}

	public int getAreaCount() {
		return headers.size();
	}
}
