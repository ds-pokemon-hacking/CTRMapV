package ctrmap.formats.pokemon.gen5.rail;

import ctrmap.formats.pokemon.containers.DefaultGamefreakContainer;
import ctrmap.formats.pokemon.containers.GFContainer;
import xstandard.fs.FSFile;
import xstandard.gui.DialogUtils;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.util.ReflectionHash;
import xstandard.util.ReflectionHashIgnore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 */
public class RailData {

	public final ReflectionHash hash;

	@ReflectionHashIgnore
	private GFContainer source;

	public RailInfo info;
	public List<RailPoint> points = new ArrayList<>();
	public List<RailLine> lines = new ArrayList<>();
	public List<RailCamera> cameras = new ArrayList<>();
	public List<RailCurve> curves = new ArrayList<>();

	public RailTilemaps tilemaps;

	public RailData(FSFile rails, FSFile tilemapFile) {
		try {
			source = new DefaultGamefreakContainer(rails, "RL");

			if (source.getFileCount() != 5) {
				throw new UnsupportedOperationException("Rail data should have exactly 5 sections.");
			}

			info = new RailInfo(source.getFile(0));

			readEntries(points, 1, RailPoint.BYTES, (in) -> new RailPoint(in, this));
			readEntries(lines, 2, RailLine.BYTES, (in) -> new RailLine(in, this));
			readEntries(cameras, 3, RailCamera.BYTES, (in) -> new RailCamera(in, this));
			readEntries(curves, 4, RailCurve.BYTES, (in) -> new RailCurve(in, this));
		} catch (IOException ex) {
			Logger.getLogger(RailData.class.getName()).log(Level.SEVERE, null, ex);
		}

		tilemaps = new RailTilemaps(tilemapFile);

		hash = new ReflectionHash(this);
	}

	public void write() {
		source.makeMemoryHandle();
		source.storeFile(0, info.getBytes());
		source.storeFile(1, writeEntries(points));
		source.storeFile(2, writeEntries(lines));
		source.storeFile(3, writeEntries(cameras));
		source.storeFile(4, writeEntries(curves));
		source.flushMemoryHandle();
		source.deleteMemoryHandle();
		tilemaps.store();
	}

	private byte[] writeEntries(List<? extends RailEntry> list) {
		try {
			DataIOStream out = new DataIOStream();

			for (RailEntry e : list) {
				e.write(out);
			}

			out.close();
			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(RailData.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	private <T extends RailEntry> void readEntries(List<T> list, int fileNo, int entrySize, ReadEntryCallback callback) throws IOException {
		DataIOStream io = new DataIOStream(source.getFile(fileNo));
		int count = io.getLength() / entrySize;
		for (int i = 0; i < count; i++) {
			list.add((T) callback.instantiate(io));
		}
		io.close();
	}

	private static interface ReadEntryCallback {

		public RailEntry instantiate(DataInputEx in) throws IOException;
	}
}
