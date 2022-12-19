package ctrmap.editor.gui.editors.gen5.level.building;

import ctrmap.editor.gui.editors.common.components.Custom3DPreview;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildingResource;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildings;
import ctrmap.formats.pokemon.gen5.buildings.ChunkBuilding;
import ctrmap.missioncontrol_ntr.field.map.BuildingInstance;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;

public class VBuildingPreviewPanel extends Custom3DPreview {

	private AreaBuildings ab;

	private BuildingInstance bi;

	private boolean anmEnableAuto = true;

	private G3DResourceInstance texParentDmy = new G3DResourceInstance();

	public VBuildingPreviewPanel() {
		super();
		texParentDmy.setPersistent(true);
		getScene().addChild(texParentDmy);
	}

	@Override
	protected G3DResourceInstance getModel() {
		return bi;
	}

	public void setAnmEnable(boolean v) {
		anmEnableAuto = v;
		if (!anmEnableAuto) {
			clearAnime();
		} else if (bi != null) {
			bi.playAllTimeBmAnimations();
		}
	}

	@Override
	public void clear() {
		super.clear();
		showBuilding(null);
	}

	public void loadBuildingList(AreaBuildings ab, G3DResource bmTextures) {
		this.ab = ab;
		if (ab != null) {
			texParentDmy.setResource(bmTextures);
			mergeSceneResource(bmTextures);
			texParentDmy.removeChild(bi);
			bi = new BuildingInstance(new ChunkBuilding(), ab);
			bi.setAllowAllTimeAnm(false);
		}
		else {
			texParentDmy.clear();
			bi = null;
		}
	}

	public void bindSelectPanel(VBuildingSelectJList panel) {
		panel.addBuildingSelectionListener(new VBuildingSelectJList.BuildingSelectionListener() {
			@Override
			public void onBuildingRscSelected(int selectedBmResID) {
				showBuilding(ab.getResourceByUniqueID(selectedBmResID));
			}
		});
	}

	private void showBuilding(AreaBuildingResource rsc) {
		clearAnime();

		if (rsc != null) {
			bi.setResourceID(rsc.uid);
			if (!anmEnableAuto) {
				bi.stopAllAnimations();
			}

			texParentDmy.addChild(bi);

			setCamera(rsc);
		} else {
			texParentDmy.removeChild(bi);
		}
	}
}
