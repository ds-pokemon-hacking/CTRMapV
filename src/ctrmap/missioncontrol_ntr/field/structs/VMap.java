package ctrmap.missioncontrol_ntr.field.structs;

import ctrmap.formats.common.collision.ICollisionProvider;
import ctrmap.formats.pokemon.containers.DefaultGamefreakContainer;
import ctrmap.formats.pokemon.containers.GFContainer;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMD;
import ctrmap.formats.pokemon.gen5.zone.entities.VGridObject;
import ctrmap.renderer.scenegraph.G3DResInstanceList;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.model.ModelInstance;
import xstandard.util.ResizeableMatrix;
import ctrmap.formats.pokemon.gen5.buildings.ChunkBuilding;
import ctrmap.formats.pokemon.gen5.buildings.ChunkBuildings;
import ctrmap.formats.pokemon.gen5.camera.VAbstractCameraData;
import ctrmap.formats.pokemon.gen5.camera.VCameraDataCircle;
import ctrmap.formats.pokemon.gen5.camera.VCameraDataRect;
import ctrmap.formats.pokemon.gen5.camera.VCameraDataFile;
import ctrmap.formats.pokemon.gen5.mapmatrix.VMapMatrix;
import ctrmap.formats.pokemon.gen5.mapmatrix.VMatrixCameraBoundaries;
import ctrmap.formats.pokemon.gen5.rail.RailData;
import ctrmap.formats.pokemon.gen5.terrain.VMapTerrain;
import ctrmap.missioncontrol_base.debug.IMCDebuggable;
import ctrmap.missioncontrol_ntr.field.FieldOmnimodel;
import ctrmap.missioncontrol_ntr.field.rail.FieldRailLoader;
import ctrmap.missioncontrol_ntr.field.VFieldController;
import ctrmap.missioncontrol_ntr.field.debug.VMapDebugger;
import ctrmap.missioncontrol_ntr.field.map.BuildingInstance;
import ctrmap.missioncontrol_ntr.field.map.VMapSamplerUtil;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import xstandard.math.FAtan;
import xstandard.math.vec.Vec3f;

public class VMap implements ICollisionProvider, IMCDebuggable<VMapDebugger> {

	public static final int MAPPACK_TERRAINMDL_IDX = 0;

	public int matrixId = -1;
	public int matrixCameraBoundaryId = -1;
	public int matrixZoneId = -1;

	public VMapMatrix matrix;
	public VMatrixCameraBoundaries cameraBoundaries;

	public RailData rails;
	public VCameraDataFile cameras;

	public ResizeableMatrix<GFContainer> chunks = new ResizeableMatrix<>(0, 0, null);

	public ResizeableMatrix<ModelInstance> models = new ResizeableMatrix<>(0, 0, null);

	public G3DResInstanceList<BuildingInstance> buildings = new G3DResInstanceList<>(null);

	public ResizeableMatrix<VMapTerrain> terrain = new ResizeableMatrix<>(0, 0, null);

	public Scene terrainScene;
	public Scene propsScene;
	public Scene worldScene = new Scene("Map_WorldScene");

	public VFieldController ctrl;

	public float chunkSpan;

	public final FieldOmnimodel omniChunk = new FieldOmnimodel();
	public final FieldOmnimodel omniBld = new FieldOmnimodel();

	public VMap() {
		matrixId = -1;
	}

	public VMap(NTRGameFS fs, VZone zone, VFieldController ctrl) {
		this.ctrl = ctrl;

		loadSequence(fs, zone, true);
	}

	public void free() {
		if (ctrl != null) {
			ctrl.mc.getDebuggerManager().unregistDebuggable(this);
		}
		matrix = null;
		cameraBoundaries = null;
		rails = null;
		cameras = null;
		chunks = null;
		models = null;
		buildings = null;
		terrain = null;
		terrainScene = null;
		worldScene = null;
		propsScene = null;
		ctrl = null;
	}

	public void forceFullReload(NTRGameFS fs) {
		loadSequence(fs, null, true);
	}

	public void changeZone(NTRGameFS fs, VZone zone) {
		if (matrixZoneId != zone.id) {
			loadSequence(fs, zone, false);
		}
	}

	private void loadSequence(NTRGameFS fs, VZone zone, boolean forceReload) {
		int newMatrixId = matrixId;
		int newMatrixZoneId = matrixZoneId;
		int newMatrixCamBoundId = matrixCameraBoundaryId;

		if (zone != null) {
			newMatrixId = ctrl.resolveMatrixID(zone.header.matrixID);
			newMatrixZoneId = zone.id;
			newMatrixCamBoundId = zone.header.matrixCamBoundaryIndex;
		}

		if (forceReload || newMatrixId != matrixId) {
			matrixId = newMatrixId;
			if (matrixId < fs.NARCGetDataMax(NARCRef.FIELD_MAP_MATRIX)) {
				matrix = new VMapMatrix(fs.NARCGet(NARCRef.FIELD_MAP_MATRIX, matrixId));
			} else {
				matrix = new VMapMatrix();
			}
		}
		worldScene.name = "Map" + matrixId + "_WorldScene";
		if (forceReload || newMatrixCamBoundId != matrixCameraBoundaryId) {
			matrixCameraBoundaryId = newMatrixCamBoundId;
			if (newMatrixCamBoundId != -1) {
				cameraBoundaries = new VMatrixCameraBoundaries(fs.NARCGet(NARCRef.FIELD_CAMERA_BOUNDARIES, matrixCameraBoundaryId));
			} else {
				cameraBoundaries = null;
			}
		}
		if (forceReload || newMatrixZoneId != matrixZoneId) {
			FieldRailLoader.RailHeader railHeader = ctrl.railLoader.loadRailData(newMatrixZoneId);
			if (railHeader != null) {
				rails = new RailData(
					fs.NARCGet(NARCRef.FIELD_RAIL_DATA, railHeader.railFile),
					fs.NARCGet(NARCRef.FIELD_RAIL_DATA, railHeader.railFile + (fs.NARCGetDataMax(NARCRef.FIELD_RAIL_DATA) >> 1))
				);

				if (railHeader.cameraDataIdx != -1) {
					cameras = ctrl.camLoader.loadNoGridCameraData(railHeader.cameraDataIdx);
				}
			} else {
				cameras = ctrl.camLoader.loadCameraData(newMatrixZoneId);
			}
		}

		matrixZoneId = newMatrixZoneId;

		chunks = new ResizeableMatrix<>(matrix.getWidth(), matrix.getHeight(), null);
		//if (models == null) {
		models = new ResizeableMatrix<>(matrix.getWidth(), matrix.getHeight(), null);
		//}
		buildings.clear();
		terrain = new ResizeableMatrix<>(matrix.getWidth(), matrix.getHeight(), null);

		if (zone != null) {
			chunkSpan = ctrl.mapConfigs.getChunkSpan(zone.header.mapType);
		}

		for (int x = 0; x < matrix.getWidth(); x++) {
			for (int y = 0; y < matrix.getHeight(); y++) {
				if (matrix.chunkIds.get(x, y) != -1) {
					if (!ctrl.isOmniMatrixLoad && matrix.hasZones && matrix.zoneIds.get(x, y) != matrixZoneId) {
						continue;
					}
					int baseChunkId = matrix.chunkIds.get(x, y);
					int replacedChunkId = ctrl.resolveChunkID(matrixId, baseChunkId);

					GFContainer source = new DefaultGamefreakContainer(fs.NARCGet(NARCRef.FIELD_MAP_CHUNKS, replacedChunkId));
					chunks.set(x, y, source);

					ModelInstance chunk = new NSBMD(source.getFile(MAPPACK_TERRAINMDL_IDX)).toGeneric().createInstance();

					chunk.p.x = x * chunkSpan + chunkSpan / 2f;
					chunk.p.z = y * chunkSpan + chunkSpan / 2f;

					switch (source.getSignature()) {
						case "WB":
							terrain.set(x, y, new VMapTerrain(source.getFile(1)));
							break;
					}

					ChunkBuildings blds = new ChunkBuildings(source.getFile(source.getFileCount() - 1));
					for (ChunkBuilding bi : blds.buildings) {
						bi.position.x += chunk.p.x;
						bi.position.z = chunk.p.z - bi.position.z;
						if (ctrl.isOmniMatrixLoad) {
							BuildingInstance mainBld = new BuildingInstance(bi, ctrl.area.bmReg);
							omniBld.mergeModelInst(mainBld);
							if (mainBld.doorInstance != null) {
								mainBld.doorInstance.p = mainBld.getPosition();
								omniBld.mergeModelInst(mainBld.doorInstance);
							}
						} else {
							buildings.add(new BuildingInstance(bi, ctrl.area.bmReg));
						}
					}

					if (ctrl.isOmniMatrixLoad) {
						omniChunk.mergeModelInst(chunk);
					} else {
						models.set(x, y, chunk);
					}
				}
			}
		}

		if (ctrl.isOmniMatrixLoad) {
			omniChunk.finish();
			omniBld.finish();
		}

		ctrl.mc.getDebuggerManager().registDebuggable(this);

		buildScene();
	}

	private void buildScene() {
		worldScene.clear();

		terrainScene = new Scene("TerrainScene");
		propsScene = new Scene("PropScene");

		propsScene.setResource(ctrl.area.propTextures);

		for (int x = 0; x < matrix.getWidth(); x++) {
			for (int y = 0; y < matrix.getHeight(); y++) {
				if (models.get(x, y) != null) {
					terrainScene.addModel(models.get(x, y));
				}
			}
		}
		if (ctrl.isOmniMatrixLoad) {
			terrainScene.addChild(omniChunk.createInstance());
			ModelInstance ob = omniBld.createInstance();
			ob.playResourceAnimations();
			propsScene.addChild(ob);
		} else {
			propsScene.setChildren(buildings);
		}

		worldScene.addScene(terrainScene);
		worldScene.addScene(propsScene);
	}

	public void getTerrainAtWorldLoc(VMapSamplerUtil.MapTerrainBuf dest, float x, float lastY, float z) {
		int chunkX = (int) (x / chunkSpan);
		int chunkZ = (int) (z / chunkSpan);

		if (terrain.containsPoint(chunkX, chunkZ)) {
			VMapTerrain colls = terrain.get(chunkX, chunkZ);

			if (colls != null) {
				x %= chunkSpan;
				z %= chunkSpan;
				x -= (chunkSpan * 0.5f);
				z -= (chunkSpan * 0.5f);
				VMapSamplerUtil.sampleHeight(dest, colls, x, z, chunkSpan, 0f);
			}
		}
	}

	@Override
	public float getHeightAtWorldLoc(float x, float lastY, float z) {
		/*int chunkX = (int) (x / chunkSpan);
		int chunkZ = (int) (z / chunkSpan);

		if (generatedCollisions.containsPoint(chunkX, chunkZ)) {
			GFCollisionFile colls = generatedCollisions.get(chunkX, chunkZ);

			if (colls != null) {
				x %= chunkSpan;
				z %= chunkSpan;
				x -= (chunkSpan * 0.5f);
				z -= (chunkSpan * 0.5f);
				if (colls.hasHeightAtPoint(x, z)) {
					return colls.getHeightAtPoint(x, lastY, z);
				}
			}
		}

		return lastY;*/
		VMapSamplerUtil.MapTerrainBuf buf = new VMapSamplerUtil.MapTerrainBuf();
		getTerrainAtWorldLoc(buf, x, lastY, z);
		return buf.heightY;
	}

	public VAbstractCameraData getCamAtWorldLoc(float x, float z) {
		int gx = VGridObject.worldToTile(x);
		int gz = VGridObject.worldToTile(z);

		if (cameras != null) {
			for (VAbstractCameraData c : cameras.entries) {
				switch (c.getType()) {
					case RECTANGLE:
						VCameraDataRect rect = (VCameraDataRect) c;
						if (gx >= rect.gridX && gx < rect.gridX + rect.gridW && gz >= rect.gridZ && gz < rect.gridZ + rect.gridH) {
							return c;
						}
						break;
					case CIRCLE:
						VCameraDataCircle circ = (VCameraDataCircle) c;
						Vec3f diff = new Vec3f();
						circ.centre.sub(x, circ.centre.y, z, diff);
						float length = diff.length();
						if (length >= circ.radiusStart && length <= circ.radiusEnd) {
							float angle = FAtan.atan2(diff.x, diff.z);
							boolean isEndGreater = circ.angleEnd > circ.angleStart;
							float lower = isEndGreater ? circ.angleStart : circ.angleEnd;
							float upper = isEndGreater ? circ.angleEnd : circ.angleStart;
							if (angle >= lower && angle <= upper) {
								return c;
							}
						}
						break;
				}
			}
		}

		return null;
	}

	public void reattachDebuggers() {
		ctrl.mc.getDebuggerManager().reattachDebuggers(this);
	}

	@Override
	public Class<VMapDebugger> getDebuggerClass() {
		return VMapDebugger.class;
	}

	@Override
	public void attach(VMapDebugger debugger) {
		debugger.loadMapMatrix(this);
	}

	@Override
	public void detach(VMapDebugger debugger) {

	}

	@Override
	public void destroy(VMapDebugger debugger) {
		debugger.loadMapMatrix(null);
	}
}
