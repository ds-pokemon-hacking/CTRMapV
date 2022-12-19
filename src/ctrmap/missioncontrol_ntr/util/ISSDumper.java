package ctrmap.missioncontrol_ntr.util;

import ctrmap.formats.pokemon.gen5.iss.ISS3DSoundConfig;
import ctrmap.formats.pokemon.gen5.iss.ISSBGMInfo;
import ctrmap.formats.pokemon.gen5.iss.ISSCityUnit;
import ctrmap.formats.pokemon.gen5.iss.ISSDungeonConfig;
import ctrmap.formats.pokemon.gen5.iss.ISSSwitchSet;
import ctrmap.formats.pokemon.gen5.iss.ISSZoneConfig;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;

public class ISSDumper {

	public static void dump(NTRGameFS fs, FSFile outDir) {
		ISSBGMInfo bgmInfo = new ISSBGMInfo(fs.NARCGet(NARCRef.ISS_BGM_INFO, 0));
		YamlReflectUtil.serializeObjectAsYml(bgmInfo).writeToFile(outDir.getChild("spacestation_bgmInfo.yml"));

		int cityCount = fs.NARCGetDataMax(NARCRef.ISS_CITY_DATA);
		ISSCityUnit[] cityUnits = new ISSCityUnit[cityCount];
		for (int i = 0; i < cityCount; i++) {
			cityUnits[i] = new ISSCityUnit(fs.NARCGet(NARCRef.ISS_CITY_DATA, i));
		}
		YamlReflectUtil.serializeObjectAsYml(cityUnits).writeToFile(outDir.getChild("spacestation_cityUnits.yml"));

		int zoneCount = fs.NARCGetDataMax(NARCRef.ISS_ZONE_DATA);
		ISSZoneConfig[] zoneConfigs = new ISSZoneConfig[zoneCount];
		for (int i = 0; i < zoneCount; i++) {
			zoneConfigs[i] = new ISSZoneConfig(fs.NARCGet(NARCRef.ISS_ZONE_DATA, i));
		}
		YamlReflectUtil.serializeObjectAsYml(zoneConfigs).writeToFile(outDir.getChild("spacestation_zoneConfigs.yml"));

		int dunCount = fs.NARCGetDataMax(NARCRef.ISS_DUNGEON_DATA);
		ISSDungeonConfig[] dunConfigs = new ISSDungeonConfig[dunCount];
		for (int i = 0; i < dunCount; i++) {
			dunConfigs[i] = new ISSDungeonConfig(fs.NARCGet(NARCRef.ISS_DUNGEON_DATA, i));
		}
		YamlReflectUtil.serializeObjectAsYml(dunConfigs).writeToFile(outDir.getChild("spacestation_dungeonConfigs.yml"));

		int switchCount = fs.NARCGetDataMax(NARCRef.ISS_SWITCH_DATA);
		ISSSwitchSet[] switchSets = new ISSSwitchSet[switchCount];
		for (int i = 0; i < switchCount; i++) {
			switchSets[i] = new ISSSwitchSet(fs.NARCGet(NARCRef.ISS_SWITCH_DATA, i));
		}
		YamlReflectUtil.serializeObjectAsYml(switchSets).writeToFile(outDir.getChild("spacestation_switchSets.yml"));

		int sound3dCount = fs.NARCGetDataMax(NARCRef.ISS_3D_SOUND_DATA);
		ISS3DSoundConfig[] sound3DConfigs = new ISS3DSoundConfig[sound3dCount];
		for (int i = 0; i < sound3dCount; i++) {
			sound3DConfigs[i] = new ISS3DSoundConfig(fs.NARCGet(NARCRef.ISS_3D_SOUND_DATA, i));
		}
		YamlReflectUtil.serializeObjectAsYml(sound3DConfigs).writeToFile(outDir.getChild("spacestation_3dSoundConfigs.yml"));
	}
}
