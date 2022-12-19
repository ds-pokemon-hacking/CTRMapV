package ctrmap.missioncontrol_ntr.field;

import ctrmap.missioncontrol_ntr.field.rail.FieldRailLoader;
import ctrmap.missioncontrol_ntr.field.script.FieldScriptConfigurator;
import ctrmap.formats.pokemon.containers.DefaultGamefreakContainer;
import ctrmap.formats.ntr.nitroreader.nsbta.NSBTA;
import ctrmap.formats.ntr.nitroreader.nsbtx.NSBTX;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_base.McLogger;
import ctrmap.missioncontrol_base.InputManager;
import ctrmap.renderer.scene.Scene;
import java.util.List;
import ctrmap.formats.common.GameInfo;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import xstandard.util.ProgressMonitor;
import java.util.ArrayList;
import ctrmap.formats.pokemon.gen5.area.AreaPatAnimeData;
import ctrmap.formats.pokemon.gen5.area.AreaTable;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildingResource;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildings;
import ctrmap.formats.pokemon.gen5.mapreplace.VMapReplaceTable;
import ctrmap.formats.pokemon.gen5.zone.extra.ZoneFogIndex;
import ctrmap.formats.pokemon.gen5.zone.extra.ZoneGimmickIndex;
import ctrmap.formats.pokemon.gen5.npcreg.VNPCRegistry;
import ctrmap.formats.pokemon.gen5.zone.VZoneTable;
import ctrmap.formats.pokemon.gen5.zone.entities.VGridObject;
import ctrmap.formats.pokemon.gen5.zone.extra.ZoneMapEffSkillIndex;
import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.missioncontrol_base.debug.IMCDebuggable;
import ctrmap.missioncontrol_ntr.VGameConstant;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.VMCModuleLogSource;
import ctrmap.missioncontrol_ntr.VMcConfig;
import ctrmap.missioncontrol_ntr.field.debug.VFieldDebugger;
import ctrmap.missioncontrol_ntr.field.mmodel.PokemonFieldMModelLUT;
import ctrmap.missioncontrol_ntr.field.structs.VArea;
import ctrmap.missioncontrol_ntr.field.structs.VMap;
import ctrmap.missioncontrol_ntr.field.structs.VZone;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;

public class VFieldController implements IMCDebuggable<VFieldDebugger> {

	public static final VGameConstant LOC_NAME_TEXT_ID = new VGameConstant(89, 109);

	public VLaunchpad mc;

	public boolean isModuleLoaded = false;
	public boolean isOmniMatrixLoad = false;
	public boolean isOmniMatrixLoaded = false;

	public Scene rootScene;

	public Scene fieldScene = new Scene("Field");

	private final NTRGameFS fs;
	private final GameInfo game;

	private InputManager input;

	public VZoneTable zoneTable;
	public AreaTable areaTable;
	public ZoneFogIndex fogIndex;
	public ZoneGimmickIndex gimmickIndex;
	public ZoneMapEffSkillIndex flashIndex;
	public VNPCRegistry npcTable;
	public PokemonFieldMModelLUT pokeNPCLUT;
	public VMapReplaceTable mapReplace;

	public VFieldCameraController camera;
	public VPlayerController player;

	public VZone zone = new VZone(this);
	public VArea area = new VArea();
	public VMap map = new VMap();

	public VFieldLightSystem lights;

	public FieldRailLoader railLoader;
	public FieldCameraLoader camLoader;
	public FieldMapConfigurator mapConfigs;
	public FieldScriptConfigurator scrConfigs;

	public TextFile locationNames;

	private McLogger log;
	public VMcConfig config;

	private VFieldInput fieldInput;

	public VFieldController(VLaunchpad mc) {
		this.log = mc.log;
		this.config = mc.config;
		log.out("Bootstrapping Field.", VMCModuleLogSource.FIELD);
		long begin = System.currentTimeMillis();

		this.mc = mc;
		rootScene = mc.mcScene;
		this.input = mc.input;
		this.fs = mc.fs;
		this.game = mc.game;
		fieldInput = new VFieldInput(input, this);

		zoneTable = new VZoneTable(fs.NARCGet(NARCRef.FIELD_ZONE_DATA, 0));
		areaTable = new AreaTable(fs.NARCGetArchive(NARCRef.FIELD_AREA_DATA));
		fogIndex = new ZoneFogIndex(fs.NARCGet(NARCRef.FIELD_ENV_FOG, game.isBW2() ? 29 : 13));
		gimmickIndex = new ZoneGimmickIndex(fs.NARCGet(NARCRef.FIELD_ZONE_GIMMICK_INDEX, 0));
		flashIndex = new ZoneMapEffSkillIndex(fs.NARCGet(NARCRef.FIELD_ZONE_FLASH_INDEX, 0));
		locationNames = new TextFile(fs.NARCGet(NARCRef.MSGDATA_SYSTEM, LOC_NAME_TEXT_ID.get(game)), GenVMessageHandler.INSTANCE);
		npcTable = new VNPCRegistry(fs.NARCGet(NARCRef.FIELD_MMODEL_INDEX, 0));
		railLoader = new FieldRailLoader(zoneTable, fs, game);
		camLoader = new FieldCameraLoader(zoneTable, fs, game);
		mapConfigs = new FieldMapConfigurator(fs, game);
		scrConfigs = new FieldScriptConfigurator(fs, game);
		lights = new VFieldLightSystem(fs);
		pokeNPCLUT = new PokemonFieldMModelLUT(fs.NARCGet(NARCRef.FIELD_MMODEL_PM_LOOKUP, 0));
		mapReplace = new VMapReplaceTable(fs.NARCGet(NARCRef.FIELD_MAP_REPLACE_TABLE, 0));

		/*int max = fs.NARCGetDataMax(NARCRef.FIELD_CAMERA_AREA_GRID);
		
		for (int i = 0; i < max; i++) {
			VCameraDataFile f = new VCameraDataFile(fs.NARCGet(NARCRef.FIELD_CAMERA_AREA_GRID, i));
			for (VAbstractCameraData c : f.entries) {
				System.out.println(((VCameraDataRect)c).horizontal);
			}
		}
		
		int rsMax = fs.NARCGetDataMax(NARCRef.FIELD_ZONE_NOGRID_HEADERS);
		
		for (int i = 0; i < rsMax; i++) {
			FieldRailLoader.RailHeader rh = new FieldRailLoader.RailHeader(fs.NARCGet(NARCRef.FIELD_ZONE_NOGRID_HEADERS, i));
			System.out.println(rh.cameraDataIdx);
		}*/

 /*VMoveModel mdl = new VMoveModel(this, null);
		mdl.NPCData = new VNPC();
		for (int i = 4096; i < 4716; i++) {
			mdl.setModelID(i);
			if (!mdl.resource.textures.isEmpty()) {
				Texture tex = mdl.resource.textures.get(0);
				TextureConverter.writeTextureToFile(new DiskFile("D:\\_REWorkspace\\DevDocs\\pokeactor\\" + (i - 4096) + ".png"), "png", tex);
			}
		}*/
		log.out("Field init done. Loading took " + (System.currentTimeMillis() - begin) + "ms.", VMCModuleLogSource.FIELD);
	}

	public String[] getLocationNamesArray() {
		return locationNames.getLinesArray();
	}

	public void loadResident() {
		if (mc.residentMode) {
			log.out("Loading Field in resident mode.", VMCModuleLogSource.FIELD);
			loadFieldModule();
			log.out("Field loaded.", VMCModuleLogSource.FIELD);
		}
	}

	public void loadFieldModule() {
		if (!isModuleLoaded) {
			player = new VPlayerController(this, fs, game, config);
			camera = new VFieldCameraController(this);

			fieldScene.addSceneAnimationCallback((frameAdvance) -> {
				fieldInput.update();
				lights.updateScene(fieldScene);
				camera.updateLocal();
				if (zone != null && !fieldScene.cameraInstances.isEmpty()) {
					zone.updateActorCamera(fieldScene.cameraInstances.get(0));
				}
			});

			mc.getDebuggerManager().registDebuggable(this);

			isModuleLoaded = true;
		}
	}

	public void unloadFieldModule() {
		if (!mc.residentMode) {
			fieldScene = null;
			player = null;
			camera = null;
			isModuleLoaded = false;
			mc.getDebuggerManager().unregistDebuggable(this);
		}
	}

	public String getPlaceName(int zoneNumber) {
		return locationNames.getOrDefault(zoneTable.getHeader(zoneNumber).locNameNo, "PlaceName_Zone" + zoneNumber);
	}

	public void forceFullReload() {
		if (zone.header.areaID != area.id) {
			loadArea(zone.header.areaID);
		} else {
			area.forceFullReload(fs);
		}
		if (zone.header.matrixID != map.matrixId) {
			loadMatrix(zone.header.matrixID);
		} else {
			map.forceFullReload(fs);
		}
		area.finalizeLoad(map);
		zone.forceFullReload();
	}

	public void zoneLoad(int zoneId) {
		zoneLoad(zoneId, null);
	}

	public void zoneLoad(int zoneId, ProgressMonitor monitor) {
		loadZone(zoneId, -1, -1, VZone.LoadMethod.COLD_BOOT, monitor);
	}

	public void zoneWarp(int zoneId, int sourceWarpId, int targetWarpId) {
		loadZone(zoneId, sourceWarpId, targetWarpId, VZone.LoadMethod.WARP, null);
	}

	public void zoneSeamlessTransition(int zoneId) {
		loadZone(zoneId, -1, -1, VZone.LoadMethod.TRANSITION, null);
	}

	private void loadZone(int zoneId, int srcWarpId, int targetWarpId, VZone.LoadMethod loadMethod, ProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new ProgressMonitor.DummyProgressMonitor();
		}
		monitor.setProgressPercentage(0);
		monitor.setProgressSubTitle("Loading ZoneData");
		log.out("Began loading zone " + zoneId + " using method " + loadMethod.toString() + ".", "[ZONE]", VMCModuleLogSource.FIELD);
		long begin = System.currentTimeMillis();

		boolean isNewZone = zoneId != zone.id;

		if (isNewZone) {
			fieldScene.removeChild(zone.npcScene);
			zone.unregistDebuggable();
			zone = new VZone(this, fs, zoneId);
		}

		monitor.setProgressPercentage(25);
		monitor.setProgressSubTitle("Loading AreaData");
		log.out("Loading area " + zone.header.areaID + " over " + area.id, "[AREA]", VMCModuleLogSource.FIELD);
		boolean areaChange = zone.header.areaID != area.id;
		loadArea(zone.header.areaID);

		monitor.setProgressPercentage(50);
		monitor.setProgressSubTitle("Loading world");
		log.out("Loading map " + zone.header.matrixID + " over " + map.matrixId, "[WORLD]", VMCModuleLogSource.FIELD);
		if (isOmniMatrixLoad && !isOmniMatrixLoaded) {
			int bldMax = fs.NARCGetDataMax(NARCRef.FIELD_BMODEL_BUNDLE_EXT);
			for (int i = 0; i < bldMax; i++) {
				AreaBuildings blds = new AreaBuildings(new DefaultGamefreakContainer(fs.NARCGet(NARCRef.FIELD_BMODEL_BUNDLE_EXT, i), "AB"));
				area.bmReg.merge(blds);
			}
		}
		boolean needsMapDebuggerForce = areaChange && zone.header.matrixID == map.matrixId && map.matrixZoneId != zoneId;
		loadMatrix(zone.header.matrixID);
		if (needsMapDebuggerForce) {
			map.reattachDebuggers();
		}

		if (isOmniMatrixLoad && !isOmniMatrixLoaded) {
			List<Material> worldMats = new ArrayList<>();
			for (Model mdl : map.omniChunk.models) {
				worldMats.addAll(mdl.materials);
			}

			int extTexturesMax = fs.NARCGetDataMax(NARCRef.FIELD_MAP_TEX);

			for (int i = 0; i < extTexturesMax; i++) {
				area.worldTextures.merge(new NSBTX(fs.NARCGet(NARCRef.FIELD_MAP_TEX, i)).TEX0.toGeneric(worldMats));
			}

			int srtAnimeMax = fs.NARCGetDataMax(NARCRef.FIELD_MAP_ANM_SRT);
			for (int i = 0; i < srtAnimeMax; i++) {
				area.worldAnimationsSRT.merge(new NSBTA(fs.NARCGet(NARCRef.FIELD_MAP_ANM_SRT, i)).toGeneric());
			}

			int patAnimeMax = fs.NARCGetDataMax(NARCRef.FIELD_MAP_ANM_PAT);
			for (int i = 0; i < patAnimeMax; i++) {
				AreaPatAnimeData patAnime = new AreaPatAnimeData(fs.NARCGet(NARCRef.FIELD_MAP_ANM_PAT, i));
				area.worldAnimationsPat.merge(patAnime.toGeneric(worldMats));
			}

			List<Material> bldMats = new ArrayList<>();
			for (AreaBuildingResource mdl : area.bmReg.buildings) {
				for (Model model : mdl.models) {
					bldMats.addAll(model.materials);
				}
			}

			int bldTexMax = fs.NARCGetDataMax(NARCRef.FIELD_BMODEL_TEX_EXT);
			for (int i = 0; i < bldTexMax; i++) {
				area.propTextures.merge(new NSBTX(fs.NARCGet(NARCRef.FIELD_BMODEL_TEX_EXT, i)).TEX0.toGeneric(bldMats));
			}

			isOmniMatrixLoaded = true;
		}
		area.finalizeLoad(map);

		monitor.setProgressPercentage(85);
		monitor.setProgressSubTitle("Finishing up...");
		zone.loadingFinalize();
		camera.onZoneLoad(zone);
		log.out("Finalizing load process...", "[ZONE]", VMCModuleLogSource.FIELD);

		if (isNewZone || loadMethod == VZone.LoadMethod.COLD_BOOT) {
			switch (loadMethod) {
				case SAVE_DATA:
				case COLD_BOOT:
					player.setLocation(zone.header.flyX, zone.header.flyZ, VGridObject.tileToWorldNonCentered(zone.header.flyY));
					player.forceUpdateHeight();
					break;
				case WARP:
					player.setFromWarp(zone.entities.warps.get(srcWarpId), zone.entities.warps.get(targetWarpId));
					break;
			}
		}
		log.out("Warped player to " + player.getPosition(), "[ZONE]", VMCModuleLogSource.FIELD);
		if (isNewZone) {
			fieldScene.addScene(zone.npcScene);
		}
		zone.registDebuggable();

		monitor.setProgressPercentage(100);
		monitor.setProgressSubTitle("Done. Collecting garbage.");
		System.gc();
		log.out("Finished loading zone in " + (System.currentTimeMillis() - begin) + "ms.", "[ZONE]", VMCModuleLogSource.FIELD);
	}

	private void loadArea(int areaId) {
		if (VArea.isAreaIdHasSeasons(areaId, game)) {
			areaId += mc.season.ordinal();
		}
		if (areaId != area.id) {
			area.free();
			area = null;
			System.gc();
			area = new VArea(fs, this, areaId);
			lights.loadArea(area, mc.season);
		}
	}

	public final int resolveChunkID(int matrixId, int chunkId) {
		for (VMapReplaceTable.Entry e : mapReplace.entries) {
			if (!e.typeIsMatrix && e.matrixID == matrixId) {
				if (e.replacementValues[0] == chunkId) {
					return resolveMapReplace(e);
				}
			}
		}
		return chunkId;
	}

	public final int resolveMatrixID(int matrixId) {
		for (VMapReplaceTable.Entry e : mapReplace.entries) {
			if (e.typeIsMatrix && e.matrixID == matrixId) {
				return resolveMapReplace(e);
			}
		}
		return matrixId;
	}

	public final int resolveMapReplace(VMapReplaceTable.Entry entry) {
		int seasonOrd = mc.season.ordinal();
		int gamePrimaryInc = game.isPrimary() ? 1 : 0;
		switch (entry.replacementCondition) {
			case VMapReplaceTable.CONDITION_GAME_VERSION:
				return entry.replacementValues[gamePrimaryInc];
			case VMapReplaceTable.CONDITION_SEASON:
				return entry.replacementValues[seasonOrd];
			case VMapReplaceTable.CONDITION_DRIFTVEIL_MARKET:
				return entry.replacementValues[seasonOrd + gamePrimaryInc];
			default:
				return entry.replacementValues[0]; //work values - not yet implemented
		}
	}

	private void loadMatrix(int matrixId) {
		if (resolveMatrixID(matrixId) != map.matrixId) {
			fieldScene.removeChild(map.worldScene);
			map.free();
			map = null;
			System.gc();
			map = new VMap(fs, zone, this);
			fieldScene.addScene(map.worldScene);
		} else if (map.matrixZoneId != zone.id) {
			map.changeZone(fs, zone);
		}
	}

	public void fieldOpen() {
		loadFieldModule();
		rootScene.addChild(fieldScene);
	}

	public void fieldClose() {
		rootScene.removeChild(fieldScene);
		unloadFieldModule();
	}

	@Override
	public Class<VFieldDebugger> getDebuggerClass() {
		return VFieldDebugger.class;
	}

	@Override
	public void attach(VFieldDebugger debugger) {
		debugger.attachField(this);
	}

	@Override
	public void detach(VFieldDebugger debugger) {
		debugger.attachField(null);
	}

	@Override
	public void destroy(VFieldDebugger debugger) {
		debugger.attachField(null);
	}
}
