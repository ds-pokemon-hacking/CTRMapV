package ctrmap.missioncontrol_ntr.poketool;

import ctrmap.missioncontrol_base.pokeparam.*;

/**
 *
 */
public class VNatureCalc {

	public static void main(String[] args) {
		/*for (int iv = 0; iv < 256; iv++) {
			int pid = calculatePIDForTrPoke(
					197,
					161,
					389,
					iv,
					74,
					getGenderAndAbilityCfg(GenderGenerationType.DEFAULT, PokemonAbilityConfig.Slot.SLOT_1),
					31,
					false
			);
			PokemonNature n = getNatureForPID(pid);
			int normalIV = getAllIVForTrPoke(iv);
			System.out.println(iv + " TrainerIV | " + "AllIV: " + normalIV + "/Nature: " + n);
		}*/
		int trId = 50;
		int trClass = 161;
		int pokeNum = 389;
		int iv;
		int level;
		int abilGnd = getGenderAndAbilityCfg(GenderGenerationType.DEFAULT, PokemonAbilityConfig.Slot.SLOT_1);
		int personalGenderProb = 127;
		boolean isTrainerFemale = false;
		//for (int trainer = 0; trainer < 1000; trainer++) {
			for (int poke = 0; poke < 650; poke++) {
				for (iv = 0; iv < 256; iv++) {
					for (level = 1; level < 100; level++) {
						int pid = calculatePIDForTrPoke(trId, trClass, poke, iv, level, abilGnd, personalGenderProb, isTrainerFemale);
						if (isTrainerShiny(pid)) {
							System.out.println("shiny pkm " + poke + " of trainer " + trId + " with difficulty " + iv + " level " + level);
						}
					}
				}
			}
		//}
	}

	private static int getAllIVForTrPoke(int trPokeIV) {
		int omniIV = (((31 * trPokeIV) / 255) & 0x1F);
		return omniIV;
	}

	private static PokemonIVSet getNormalIVForTrPoke(int trPokeIV) {
		int omniIV = getAllIVForTrPoke(trPokeIV);
		PokemonIVSet set = new PokemonIVSet();
		set.setAll(omniIV);
		return set;
	}

	private static PokemonNature getNatureForPID(int pid) {
		return PokemonNature.valueOf((int) (Integer.toUnsignedLong(pid >>> 8) % PokemonNature.COUNT));
	}

	public static boolean isTrainerShiny(int pid) {
		return isShiny(0, pid);
	}

	public static boolean isShiny(int tid, int pid) {
		return ((tid & 0xFFFF) ^ ((tid & 0xFFFF0000) >>> 16) ^ ((pid & 0xFFFF0000) >>> 16) ^ (pid & 0xFFFF)) < 8;
	}

	public static int calculatePIDForTrPoke(int trainerID, int trainerClass, int pokeNum, int trPokeIV, int pokeLevel, int abilGenderCfg, int personalGenderCfg, boolean isTrainerFemale) {
		long pidTemp = trainerID + pokeNum + trPokeIV + pokeLevel;
		for (int i = 0; i < trainerClass; i++) {
			pidTemp = pidTemp * 0x5D588B656C078965L + 0x269EC3;
		}
		int pid = (int) (((pidTemp >>> Integer.SIZE) & 0xFFFFFFFF) >> 16 << 8) + getGenderAndAbility(abilGenderCfg, personalGenderCfg, isTrainerFemale);
		return pid;
	}

	public static int getGenderAndAbilityCfg(GenderGenerationType g, PokemonAbilityConfig.Slot abilitySlot) {
		return g.ordinal() | (abilitySlot.ordinal() << 4);
	}

	public static int getGenderAndAbility(int abilGenderCfg, int personalGenderCfg, boolean isTrainerFemale) {
		int result = isTrainerFemale ? 120 : 136;
		int g = abilGenderCfg & 0xF;
		int a = (abilGenderCfg & 0xF0) >> 4;
		if (abilGenderCfg != 0) {
			if (g != 0) {
				result = personalGenderCfg;
				if (g == 1) {
					result += 2;
				} else {
					result -= 2;
				}
			}

			switch (PokemonAbilityConfig.Slot.values()[a]) {
				case SLOT_1:
					//default behavior
					break;
				case SLOT_2:
					result &= 0xFFFFFFFE;
					break;
				case SLOT_HIDDEN:
					result |= 1;
					break;
			}
		}
		return result;
	}

	public static int getIVValueForPokeStat(int ivUnion, PokemonStat stat) {
		return (ivUnion >> (stat.ordinal() * 5)) & 31;
	}

	public static enum GenderGenerationType {
		DEFAULT,
		MALE,
		FEMALE
	}
}
