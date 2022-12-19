package ctrmap.editor.gui.editors.gen5.level.rail;

import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import ctrmap.formats.pokemon.gen5.rail.RailLine;
import ctrmap.formats.pokemon.gen5.rail.RailPoint;

public class VRailLineModel extends G3DResourceInstance {

	public static final float VRLM_HEIGHT_OFF = 0.05f;

	private RailLine line;

	private RGBA highLightColor = new RGBA(RGBA.BLACK);

	public VRailLineModel(RailLine line) {
		this.line = line;

		createRendererData();

		p.y = VRLM_HEIGHT_OFF;
	}

	public void setHighLightEnable(boolean value) {
		if (value) {
			highLightColor.set(RGBA.RED);
		} else {
			highLightColor.set(RGBA.BLACK);
		}
	}

	private void createRendererData() {
		resource.merge(createRailRenderResource());

		/*if (line.getCurve().curveType != RailCurve.RailCurveType.LERP) {
			Mesh curMesh = BoundingBoxGenerator.generateBBox(9, 9, 9, true, false, 0, new RGBA(0, 255, 0, 255));

			curMesh.hasColor = true;

			Material curMat = new Material();
			curMat.name = "mat";
			curMesh.materialName = curMat.name;
			curMat.tevStages.stages[0].constantColor = highLightColor;
			curMat.tevStages.stages[0].rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.ADD;
			curMat.tevStages.stages[0].rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR;
			curMat.tevStages.stages[0].rgbSource[1] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;

			Model posModel = new Model();
			posModel.addMesh(curMesh);
			posModel.addMaterial(curMat);

			G3DResourceInstance cur = new G3DResourceInstance();
			cur.resource.addModel(posModel);
			cur.p = line.getCurve().position;

			addChild(cur);
		}*/
	}

	private G3DResource createRailRenderResource() {
		int railSizeX = line.getTilemapBlock().tiles.getWidth();
		int railSizeY = line.lineTileLength;
		float railSizeXHalf = railSizeX / 2f;
		
		//The rail is spread between the points according to the hori tables
		//The UV coordinates of the tilemap texture are directly mapped to rail-space
		Vec3f[][] sampledPositions = new Vec3f[railSizeX + 1][railSizeY + 1];
		Vec2f[][] sampledUVs = new Vec2f[sampledPositions.length][sampledPositions[0].length];

		float yMax = ((line.getTilemapBlock().tiles.getHeight() - 1) / (float) railSizeY);

		RailPoint p1 = line.getP1();
		RailPoint p2 = line.getP2();

		for (int x = 0; x <= railSizeX; x++) {
			for (int y = 0; y <= railSizeY; y++) {
				sampledPositions[x][y] = new Vec3f((x - railSizeXHalf), y, 0f);
				float xd = (x / (float) railSizeX);
				sampledUVs[x][y] = new Vec2f(
					xd,
					yMax - (y / (float) railSizeY) * yMax
				);
			}
		}

		Mesh mesh = new Mesh();
		mesh.name = line.name + "_mesh";
		mesh.primitiveType = PrimitiveType.QUADS;
		mesh.hasUV[0] = true;

		for (int x = 0; x < sampledPositions.length - 1; x++) {
			for (int y = 0; y < sampledPositions[x].length - 1; y++) {
				Vertex v0 = new Vertex();
				Vertex v1 = new Vertex();
				Vertex v2 = new Vertex();
				Vertex v3 = new Vertex();
				v0.position = sampledPositions[x][y];
				v1.position = sampledPositions[x][y + 1];
				v2.position = sampledPositions[x + 1][y + 1];
				v3.position = sampledPositions[x + 1][y];
				v0.uv[0] = new Vec2f(sampledUVs[x][y]);
				v1.uv[0] = new Vec2f(sampledUVs[x][y + 1]);
				v2.uv[0] = new Vec2f(sampledUVs[x + 1][y + 1]);
				v3.uv[0] = new Vec2f(sampledUVs[x + 1][y]);
				mesh.vertices.add(v0);
				mesh.vertices.add(v1);
				mesh.vertices.add(v2);
				mesh.vertices.add(v3);
			}
		}

		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.RAIL_ID, line.getRails().lines.indexOf(line), true));
		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.POINT1_POS, p1.position, true));
		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.POINT2_POS, p2.position, true));
		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.SLERP_CENTRE, line.getCurve().position, true));
		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.RAIL_DIM_FRONT, (float) line.lineTileLength, true));
		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.RAIL_DIM_SIDE, (float) line.getTilemapBlock().tiles.getWidth(), true));
		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.RAIL_DIM_SIDE_1, p1.getAttachmentByLine(line).width, true));
		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.RAIL_DIM_SIDE_2, p2.getAttachmentByLine(line).width, true));
		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.RAIL_TILE_WIDTH, (float) line.getRails().info.tileDim, true));
		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.CURVE_TYPE, line.getCurve().curveType, true));
		mesh.metaData.putValue(new MetaDataValue(VRailRenderUniforms.RAIL_POS_OFFSET_EX, new Vec3f(), true));

		Material mat = new Material();
		mat.name = line.name + "_rl_mat";
		mat.vertexShaderName = "RailRendererV.vsh";
		mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.TEX0);
		mat.addShaderExtension("RailRendererVObjectSelectionFragmentShader.fsh_ext");

		Texture tilemapTexture = line.getTilemapBlock().createTexture();
		tilemapTexture.name = line.name + "_tilemap_tex";

		TextureMapper tilemapMapper = new TextureMapper(tilemapTexture.name);
		tilemapMapper.textureMagFilter = MaterialParams.TextureMagFilter.NEAREST_NEIGHBOR;
		tilemapMapper.textureMinFilter = MaterialParams.TextureMinFilter.NEAREST_NEIGHBOR;

		mat.textures.add(tilemapMapper);

		mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.TEX0_CCOL_ADD);
		mat.tevStages.stages[0].constantColor = MaterialColorType.CONSTANT0;
		mat.constantColors[0] = highLightColor;

		Model model = new Model();
		mesh.materialName = mat.name;
		model.addMesh(mesh);
		model.addMaterial(mat);

		G3DResource res = new G3DResource();
		res.addTexture(tilemapTexture);
		res.addModel(model);

		return res;
	}
}
