package ctrmap.missioncontrol_ntr.field.mmodel;

/**
 *
 */
public class OBJIDNormalizer {

	public static int getNormalizedOBJCODE_BW1(int objCode) {
		if (objCode >= 223) {
			if (objCode < 4096 || objCode >= 4662) {
				if (objCode < 8192 || objCode >= 8202) {
					objCode = 10;
				} else {
					objCode = objCode - 7403;
				}
			} else {
				objCode = objCode - 3873;
			}
		}
		return objCode;
	}

	public static int getNormalizedOBJCODE_BW2(int objCode) {
		//All objects < 377 -> 0 - 377
		//Objects 4096 to 4716 -> 377 to 997
		//Objects 8192 to 8203 -> 998 to 1008
		if (objCode >= 377) {
			if (objCode < 4096 || objCode > 4716) {
				if (objCode < 8192 || objCode > 8203) {
					objCode = 10;
				} else {
					objCode -= 7195;
				}
			} else {
				objCode -= 3719;
			}
		}
		return objCode;
	}
}
