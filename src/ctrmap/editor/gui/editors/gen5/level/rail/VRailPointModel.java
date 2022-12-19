package ctrmap.editor.gui.editors.gen5.level.rail;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.util.generators.BoundingBoxGenerator;
import xstandard.math.vec.RGBA;
import ctrmap.formats.pokemon.gen5.rail.RailPoint;

public class VRailPointModel extends G3DResourceInstance {

	private RailPoint point;

	private RGBA highLightColor = new RGBA(RGBA.BLACK);
	
	public VRailPointModel(RailPoint point) {
		this.point = point;

		createRendererData();
	}

	public void setHighLightEnable(boolean value){
		if (value){
			highLightColor.set(RGBA.RED);
		}
		else {
			highLightColor.set(RGBA.BLACK);
		}
	}

	private void createRendererData() {
		Mesh posMesh = BoundingBoxGenerator.generateBBox(9, 9, 9, true, false, 0, new RGBA(0, 0, 255, 255));
		
		posMesh.hasColor = true;
		
		Material mat = new Material();
		mat.name = "mat";
		posMesh.materialName = mat.name;
		mat.constantColors[0] = highLightColor;
		mat.tevStages.stages[0].constantColor = MaterialColorType.CONSTANT0;
		mat.tevStages.stages[0].rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.ADD;
		mat.tevStages.stages[0].rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR;
		mat.tevStages.stages[0].rgbSource[1] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;

		Model posModel = new Model();
		posModel.addMesh(posMesh);
		posModel.addMaterial(mat);

		G3DResourceInstance pos = new G3DResourceInstance();
		pos.resource.addModel(posModel);
		pos.p = point.position;
		
		addChild(pos);
	}
}
