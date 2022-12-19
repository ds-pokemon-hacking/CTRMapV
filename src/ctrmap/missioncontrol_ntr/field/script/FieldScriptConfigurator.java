package ctrmap.missioncontrol_ntr.field.script;

import ctrmap.formats.common.GameInfo;
import ctrmap.missioncontrol_ntr.field.rail.FieldRailLoader;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FieldScriptConfigurator {

	public List<ScriptPluginConfig> configs = new ArrayList<>();

	public FieldScriptConfigurator(NTRGameFS fs, GameInfo game) {
		try {
			DataIOStream io = fs.getDecompressedOverlayMaybeRO(12).getDataIOStream();

			io.setBase(fs.getOvlLoadAddr(12));

			if (game.getSubGame() == GameInfo.SubGame.W2) {
				io.seek(0x21549C0);
				int count = io.read();

				io.seekAndSeek(0x21549C8);

				for (int i = 0; i < count; i++) {
					configs.add(new ScriptPluginConfig(io));
				}
			}

			io.resetBase();

			io.close();
		} catch (IOException ex) {
			Logger.getLogger(FieldRailLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public ScriptPluginConfig getCfgForZone(int zoneId) {
		for (ScriptPluginConfig c : configs) {
			if (c.zoneIds.contains(zoneId)) {
				return c;
			}
		}
		return null;
	}

	public static class ScriptPluginConfig {

		public static final int BYTES = 0x14;

		public int commandTablePtr;
		public List<Integer> zoneIds = new ArrayList<>();
		public int primaryOverlay;
		public int secondaryOverlay;

		public ScriptPluginConfig(DataIOStream io) throws IOException {
			commandTablePtr = io.readInt();
			int zoneIDsPtr = io.readInt();
			int zoneIDsCount = io.readInt();
			primaryOverlay = io.readInt();
			secondaryOverlay = io.readInt();
			io.checkpoint();

			if (zoneIDsPtr != 0) {
				io.seek(zoneIDsPtr);
				for (int i = 0; i < zoneIDsCount; i++) {
					zoneIds.add(io.readUnsignedShort());
				}
			}

			io.resetCheckpoint();
		}

		public static int[] getOverlayIDs(ScriptPluginConfig cfg) {
			if (cfg == null) {
				return new int[0];
			}
			return cfg.getOverlayIDs();
		}
		
		private int[] getOverlayIDs() {
			return new int[]{primaryOverlay, secondaryOverlay};
		}
	}
}
