package ctrmap.missioncontrol_ntr.field.structs;

import ctrmap.formats.ntr.common.FX;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scenegraph.G3DResInstanceList;
import ctrmap.scriptformats.gen5.VScriptFile;
import ctrmap.formats.pokemon.gen5.zone.VZoneHeader;
import ctrmap.formats.pokemon.gen5.zone.entities.VNPC;
import ctrmap.formats.pokemon.gen5.zone.entities.VZoneEntities;
import ctrmap.formats.pokemon.gen5.zone.entities.VZoneInitScriptDispatcher;
import ctrmap.missioncontrol_base.debug.IMCDebuggable;
import ctrmap.missioncontrol_ntr.field.VFieldController;
import ctrmap.missioncontrol_ntr.field.debug.VZoneDebugger;
import ctrmap.missioncontrol_ntr.field.mmodel.FieldActorCamera;
import ctrmap.missioncontrol_ntr.field.mmodel.VMoveModel;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import ctrmap.renderer.scene.Camera;

public class VZone implements IMCDebuggable<VZoneDebugger> {

	public int id;

	public VFieldController controller;

	private NTRGameFS fs;

	public VZoneHeader header;
	public VZoneEntities entities;

	public VScriptFile scripts;
	public VZoneInitScriptDispatcher initScrUnused;

	private final FieldActorCamera npcCamera = new FieldActorCamera();
	public Scene npcScene;

	public G3DResInstanceList<VMoveModel> NPCs = new G3DResInstanceList(null);

	public VZone(VFieldController controller) {
		id = -1;
		npcScene = new Scene("Zone_Dummy_3DScene");
		this.controller = controller;
	}

	public VZone(VFieldController controller, NTRGameFS fs, int zoneID) {
		id = zoneID;
		this.fs = fs;
		npcScene = new Scene("Zone_" + id + "_3DScene");
		this.controller = controller;
		header = controller.zoneTable.getHeader(zoneID);

		loadSequence();
	}

	private void loadSequence() {
		entities = new VZoneEntities(fs.NARCGet(NARCRef.FIELD_ZONE_ENTITIES, header.entitiesID));
		if (header.scriptsID < fs.NARCGetDataMax(NARCRef.FIELD_SCRIPTS)) {
			scripts = new VScriptFile(fs.NARCGet(NARCRef.FIELD_SCRIPTS, header.scriptsID));
		}
		initScrUnused = new VZoneInitScriptDispatcher(fs.NARCGet(NARCRef.FIELD_SCRIPTS, header.initScriptsID));
	}

	public void loadingFinalize() {
		makeNPCs();
		buildNPCScene();
	}

	public void forceFullReload() {
		loadSequence();
		loadingFinalize();
		controller.mc.getDebuggerManager().reattachDebuggers(this);
	}

	private void makeNPCs() {
		NPCs.clear();
		for (VNPC npc : entities.NPCs) {
			VMoveModel newnpc = new VMoveModel(controller, npc);
			newnpc.setLocation(npc.gposX, npc.gposZ, npc.wposY);
			NPCs.add(newnpc);
		}
		controller.player.onZoneLoadSpawn(this);
	}

	public void buildNPCScene() {
		npcCamera.setFrustumZOffset(header.actorProjMatrixType == 1 ? FX.unfx32(494) : FX.unfx32(310));
		npcScene.setChildren(NPCs);
		npcScene.instantiateCamera(npcCamera);
	}

	public void updateActorCamera(Camera fieldCamera) {
		//copy projection matrix
		npcCamera.copyProjection(fieldCamera);
		//transform is inherited from parent scene
	}

	public void registDebuggable() {
		controller.mc.getDebuggerManager().registDebuggable(this);
	}

	public void unregistDebuggable() {
		controller.mc.getDebuggerManager().unregistDebuggable(this);
	}

	@Override
	public Class<VZoneDebugger> getDebuggerClass() {
		return VZoneDebugger.class;
	}

	@Override
	public void attach(VZoneDebugger debugger) {
		debugger.loadZone(this);
	}

	@Override
	public void detach(VZoneDebugger debugger) {

	}

	@Override
	public void destroy(VZoneDebugger debugger) {
		debugger.loadZone(null);
	}

	public enum LoadMethod {
		COLD_BOOT,
		SAVE_DATA,
		WARP,
		TRANSITION
	}
}
