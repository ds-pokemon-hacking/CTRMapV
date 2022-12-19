package ctrmap.missioncontrol_ntr.field.mmodel;

import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMD;
import ctrmap.formats.ntr.nitroreader.nsbtx.NSBTX;
import ctrmap.formats.ntr.nitroreader.nsbtx.NSBTXDataBlock;
import ctrmap.formats.pokemon.IScriptObject;
import xstandard.math.vec.Vec3f;
import ctrmap.formats.pokemon.gen5.zone.entities.VGridObject;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.math.vec.Vec2f;
import java.awt.Dimension;
import java.awt.Point;
import ctrmap.formats.pokemon.gen5.npcreg.VNPCRegistry;
import ctrmap.formats.pokemon.gen5.rail.RailLine;
import ctrmap.formats.pokemon.gen5.zone.entities.VNPC;
import ctrmap.formats.pokemon.gen5.zone.entities.VZoneEntities;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;
import ctrmap.missioncontrol_ntr.field.VFieldController;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.texturing.Texture;

public class VMoveModel extends ModelInstance implements VGridObject, IScriptObject {

	private static final int[] MMDL_BILLBOARD_SIZES = new int[]{
		32, 32,
		16, 16,
		64, 64
	};

	public static final Model[] MMDL_PLANE_TEMPLATES = new Model[]{
		generateNPCPlane(0),
		generateNPCPlane(1),
		generateNPCPlane(2)
	};

	private VFieldController fieldController;

	private VNPCRegistry.Entry regEntry;
	private TextureMapper tm = null;
	public VNPC NPCData;
	
	public VMoveModel(VFieldController controller, VNPC data) {
		fieldController = controller;
		NPCData = data;
		loadResource();
	}

	private void loadResource() {
		if (NPCData != null && fieldController != null) {
			int objCode = fieldController.mc.game.isBW2() ? OBJIDNormalizer.getNormalizedOBJCODE_BW2(NPCData.objCode) : OBJIDNormalizer.getNormalizedOBJCODE_BW1(NPCData.objCode);
			if (objCode < 0 || objCode >= fieldController.npcTable.entries.size()) {
				System.err.println("Could not resolve a valid OBJCODE for " + NPCData.objCode);
				return;
			}
			regEntry = fieldController.npcTable.entries.get(objCode);

			int resId = regEntry.resourceIndices[0];

			if (resId < (fieldController.mc.game.isBW2() ? 7 : 6)) {
				setResource(new NSBMD(fieldController.mc.fs.NARCGet(NARCRef.FIELD_MMODEL_RES, resId).getBytes()).toGeneric());
			} else {
				Model mdl = new Model();
				mdl.name = "MMDL" + regEntry.uid;
				for (Mesh mesh : MMDL_PLANE_TEMPLATES[regEntry.billboardSize].meshes) {
					mdl.addMesh(mesh);
				}
				for (Joint j : MMDL_PLANE_TEMPLATES[regEntry.billboardSize].skeleton) {
					mdl.skeleton.addJoint(new Joint(j));
				}
				Material mat = new Material();
				mat.alphaTest.enabled = true;
				mat.name = "VMoveModel_Material";
				mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.TEX0);
				mdl.addMaterial(mat);
				setResource(new G3DResource(mdl));
				NSBTXDataBlock src = NSBTX.readFromBytes(fieldController.mc.fs.NARCGet(NARCRef.FIELD_MMODEL_RES, resId).getBytes()).TEX0;
				String pltName = src.palettes.get(0).name;

				for (int i = 0; i < src.textures.size(); i++) {
					String name = src.textures.get(i).name;
					Texture tex = src.getConvTexture(name, pltName);
					if (tex != null) {
						tex.name = mdl.name + "_" + name;
						resource.textures.add(tex);
					}
				}

				tm = new TextureMapper("DummyTex");
				tm.textureMagFilter = MaterialParams.TextureMagFilter.NEAREST_NEIGHBOR;
				tm.textureMinFilter = MaterialParams.TextureMinFilter.NEAREST_NEIGHBOR;
				mat.textures.add(tm);
			}
			setOrientation(NPCData.faceDirection);
		}
	}

	public void setModelID(int id) {
		if (NPCData != null && id != NPCData.objCode) {
			NPCData.objCode = id;
			loadResource();
		}
	}

	private static Model generateNPCPlane(int bbSizeType) {
		Model mdl = new Model();
		mdl.name = "mmodel_plane";
		Mesh mesh = new Mesh();
		mesh.name = "mmdl_plane_mesh";
		mesh.primitiveType = PrimitiveType.QUADS;
		mesh.skinningType = Mesh.SkinningType.SMOOTH;
		mesh.materialName = "VMoveModel_Material";

		Vertex tl = new Vertex();
		Vertex tr = new Vertex();
		Vertex bl = new Vertex();
		Vertex br = new Vertex();

		float dimX = MMDL_BILLBOARD_SIZES[bbSizeType * 2] >> 1;
		float dimY = MMDL_BILLBOARD_SIZES[bbSizeType * 2 + 1];

		tl.position = new Vec3f(-dimX, dimY, 0f);
		tr.position = new Vec3f(dimX, dimY, 0f);
		bl.position = new Vec3f(-dimX, 0f, 0f);
		br.position = new Vec3f(dimX, 0f, 0f);
		tl.uv[0] = new Vec2f(0, 1);
		tr.uv[0] = new Vec2f(1, 1);
		bl.uv[0] = new Vec2f(0, 0);
		br.uv[0] = new Vec2f(1, 0);
		tl.boneIndices.add(0);
		tr.boneIndices.add(0);
		bl.boneIndices.add(0);
		br.boneIndices.add(0);
		tl.weights.add(1f);
		tr.weights.add(1f);
		bl.weights.add(1f);
		br.weights.add(1f);
		mesh.vertices.add(bl);
		mesh.vertices.add(br);
		mesh.vertices.add(tr);
		mesh.vertices.add(tl);

		Joint j = new Joint();
		j.name = "MMDL_BillboardJoint";
		j.flags = Joint.BB_AXIS_X | Joint.BB_AXIS_Y;

		mdl.skeleton.addJoint(j);

		mesh.hasUV[0] = true;
		mesh.hasBoneIndices = true;
		mesh.hasBoneWeights = true;

		mdl.addMesh(mesh);
		return mdl;
	}

	public VFieldController getField() {
		return fieldController;
	}

	public int getEntityUID() {
		return NPCData.uid;
	}

	public int getModelUID() {
		return NPCData.objCode;
	}

	@Override
	public void setGPos(Point p) {
		this.p.x = p.x * VFieldConstants.TILE_REAL_SIZE + VFieldConstants.TILE_REAL_SIZE_HALF;
		this.p.z = p.y * VFieldConstants.TILE_REAL_SIZE - VFieldConstants.TILE_REAL_SIZE_HALF + VFieldConstants.TILE_REAL_SIZE;
		if (fieldController != null) {
			//		float z = fieldController.map.getHeightAtWorldLoc(this.p.x, this.p.y);
			//		this.p.y = z;
			if (NPCData != null) {
				NPCData.gposX = p.x;
				NPCData.gposZ = p.y;
			}
		}
	}

	private static class SpriteSelectionType {

		public final int idleAnmFrame;
		public final int perDirStep;
		public final boolean isLRMirror;

		public SpriteSelectionType(int perDirStep, boolean isLRMirror) {
			this(0, perDirStep, isLRMirror);
		}

		public SpriteSelectionType(int idleAnmFrame, int perDirStep, boolean isLRMirror) {
			this.idleAnmFrame = idleAnmFrame;
			this.perDirStep = perDirStep;
			this.isLRMirror = isLRMirror;
		}
	}

	private static final SpriteSelectionType[] DIR_STEPS_BY_TYPE = new SpriteSelectionType[]{
		new SpriteSelectionType(0, false),
		new SpriteSelectionType(3, false), //walk+run
		new SpriteSelectionType(2, true),
		new SpriteSelectionType(3, false), //walk only
		new SpriteSelectionType(0, false), //1 frame all directions
		new SpriteSelectionType(1, false),
		new SpriteSelectionType(12, 3, false), //cycling - separate idle
		new SpriteSelectionType(0, false), //anm item get
		new SpriteSelectionType(0, false), //anm pokecen
		new SpriteSelectionType(0, false), //anm report
		new SpriteSelectionType(4, true), //anm fishing
		new SpriteSelectionType(0, false), //anm fieldwaza
		new SpriteSelectionType(4, false), //anm plank balance
		new SpriteSelectionType(2, true), //pokemon
		new SpriteSelectionType(2, false), //pokemon
		new SpriteSelectionType(2, true), //+counter lady/dude anm
		new SpriteSelectionType(3, false), //Cheren
		new SpriteSelectionType(3, false), //Bianca
		new SpriteSelectionType(3, false), //N
		new SpriteSelectionType(3, false), //Ghetsis
		new SpriteSelectionType(3, false), //Ninjas
		new SpriteSelectionType(3, false), //Alder
		new SpriteSelectionType(0, false), //cobweb
		new SpriteSelectionType(0, false), //Meloetta animation
		new SpriteSelectionType(0, false), //Legend land
		new SpriteSelectionType(0, false), //Legend action
		new SpriteSelectionType(0, false), //Legend descend/ascend
		new SpriteSelectionType(3, false), //Elesa
		new SpriteSelectionType(3, false), //gym leader idk the name
		new SpriteSelectionType(3, false), //Colress
		new SpriteSelectionType(3, false), //Hugh
		new SpriteSelectionType(3, false), //Ghetsis
		new SpriteSelectionType(2, true), //Mom
		new SpriteSelectionType(2, true), //Mom
		new SpriteSelectionType(0, false), //Legend idle
	};

	public void setOrientation(int dir) {
		if (tm != null && NPCData != null) {
			int typeIdx = regEntry.spriteControllerType;
			if (typeIdx >= DIR_STEPS_BY_TYPE.length) {
				typeIdx = 0;
			}
			NPCData.faceDirection = dir;
			if (DIR_STEPS_BY_TYPE[typeIdx].isLRMirror && dir == 3) {
				dir = 2;
				tm.bindScale.x = -1f;
			} else {
				tm.bindScale.x = 1f;
			}
			int idx = dir * DIR_STEPS_BY_TYPE[typeIdx].perDirStep;
			if (idx >= resource.textures.size()) {
				idx = 0;
			}
			tm.textureName = resource.textures.get(idx).name;
		}
	}

	@Override
	public Point getGPos() {
		return new Point(NPCData.gposX, NPCData.gposZ);
	}

	@Override
	public Dimension getGDimensions() {
		return new Dimension(regEntry.width, regEntry.height);
	}

	public void setWPos(float x, float z) {
		float newY = p.y;
		setWPos(x, z, newY);
	}

	public void setWPos(float x, float z, float y) {
		setWPos(new Vec3f(x, y, z));
	}

	public void setLocation(int gridX, int gridY, float alt) {
		setGPos(new Point(gridX, gridY));
		p.y = alt;
		if (NPCData != null) {
			NPCData.wposY = alt;
		}
	}

	@Override
	public void setAltitude(float value) {
		p.y = value;
		if (NPCData != null) {
			NPCData.wposY = value;
		}
	}

	public void setLocationPrecise(float x, float z) {
		float newY = fieldController.map.getHeightAtWorldLoc(x, p.y, z);
		/*if (Math.abs(newY - p.y) > VFieldConstants.TILE_REAL_SIZE * 4) {
			newY = p.y;
		}*/
		p.x = x;
		p.z = z;
		p.y = newY;
		if (NPCData != null) {
			NPCData.wposY = p.y;
			NPCData.gposX = VGridObject.worldToTile(p.x);
			NPCData.gposZ = VGridObject.worldToTile(p.z);
		}
	}

	@Override
	public boolean getIsDimensionsCentered() {
		return true;
	}

	@Override
	public float getRotationY() {
		return 0f;
	}

	@Override
	public float getAltitude() {
		return p.y;
	}

	@Override
	public float getDimHeight() {
		return resource.boundingBox.max.y - resource.boundingBox.min.y;
	}

	@Override
	public Vec3f getWPos() {
		return getPosition();
	}

	@Override
	public Vec3f getPosition() {
		Vec3f pos = new Vec3f();
		if (NPCData.isPositionRail) {
			int lineIndex = NPCData.railLineNo;
			float railFrontPos = NPCData.railFrontPos;
			float railSidePos = NPCData.railSidePos;

			if (fieldController.map.rails != null && lineIndex >= 0 && lineIndex < fieldController.map.rails.lines.size()) {
				RailLine line = fieldController.map.rails.lines.get(lineIndex);
				pos.set(line.getAbsPositionOnRail(railSidePos, railFrontPos, true));
			}
		} else {
			pos.set(p);
		}
		pos.add(regEntry.wPosOffX, regEntry.wPosOffY, regEntry.wPosOffZ);
		return pos;
	}

	@Override
	public void setWPos(Vec3f vec) {
		Point gpos = VGridObject.wPosToGPosNonRounded(vec);
		setLocation(gpos.x, gpos.y, vec.y);
	}

	@Override
	public void setGDimensions(Dimension dim) {

	}

	@Override
	public Vec3f getWDim() {
		return VGridObject.gDimToWDim(this);
	}

	@Override
	public void setWDim(Vec3f vec) {
		setGDimensions(VGridObject.wDimToGDim(vec));
	}

	@Override
	public Vec3f getMinVector() {
		Vec3f vec = VGridObject.getIsCenteredMinVec(this);
		vec.y = resource.boundingBox.min.y;
		return vec;
	}

	@Override
	public int getSCRID() {
		return NPCData.script;
	}

	@Override
	public void setSCRID(int SCRID) {
		if (NPCData.script != SCRID) {
			NPCData.script = SCRID;
		}
	}

	@Override
	public int getObjectTypeID() {
		return VZoneEntities.VZE_SCROBJ_TYPEID_NPC;
	}
}
