package ctrmap.editor.gui.editors.gen5.level.rail;

import ctrmap.editor.gui.editors.gen5.level.tools.VRailTool;
import ctrmap.renderer.scene.metadata.uniforms.CustomUniformFloat;
import ctrmap.renderer.scene.metadata.uniforms.CustomUniformInt;
import ctrmap.renderer.scene.metadata.uniforms.CustomUniformVec3;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.util.generators.GridGenerator;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import java.util.List;
import ctrmap.formats.pokemon.gen5.rail.RailLine;

public class VRailSelector extends G3DResourceInstance {

	private VRailTool tool;

	public int selectedRail = -1;
	public int selPosX;
	public int selPosY;

	public VRailSelector(VRailTool tool) {
		this.tool = tool;

		merge(createVRailSelectorResource());
	}

	public RailLine getCurrentLine() {
		if (tool.ref.rails != null) {
			List<RailLine> l = tool.ref.rails.lines;
			if (selectedRail >= 0 && selectedRail < l.size()) {
				return l.get(selectedRail);
			}
		}
		return null;
	}
	
	private static final Vec3f ZERO_VEC = Vec3f.ZERO();

	private G3DResource createVRailSelectorResource() {
		Model mdl = GridGenerator.generateGridModel(1f, 0, 1, 2, RGBA.RED, false, true);
		for (Mesh mesh : mdl.meshes){
			for (Vertex vtx : mesh.vertices){
				vtx.position.x -= 0.5f; //center X only
			}
			mesh.createAndInvalidateBuffers();
		}
		for (Material mat : mdl.materials) {
			mat.depthColorMask.depthFunction = MaterialParams.TestFunction.ALWAYS;
			mat.vertexShaderName = "RailRendererV.vsh";
			mat.addShaderExtension("RailRendererVObjectSelectionFragmentShader.fsh_ext"); //retain selection when cursor overlaps selector
		}

		mdl.metaData.putValue(new CustomUniformInt(VRailRenderUniforms.RAIL_ID) {
			@Override
			public int intValue() {
				return tool.isBusyDragging() ? -1 : selectedRail;
			}
		});
		mdl.metaData.putValue(new CustomUniformVec3(VRailRenderUniforms.RAIL_POS_OFFSET_EX) {			
			@Override
			public Vec3f vec3Value() {
				return new Vec3f(selPosX, selPosY, 0f);
			}
		});
		mdl.metaData.putValue(new CustomUniformVec3(VRailRenderUniforms.POINT1_POS) {
			@Override
			public Vec3f vec3Value() {
				RailLine l = getCurrentLine();
				if (l != null) {
					return l.getP1().position;
				}
				return ZERO_VEC;
			}
		});
		mdl.metaData.putValue(new CustomUniformVec3(VRailRenderUniforms.POINT2_POS) {
			@Override
			public Vec3f vec3Value() {
				RailLine l = getCurrentLine();
				if (l != null) {
					return l.getP2().position;
				}
				return ZERO_VEC;
			}
		});
		mdl.metaData.putValue(new CustomUniformVec3(VRailRenderUniforms.SLERP_CENTRE) {
			@Override
			public Vec3f vec3Value() {
				RailLine l = getCurrentLine();
				if (l != null) {
					return l.getCurve().position;
				}
				return ZERO_VEC;
			}
		});
		mdl.metaData.putValue(new CustomUniformFloat(VRailRenderUniforms.RAIL_DIM_FRONT) {
			@Override
			public float floatValue() {
				RailLine l = getCurrentLine();
				if (l != null) {
					return l.lineTileLength;
				}
				return 0f;
			}
		});
		mdl.metaData.putValue(new CustomUniformFloat(VRailRenderUniforms.RAIL_DIM_SIDE) {
			@Override
			public float floatValue() {
				RailLine l = getCurrentLine();
				if (l != null) {
					return l.getTilemapBlock().tiles.getWidth();
				}
				return 0f;
			}
		});
		mdl.metaData.putValue(new CustomUniformFloat(VRailRenderUniforms.RAIL_DIM_SIDE_1) {
			@Override
			public float floatValue() {
				RailLine l = getCurrentLine();
				if (l != null) {
					return l.getP1().getAttachmentByLine(l).width;
				}
				return 0f;
			}
		});
		mdl.metaData.putValue(new CustomUniformFloat(VRailRenderUniforms.RAIL_DIM_SIDE_2) {
			@Override
			public float floatValue() {
				RailLine l = getCurrentLine();
				if (l != null) {
					return l.getP2().getAttachmentByLine(l).width;
				}
				return 0f;
			}
		});
		mdl.metaData.putValue(new CustomUniformFloat(VRailRenderUniforms.RAIL_TILE_WIDTH) {
			@Override
			public float floatValue() {
				if (tool.ref.rails != null){
					return tool.ref.rails.info.tileDim;
				}
				return 0f;
			}
		});
		mdl.metaData.putValue(new CustomUniformInt(VRailRenderUniforms.CURVE_TYPE) {
			@Override
			public int intValue() {
				RailLine l = getCurrentLine();
				if (l != null) {
					return l.getCurve().curveType.ordinal();
				}
				return 0;
			}
		});

		return new G3DResource(mdl);
	}
}
