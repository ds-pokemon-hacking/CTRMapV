package ctrmap.missioncontrol_ntr.field.structs;

import ctrmap.formats.pokemon.containers.DefaultGamefreakContainer;
import ctrmap.formats.ntr.nitroreader.nsbta.NSBTA;
import ctrmap.formats.ntr.nitroreader.nsbtx.NSBTX;
import ctrmap.formats.ntr.nitroreader.nsbtx.NSBTXDataBlock;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.util.MaterialProcessor;
import ctrmap.formats.common.GameInfo;
import java.util.ArrayList;
import java.util.List;
import ctrmap.formats.pokemon.gen5.area.VAreaHeader;
import ctrmap.formats.pokemon.gen5.area.AreaPatAnimeData;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildingResource;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildings;
import ctrmap.missioncontrol_base.debug.IMCDebuggable;
import ctrmap.missioncontrol_ntr.field.VFieldController;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import ctrmap.missioncontrol_ntr.field.debug.VAreaDebugger;
import ctrmap.missioncontrol_ntr.field.map.BuildingTexturePack;
import xstandard.fs.FSFile;

public class VArea implements IMCDebuggable<VAreaDebugger> {

	public int id;
	public VAreaHeader header;

	private FSFile propTexFile;

	private NSBTXDataBlock prop_TEX0;
	private NSBTXDataBlock world_TEX0;
	public AreaPatAnimeData worldAnimations_APAD;

	public BuildingTexturePack propTextures;
	public G3DResource worldTextures;

	public G3DResource worldAnimationsSRT;
	public G3DResource worldAnimationsPat;

	public AreaBuildings bmReg;

	private VFieldController ctrl;
	private GameInfo game;

	public VArea() {
		id = -1;
	}

	public VArea(NTRGameFS fs, VFieldController ctrl, int areaID) {
		id = areaID;
		this.ctrl = ctrl;
		game = ctrl.mc.game;
		if (areaID >= 0 && areaID < ctrl.areaTable.getAreaCount()) {
			header = ctrl.areaTable.getHeader(areaID);
			loadSequence(fs);
		}
	}

	public void free() {
		if (ctrl != null) {
			ctrl.mc.getDebuggerManager().unregistDebuggable(this);
		}
		header = null;
		propTexFile = null;
		prop_TEX0 = null;
		world_TEX0 = null;
		worldAnimations_APAD = null;
		propTextures = null;
		worldTextures = null;
		worldAnimationsSRT = null;
		worldAnimationsPat = null;
		bmReg = null;
		ctrl = null;
		game = null;
	}

	public void forceFullReload(NTRGameFS fs) {
		loadSequence(fs);
	}

	public void finalizeLoad(VMap map) {
		if (world_TEX0 != null) {
			List<Material> worldMats = new ArrayList<>();
			for (ModelInstance mdl : map.models) {
				if (mdl != null) {
					for (Model model : mdl.resource.models) {
						worldMats.addAll(model.materials);
					}
				}
				worldAnimationsPat = worldAnimations_APAD == null ? new G3DResource() : worldAnimations_APAD.toGeneric(worldMats);
			}
			worldTextures = world_TEX0.toGeneric(worldMats);
		}
		if (prop_TEX0 != null && bmReg != null) {
			List<Material> bldMats = new ArrayList<>();
			for (AreaBuildingResource mdl : bmReg.buildings) {
				for (Model model : mdl.models) {
					bldMats.addAll(model.materials);
				}
			}
			propTextures = new BuildingTexturePack(propTexFile, prop_TEX0.toGeneric(bldMats));
		}

		mergeToFieldScene(map);

		if (ctrl != null) {
			ctrl.mc.getDebuggerManager().registDebuggable(this);
		}
	}

	public void mergeToFieldScene(VMap map) {
		for (ModelInstance mdl : map.models) {
			if (mdl != null) {
				MaterialProcessor.setAutoAlphaBlendByTexture(mdl.resource, worldTextures);
			}
		}
		if (ctrl != null && ctrl.isOmniMatrixLoad) {
			MaterialProcessor.setAutoAlphaBlendByTexture(map.omniChunk, worldTextures);
		}
		if (bmReg != null) {
			for (AreaBuildingResource rsc : bmReg.buildings) {
				MaterialProcessor.setAutoAlphaBlendByTexture(rsc, propTextures);
			}
		}

		if (map.terrainScene != null) {
			map.terrainScene.resource.textures.clear();
			map.terrainScene.merge(worldTextures);
			map.terrainScene.merge(worldAnimationsPat);
			map.terrainScene.merge(worldAnimationsSRT);
			map.terrainScene.playResourceAnimations();
		}

		if (map.propsScene != null) {
			map.propsScene.setResource(propTextures);
		}
	}

	private void loadSequence(NTRGameFS fs) {
		int buildingsId = getBuildingsID();
		propTexFile = fs.NARCGet(header.isExterior ? NARCRef.FIELD_BMODEL_TEX_EXT : NARCRef.FIELD_BMODEL_TEX_INT, buildingsId);
		world_TEX0 = new NSBTX(fs.NARCGet(NARCRef.FIELD_MAP_TEX, header.texturesId)).TEX0;
		prop_TEX0 = new NSBTX(propTexFile).TEX0;
		if (header.srtAnimeIdx != 255) {
			worldAnimationsSRT = new NSBTA(fs.NARCGet(NARCRef.FIELD_MAP_ANM_SRT, header.srtAnimeIdx)).toGeneric();
		} else {
			worldAnimationsSRT = new G3DResource();
		}
		if (header.patAnimeIdx != 255) {
			worldAnimations_APAD = new AreaPatAnimeData(fs.NARCGet(NARCRef.FIELD_MAP_ANM_PAT, header.patAnimeIdx));
		} else {
			worldAnimations_APAD = null;
		}
		bmReg = new AreaBuildings(new DefaultGamefreakContainer(fs.NARCGet(header.isExterior ? NARCRef.FIELD_BMODEL_BUNDLE_EXT : NARCRef.FIELD_BMODEL_BUNDLE_INT, buildingsId), "AB"));
	}

	private int getBuildingsID() {
		if (game.isBW2()) {
			return header.buildingsId;
		} else {
			return getBuildingsIDByAreaBW(id);
		}
	}

	public static int getBuildingsIDByAreaBW(int areaID) {
		if (areaID >= 210) {
			return areaID - 210;
		} else if (areaID >= 2) {
			/*int r1 = areaID - 2;
			r1 = r1 >> 1;
			r1 = r1 >>> 0x1E;
			r1 += areaID;
			r1 <<= 14;
			r1 >>= 16;
			return r1;*/ //yeah no clue how GF came up with this thing. might have been bad division compiler optimization?
			return (areaID - 2) / 4;
		} else {
			return 0;
		}
	}

	public static boolean isAreaIdHasSeasons(int areaID, GameInfo game) {
		if (game.isBW2()) {
			return areaID >= 2 && areaID < 282;
		} else {
			return areaID >= 2 && areaID < 210;
		}
	}

	@Override
	public Class<VAreaDebugger> getDebuggerClass() {
		return VAreaDebugger.class;
	}

	@Override
	public void attach(VAreaDebugger debugger) {
		debugger.loadArea(this);
	}

	@Override
	public void detach(VAreaDebugger debugger) {

	}

	@Override
	public void destroy(VAreaDebugger debugger) {
		debugger.loadArea(null);
	}
}
