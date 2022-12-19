package ctrmap.missioncontrol_ntr.field;

import ctrmap.missioncontrol_ntr.field.rail.FieldRailLoader;
import ctrmap.formats.common.GameInfo;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.formats.ntr.common.FXIO;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;

/**
 *
 */
public class FieldMapConfigurator {

	public List<MapConfig> configs = new ArrayList<>();
	
	public FieldMapConfigurator(NTRGameFS fs, GameInfo game) {
		try {
			DataIOStream io = fs.getDecompressedOverlayMaybeRO(36).getDataIOStream();

			io.setBase(fs.getOvlLoadAddr(36));

			if (game.getSubGame() == GameInfo.SubGame.W2) {
				io.seekAndSeek(0x21814EC);

				for (int i = 0; i < 29; i++) {
					configs.add(new MapConfig(io));
				}
			}
			
			io.resetBase();

			io.close();
		} catch (IOException ex) {
			Logger.getLogger(FieldRailLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public float getChunkSpan(int config){
		if (config >= 0 && config < configs.size()){
			return configs.get(config).chunkSpan;
		}
		return 512f;
	}
	
	public static class MapConfig {
		public static final int BYTES = 0x48;
		
		public float chunkSpan;
		public byte[] unknown;
		
		public MapConfig(DataInput in) throws IOException {
			chunkSpan = FXIO.readFX32(in);
			unknown = new byte[BYTES - Integer.BYTES];
			in.readFully(unknown);
		}
	}
}
