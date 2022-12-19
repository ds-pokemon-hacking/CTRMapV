package ctrmap.formats.pokemon.gen5.cal;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VWeatherCalendar {

	private static final int LEAP_YEAR_DAYS = 366; //February is forced to 29 days in Pokemon

	private Map<Integer, int[]> calendar = new HashMap<>();

	public VWeatherCalendar(FSFile calData, FSFile indexData) {
		try {
			DataInStream idx = indexData.getDataInputStream();
			DataIOStream cal = calData.getDataIOStream();

			int count = idx.readUnsignedShort();
			for (int i = 0; i < count; i++) {
				int zoneId = idx.readUnsignedShort();
				cal.seek(idx.readUnsignedShort());
				int[] calArr = new int[LEAP_YEAR_DAYS];
				for (int day = 0; day < calArr.length; day++) {
					calArr[day] = cal.read();
				}
				calendar.put(zoneId, calArr);
			}

			idx.close();
			cal.close();
		} catch (IOException ex) {
			Logger.getLogger(VWeatherCalendar.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public int getWeatherForZoneAndDay(int zoneId, int month, int day) {
		int[] arr = calendar.get(zoneId);
		if (arr == null) {
			return -1;
		}
		int daysBefore = 0;
		for (int m = 1; m < month; m++) {
			daysBefore += 30;
			if (m == 2) {
				daysBefore--;
			} else {
				int change = (m & 1);
				if (m < 8) {
					daysBefore += change;
				} else {
					daysBefore += 1 - change;
				}
			}
		}
		int daysAbs = daysBefore + day;
		return arr[daysAbs];
	}
}
