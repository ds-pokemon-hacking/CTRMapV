package ctrmap.editor.gui.editors.gen5.level.rail;

import ctrmap.renderer.scene.Scene;
import java.util.HashMap;
import java.util.Map;
import ctrmap.formats.pokemon.gen5.rail.RailLine;
import ctrmap.formats.pokemon.gen5.rail.RailPoint;
import ctrmap.missioncontrol_ntr.field.structs.VMap;

public class VRailRenderer extends Scene {

	private HashMap<RailLine, VRailLineModel> lineModels = new HashMap<>();
	private HashMap<RailPoint, VRailPointModel> pointModels = new HashMap<>();

	public VRailRenderer() {
		super("RailRenderSystemScene");
	}

	public void setSelectedLine(RailLine line) {
		for (Map.Entry<RailLine, VRailLineModel> e : lineModels.entrySet()) {
			e.getValue().setHighLightEnable(e.getKey() == line);
		}
	}

	public void setSelectedPoint(RailPoint point) {
		for (Map.Entry<RailPoint, VRailPointModel> e : pointModels.entrySet()) {
			e.getValue().setHighLightEnable(e.getKey() == point);
		}
	}

	public void loadMap(VMap map) {
		clear();
		lineModels.clear();
		pointModels.clear();
		if (map != null && map.rails != null) {
			for (RailLine line : map.rails.lines) {
				VRailLineModel mdl = new VRailLineModel(line);
				addChild(mdl);
				lineModels.put(line, mdl);
			}
			/*for (RailPoint point : map.rails.points) {
				VRailPointModel mdl = new VRailPointModel(point);
				addChild(mdl);
				pointModels.put(point, mdl);
			}*/
		}
	}
}
