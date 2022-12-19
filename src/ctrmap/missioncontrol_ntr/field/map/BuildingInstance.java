package ctrmap.missioncontrol_ntr.field.map;

import ctrmap.formats.pokemon.WorldObject;
import ctrmap.renderer.scene.model.ModelInstance;
import xstandard.math.vec.Vec3f;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildingResource;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildings;
import ctrmap.formats.pokemon.gen5.buildings.ChunkBuilding;
import ctrmap.missioncontrol_ntr.VRTC;
import ctrmap.renderer.scene.animation.AbstractAnimation;

/**
 *
 */
public class BuildingInstance extends ModelInstance implements WorldObject {

	private AreaBuildings bmReg;

	private AreaBuildingResource parentRsc;
	private AreaBuildingResource bmRsc;

	private ChunkBuilding bld;

	public BuildingInstance doorInstance;

	private boolean isDoor = false;
	
	private boolean allowAllTimeAnm = true;

	private final AreaBuildingResource.ABResourceListener doorChgListener = new AreaBuildingResource.ABResourceListener() {
		@Override
		public void onDoorUIDChanged(int newUID) {
			setUpResourceToG3D();
		}

		@Override
		public void onUIDChanged(int newUID) {
			if (bmRsc == null || bmRsc.uid != newUID) {
				setResourceID(newUID);
			} else {
				bld.modelUID = newUID; //adjust id of this building to new resource's UID
			}
		}

		@Override
		public void onAnmCntTypeChanged() {
			playAllTimeBmAnimations();
		}
	};

	public BuildingInstance(ChunkBuilding bld, AreaBuildings ab) {
		bmReg = ab;
		this.isDoor = false;
		this.bld = bld;
		setResourceID(bld.modelUID);
	}

	public BuildingInstance(AreaBuildingResource parent, ChunkBuilding bld, AreaBuildings ab) {
		bmReg = ab;
		this.isDoor = true;
		this.bld = bld;
		this.parentRsc = parent;
		setResourceID(bld.modelUID);
	}
	
	public void setAllowAllTimeAnm(boolean val) {
		allowAllTimeAnm = val;
		if (!val) {
			stopAllAnimations();
		}
	}

	public void freeRsc() {
		if (bmRsc != null) {
			bmRsc.removeListener(doorChgListener);
		}
	}

	public final void setResourceID(int uid) {
		bld.modelUID = uid;
		if (bmReg != null) {
			freeRsc();
			bmRsc = bmReg.getResourceByUniqueID(bld.modelUID);
			if (bmRsc == null) {
				System.out.println("INVALID RESOURCEID " + uid + " for BM! There are " + bmReg.buildings.size() + " buildings in the AreaBuilding data.");
			} else {
				bmRsc.addListener(doorChgListener);
			}
		} else {
			System.out.println("BUILDING REGISTRY NOT ATTACHED!");
		}
		setUpResourceToG3D();
	}

	public ChunkBuilding getBld() {
		return bld;
	}

	public AreaBuildingResource getBldRes() {
		return bmRsc;
	}

	public void setUpResourceToG3D() {
		setResource(bmRsc);
		clearChildren(true);
		if (bmRsc == null) {
			return;
		}

		if (bmRsc.doorUID != -1) {
			ChunkBuilding doorBld = new ChunkBuilding();
			doorBld.modelUID = bmRsc.doorUID;
			doorInstance = new BuildingInstance(bmRsc, doorBld, bmReg);
			addChild(doorInstance);
		} else {
			doorInstance = null;
		}

		playAllTimeBmAnimations();
	}

	private static final int[] ANMIDX_FOR_DAY_PART = new int[]{0, 1, 2, 3, 3};

	public void playAllTimeBmAnimations() {
		if (!allowAllTimeAnm) {
			return;
		}
		stopAllAnimations();
		if (bmRsc != null) {
			switch (bmRsc.anmCntType) {
				case AMBIENT_GENERIC:
					for (int i = 0; i < bmRsc.anmSetEntryCount; i++) {
						AbstractAnimation a = bmRsc.getConvAnm(i);
						if (a != null) {
							playAnimation(a);
						}
					}
					break;
				case DYNAMIC:
				case NON_ANIMATED:
					break;
				case AMBIENT_RTC:
					int idx = ANMIDX_FOR_DAY_PART[VRTC.DayPart.getRTC().ordinal()];

					for (int i = 0; i < 4; i++) {
						AbstractAnimation a = bmRsc.getConvAnm(i);
						if (i == idx) {
							if (a != null) {
								playAnimation(a);
							}
						} else {
							removeAnimation(a);
						}
					}

					break;
			}
		}
	}

	@Override
	public Vec3f getPosition() {
		if (isDoor) {
			return new Vec3f(parentRsc.doorX, parentRsc.doorY, parentRsc.doorZ);
		}
		return bld.position;
	}

	private Vec3f rot_tmp = new Vec3f();

	@Override
	public Vec3f getRotation() {
		rot_tmp.y = bld.rotation;
		return rot_tmp;
	}

	@Override
	public Vec3f getWPos() {
		return getPosition();
	}

	@Override
	public void setWPos(Vec3f vec) {
		bld.position.set(vec);
	}

	@Override
	public Vec3f getWDim() {
		if (resource == null) {
			return new Vec3f(10f, 10f, 10f);
		}
		return resource.getDimVector();
	}

	@Override
	public void setWDim(Vec3f vec) {

	}

	@Override
	public Vec3f getMinVector() {
		if (resource == null) {
			return new Vec3f();
		}
		return resource.boundingBox.min;
	}

	@Override
	public float getRotationY() {
		return bld.rotation;
	}
}
