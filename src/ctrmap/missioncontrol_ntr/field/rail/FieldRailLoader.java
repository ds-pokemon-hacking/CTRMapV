package ctrmap.missioncontrol_ntr.field.rail;

import xstandard.fs.FSFile;
import ctrmap.formats.common.GameInfo;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.formats.pokemon.gen5.zone.VZoneTable;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;

public class FieldRailLoader {

	private NTRGameFS fs;

	private List<RailInfo> railRegistry = new ArrayList<>();
	
	public final boolean EX_IS_LOADED_FROM_INJECTED;

	public FieldRailLoader(VZoneTable zones, NTRGameFS fs, GameInfo game) {
		this.fs = fs;
		FSFile railIndex = fs.getDataFile("rail_data_index.tbl");
		EX_IS_LOADED_FROM_INJECTED = railIndex.exists();
		try {
			if (!EX_IS_LOADED_FROM_INJECTED) {
				DataIOStream io = fs.getDecompressedArm9BinMaybeRO().getDataIOStream();

				io.setBase(fs.getARM9LoadAddr());

				if (game.getSubGame() == GameInfo.SubGame.W2) {
					io.seek(0x2018A72);
					int count = io.read();

					io.seekAndSeek(0x2018A7C);

					for (int i = 0; i < count; i++) {
						railRegistry.add(new RailInfo(io));
					}
				}

				io.close();

				DataIOStream railIndexOut = railIndex.getDataIOStream();

				for (int i = 0; i < zones.getZoneCount(); i++) {
					railIndexOut.writeShort(getRailIndexForZone(i));
				}

				railIndexOut.close();
			} else {
				DataIOStream io = railIndex.getDataIOStream();

				int count = io.getLength() / Short.BYTES;

				for (int i = 0; i < count; i++) {
					short value = io.readShort();
					if (value != -1) {
						RailInfo ri = new RailInfo(i, value);
						railRegistry.add(ri);
					}
				}

				io.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(FieldRailLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public final int getRailIndexForZone(int zoneId) {
		for (RailInfo ri : railRegistry) {
			if (ri.zone == zoneId) {
				return ri.rail;
			}
		}
		return -1;
	}

	public RailHeader loadRailData(int zoneId) {
		int idx = getRailIndexForZone(zoneId);

		if (idx != -1) {
			RailHeader railHeader = new RailHeader(fs.NARCGet(NARCRef.FIELD_ZONE_NOGRID_HEADERS, idx));
			return railHeader;
		}
		return null;
	}

	public static class RailInfo {

		public int zone;
		public int rail;

		public RailInfo(DataInput in) throws IOException {
			int value = in.readUnsignedShort();
			zone = value & 0x7FF;
			rail = value >>> 11;
		}

		public RailInfo(int zone, int rail) {
			this.zone = zone;
			this.rail = rail;
			//System.out.println("zone " + zone + " rail " + rail);
		}
	}

	public static class RailHeader {

		public int railFile;
		public short cameraDataIdx;

		public RailHeader(FSFile fsf) {
			try {
				DataInStream in = fsf.getDataInputStream();

				railFile = in.readUnsignedShort();
				cameraDataIdx = in.readShort();

				in.close();
			} catch (IOException ex) {
				Logger.getLogger(FieldRailLoader.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
