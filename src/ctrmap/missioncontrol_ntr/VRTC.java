package ctrmap.missioncontrol_ntr;

import java.util.Calendar;

public class VRTC {

	public static final boolean RTC_ACCEL = false;
	public static final double RTC_ACCEL_FACTOR_INV = 10000.0;

	public static final int getSecondOfDay() {
		Calendar cal = Calendar.getInstance();
		if (!RTC_ACCEL) {
			return cal.get(Calendar.SECOND) + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.HOUR_OF_DAY) * 3600;
		} else {
			return (int) ((System.currentTimeMillis() % RTC_ACCEL_FACTOR_INV) * (86400.0 / RTC_ACCEL_FACTOR_INV));
		}
	}

	public static enum Season {
		SPRING,
		SUMMER,
		AUTUMN,
		WINTER;

		public static Season getRTC() {
			Calendar cal = Calendar.getInstance();
			return values()[cal.get(Calendar.MONTH) & 3];
		}

		public static Season getRTCAstronomical() {
			Calendar cal = Calendar.getInstance();
			int day = cal.get(Calendar.DAY_OF_MONTH);
			switch (cal.get(Calendar.MONTH)) {
				case Calendar.JANUARY:
				case Calendar.FEBRUARY:
					return WINTER;
				case Calendar.APRIL:
				case Calendar.MAY:
					return SPRING;
				case Calendar.JULY:
				case Calendar.AUGUST:
					return SUMMER;
				case Calendar.OCTOBER:
				case Calendar.NOVEMBER:
					return AUTUMN;

				case Calendar.MARCH:
					return day >= 20 ? SPRING : WINTER;
				case Calendar.JUNE:
					return day >= 21 ? SUMMER : SPRING;
				case Calendar.SEPTEMBER:
					return day >= 22 ? AUTUMN : SUMMER;
				case Calendar.DECEMBER:
					return day >= 21 ? WINTER : AUTUMN;
			}
			return null;
		}
	}

	public static enum DayPart {
		MORNING,
		DAY,
		SUNSET,
		EVENING,
		NIGHT;

		private static final byte[][] DAY_PARTS_PER_HOUR = new byte[][]{
			{04, 04, 04, 04, 04, 00, 00, 00, 00, 00, 01, 01, 01, 01, 01, 01, 01, 02, 02, 02, 03, 03, 03, 03},
			{04, 04, 04, 04, 00, 00, 00, 00, 00, 01, 01, 01, 01, 01, 01, 01, 01, 01, 01, 02, 02, 03, 03, 03},
			{04, 04, 04, 04, 04, 04, 00, 00, 00, 00, 01, 01, 01, 01, 01, 01, 01, 02, 02, 02, 03, 03, 03, 03},
			{04, 04, 04, 04, 04, 04, 04, 00, 00, 00, 00, 01, 01, 01, 01, 01, 01, 02, 02, 03, 03, 03, 03, 03}
		};

		public static DayPart getRTC() {
			Season season = Season.getRTC();
			int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			return DayPart.values()[DAY_PARTS_PER_HOUR[season.ordinal()][hour]];
		}
	}
}
