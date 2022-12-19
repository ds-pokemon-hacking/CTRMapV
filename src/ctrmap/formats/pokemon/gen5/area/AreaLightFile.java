package ctrmap.formats.pokemon.gen5.area;

import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.missioncontrol_ntr.VRTC;

/**
 *
 */
public class AreaLightFile {

	public List<AreaLightEntry> entries = new ArrayList<>();

	public AreaLightFile(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();

			int count = in.getLength() / 0x34;
			for (int i = 0; i < count; i++) {
				entries.add(new AreaLightEntry(in));
			}

			in.close();
		} catch (IOException ex) {
			Logger.getLogger(AreaLightFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public AreaLightEntry getLightEntryBySeconds(VRTC.Season season, int secondOfDay) {
		int lastSeconds = 0;
		for (AreaLightEntry e : entries) {
			int seconds = e.getLightSecondThreshold(season);
			if (secondOfDay >= lastSeconds && secondOfDay < seconds) {
				return e;
			}
			lastSeconds = seconds;
		}
		if (!entries.isEmpty()) {
			return entries.get(0);
		}
		return null;
	}

	public AreaLightEntry getNextLightEntry(AreaLightEntry e) {
		int nextIdx = entries.indexOf(e) + 1;
		nextIdx %= entries.size();
		return entries.get(nextIdx);
	}

	public AreaLightEntry getPreviousLightEntry(AreaLightEntry e) {
		int prevIdx = entries.indexOf(e) - 1;
		if (prevIdx < 0) {
			prevIdx += entries.size();
		}
		return entries.get(prevIdx);
	}

	public static class AreaLightEntry {

		public VRTC.DayPart dayPart = VRTC.DayPart.DAY;
		public int minutesShift;

		public final boolean[] lightsEnabled = new boolean[4];
		public final GXColor[] colors = new GXColor[4];
		public final Vec3f[] directions = new Vec3f[4];

		public GXColor matDiffuse;
		public GXColor matAmbient;
		public GXColor matSpecular;
		public GXColor matEmission;
		
		public GXColor fogColor;

		public GXColor clearColor;
		
		public AreaLightEntry() {
			
		}
		
		public AreaLightEntry(AreaLightEntry e) {
			dayPart = e.dayPart;
			minutesShift = e.minutesShift;
			for (int i = 0; i < 4; i++) {
				lightsEnabled[i] = e.lightsEnabled[i];
				colors[i] = e.colors[i];
				directions[i] = new Vec3f(e.directions[i]);
			}
			matDiffuse = e.matDiffuse;
			matAmbient = e.matAmbient;
			matSpecular = e.matSpecular;
			matEmission = e.matEmission;
			fogColor = e.fogColor;
			clearColor = e.clearColor;
		}

		public AreaLightEntry(DataInput in) throws IOException {
			dayPart = VRTC.DayPart.values()[in.readUnsignedShort()];
			minutesShift = in.readShort();

			for (int i = 0; i < 4; i++) {
				lightsEnabled[i] = in.readBoolean();
			}
			for (int i = 0; i < 4; i++) {
				colors[i] = new GXColor(in.readUnsignedShort());
			}
			for (int i = 0; i < 4; i++) {
				directions[i] = FXIO.readVecFX16(in);
			}
			matDiffuse = new GXColor(in.readUnsignedShort());
			matAmbient = new GXColor(in.readUnsignedShort());
			matSpecular = new GXColor(in.readUnsignedShort());
			matEmission = new GXColor(in.readUnsignedShort());
			fogColor = new GXColor(in.readUnsignedShort());
			clearColor = new GXColor(in.readUnsignedShort());
		}
		
		public int getLightSecondThreshold(VRTC.Season season) {
			return 3600 /* seconds per hour */ * getLightChangeHourForSeason(season, dayPart) + (minutesShift * 60);
		}

		private static final int[][] LIGHT_CHANGE_HOURS = new int[][]{
			{5, 10, 17, 20, 0},
			{4, 9, 19, 21, 0},
			{6, 10, 17, 20, 0},
			{7, 11, 17, 19, 0}
		};

		public static final int getLightChangeHourForSeason(VRTC.Season season, VRTC.DayPart dayPart) {
			return LIGHT_CHANGE_HOURS[season.ordinal()][dayPart.ordinal()];
		}
	}
}
