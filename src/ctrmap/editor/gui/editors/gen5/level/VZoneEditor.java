package ctrmap.editor.gui.editors.gen5.level;

import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import xstandard.gui.components.ComponentUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import ctrmap.missioncontrol_ntr.VRTC;
import ctrmap.formats.pokemon.gen5.zone.VZoneHeader;
import ctrmap.formats.pokemon.gen5.zone.VZoneTable;
import ctrmap.formats.pokemon.gen5.zone.entities.VGridObject;
import ctrmap.formats.pokemon.gen5.zone.extra.ZoneMapEffSkillIndex;
import ctrmap.missioncontrol_ntr.field.VFieldController;
import ctrmap.missioncontrol_ntr.field.debug.VZoneDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VArea;
import ctrmap.missioncontrol_ntr.field.structs.VZone;
import ctrmap.missioncontrol_ntr.field.debug.VAreaDebugger;
import ctrmap.missioncontrol_ntr.field.debug.VFieldDebugger;
import xstandard.gui.DialogUtils;

public class VZoneEditor extends javax.swing.JPanel implements VFieldDebugger, VZoneDebugger, AbstractTabbedEditor, VAreaDebugger {

	private VLevelEditor editors;

	private VFieldController ctrl;
	private VZone zone;
	private VArea area;

	public VZoneEditor(VLevelEditor editors) {
		initComponents();

		this.editors = editors;

		ComponentUtils.setNFValueClass(Integer.class,
			areaID, mtxID, scrID, lvlScrID, textID, encID, entityID, camBounds, npcInfoCache, battleBG,
			bgmA, bgmSp, bgmSm, bgmW, flyX, flyY, flyZ, mapType, mapTransition, locNameDispType,
			unk1, gimmick, diffLevelAdjustment, nameIcon, weather, camera, fog,
			lights, animeSRT, bldID, texID, animeSRT, animePat, lights, outlines, unknown3
		);

		zoneDropdown.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (ctrl != null && editors.store(true)) {
					ctrl.mc.season = VRTC.Season.values()[seasonBox.getSelectedIndex()];
					ctrl.zoneLoad(zoneDropdown.getValueCB());
				}
			}
		});

		seasonBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ctrl != null && editors.store(true)) {
					ctrl.mc.season = VRTC.Season.values()[seasonBox.getSelectedIndex()];
					if (area != null) {
						if (VArea.isAreaIdHasSeasons(area.id, ctrl.mc.game)) {
							ctrl.zoneLoad(zoneDropdown.getValueCB());
						}
					}
				}
			}
		});
	}

	@Override
	public void attachField(VFieldController ctrl) {
		this.ctrl = ctrl;
		if (ctrl != null) {
			seasonBox.setSelectedIndex(ctrl.mc.season.ordinal());
			setUpLocationNames(ctrl.getLocationNamesArray(), ctrl.zoneTable);
		}
	}

	private void setUpLocationNames(String[] locNames, VZoneTable zoneTable) {
		locNameID.loadComboBoxValues(locNames);

		String[] zoneTags = new String[zoneTable.getZoneCount()];
		for (int i = 0; i < zoneTags.length; i++) {
			zoneTags[i] = getLocationName(i);
		}

		zoneDropdown.loadComboBoxValues(zoneTags);
		parentZoneID.loadComboBoxValues(zoneTags);
		zoneDropdown.setValue(-1);
	}

	private String getLocationName(int zoneIdx) {
		if (ctrl == null) {
			return "NULL";
		}
		return zoneIdx + " - " + ctrl.getPlaceName(zoneIdx);
	}

	@Override
	public void loadZone(VZone z) {
		if (z != null) {
			saveZone();
			areaID.setValue(z.header.areaID);
			mtxID.setValue(z.header.matrixID);
			textID.setValue(z.header.textFileID);
			scrID.setValue(z.header.scriptsID);
			lvlScrID.setValue(z.header.initScriptsID);
			encID.setValue(z.header.encID);
			battleBG.setValue(z.header.battleBG);
			npcInfoCache.setValue(z.header.npcInfoCacheIdx);
			bgmA.setValue(z.header.BGMAutumn);
			bgmSp.setValue(z.header.BGMSpring);
			bgmSm.setValue(z.header.BGMSummer);
			bgmW.setValue(z.header.BGMWinter);
			flyX.setValue(z.header.flyX);
			flyY.setValue(z.header.flyY);
			flyZ.setValue(z.header.flyZ);
			mapType.setValue(z.header.mapType);
			mapTransition.setValue(z.header.mapTransitionEffect);
			locNameDispType.setValue(z.header.locNameDispType);

			btnAllowCycling.setSelected(z.header.enableCycling);
			btnAllowSpecialBGM.setSelected(z.header.enableCyclingBGM);
			btnAllowRunning.setSelected(z.header.enableRunning);
			btnAllowFly.setSelected(z.header.enableFlyFrom);
			btnAllowEscapeRope.setSelected(z.header.enableEscapeRope);
			btnAllowEntralinkWarp.setSelected(z.header.enableEntralinkWarp);

			unk1.setValue(z.header.unknown);
			camBounds.setValue(z.header.matrixCamBoundaryIndex);
			diffLevelAdjustment.setValue(z.header.diffLevelAdjustment);
			camera.setValue(z.header.cameraIndex);
			actorProjectionMatrix.setSelectedIndex(z.header.actorProjMatrixType);
			weather.setValue(z.header.weather);
			nameIcon.setValue(z.header.nameIcon);

			entityID.setValue(z.header.entitiesID);
			parentZoneID.setValue(z.header.parentZoneID);
			locNameID.setValue(z.header.locNameNo);

			gimmick.setValue(ctrl.gimmickIndex.getZoneGimmick(z.id));
			fog.setValue(ctrl.fogIndex.getFogForZone(z.id));
			flashType.setSelectedIndex(ctrl.flashIndex.getMapEffSkillForZone(z.id).ordinal());

			btnIsRailsEnabled.setSelected(ctrl.railLoader.getRailIndexForZone(z.id) != -1);
			btnIsDynCameraEnabled.setSelected(ctrl.camLoader.getCameraIndexForZone(z.id) != -1);

			btnIsRailsEnabled.setEnabled(ctrl.railLoader.EX_IS_LOADED_FROM_INJECTED);
			btnIsDynCameraEnabled.setEnabled(ctrl.camLoader.EX_IS_LOADED_FROM_INJECTED);
		} else {
			ComponentUtils.clearComponents(areaID, mtxID, textID, scrID, lvlScrID, encID, bgmA, bgmSm, bgmSp, bgmW, flyX, flyY, flyZ, mapType, mapTransition,
				locNameDispType, unk1, camBounds, diffLevelAdjustment, camera, weather, nameIcon, entityID, parentZoneID, locNameID, gimmick, btnIsRailsEnabled, btnIsDynCameraEnabled,
				btnAllowCycling, btnAllowEscapeRope, btnAllowFly, btnAllowRunning, btnAllowSpecialBGM, btnAllowEntralinkWarp, battleBG, npcInfoCache, fog);
		}
		this.zone = z;
	}

	public void saveZone() {
		if (zone != null) {
			VZoneHeader z = zone.header;
			z.BGMAutumn = (Integer) bgmA.getValue();
			z.BGMSpring = (Integer) bgmSp.getValue();
			z.BGMSummer = (Integer) bgmSm.getValue();
			z.BGMWinter = (Integer) bgmW.getValue();
			z.areaID = (Integer) areaID.getValue();
			z.mapType = (Integer) mapType.getValue();
			z.matrixID = (Integer) mtxID.getValue();
			z.cameraIndex = (Integer) camera.getValue();
			z.actorProjMatrixType = actorProjectionMatrix.getSelectedIndex();
			z.encID = (Integer) encID.getValue();
			z.battleBG = (Integer) battleBG.getValue();
			z.npcInfoCacheIdx = (Integer) npcInfoCache.getValue();
			z.flyX = (Integer) flyX.getValue();
			z.flyY = (Integer) flyY.getValue();
			z.flyZ = (Integer) flyZ.getValue();
			z.initScriptsID = (Integer) lvlScrID.getValue();
			z.scriptsID = (Integer) scrID.getValue();
			z.mapTransitionEffect = (Integer) mapTransition.getValue();
			z.locNameDispType = (Integer) locNameDispType.getValue();
			z.locNameNo = (Integer) locNameID.getValueCB();
			z.nameIcon = (Integer) nameIcon.getValue();
			z.parentZoneID = (Integer) parentZoneID.getValueCB();
			z.entitiesID = (Integer) entityID.getValue();
			z.textFileID = (Integer) textID.getValue();
			z.weather = (Integer) weather.getValue();
			z.unknown = (Integer) unk1.getValue();
			z.matrixCamBoundaryIndex = (Integer) camBounds.getValue();
			z.diffLevelAdjustment = (Integer) diffLevelAdjustment.getValue();

			z.enableCycling = btnAllowCycling.isSelected();
			z.enableCyclingBGM = btnAllowSpecialBGM.isSelected();
			z.enableEscapeRope = btnAllowEscapeRope.isSelected();
			z.enableFlyFrom = btnAllowFly.isSelected();
			z.enableRunning = btnAllowRunning.isSelected();
			z.enableEntralinkWarp = btnAllowEntralinkWarp.isSelected();

			if (area != null && area.header != null) {
				area.header.texturesId = (Integer) texID.getValue();
				area.header.buildingsId = (Integer) bldID.getValue();
				area.header.srtAnimeIdx = (Integer) animeSRT.getValue();
				area.header.patAnimeIdx = (Integer) animePat.getValue();
				area.header.isExterior = isExterior.isSelected();
				area.header.lightIndex = (Integer) lights.getValue();
				area.header.outlineType = (Integer) outlines.getValue();
				area.header.unknown3 = (Integer) unknown3.getValue();
				ctrl.areaTable.saveHeader(area.id);
			}

			ctrl.zoneTable.saveHeader(zone.id);

			if (ctrl.gimmickIndex.setZoneGimmick(zone.id, (Integer) gimmick.getValue())) {
				ctrl.gimmickIndex.write();
			}
			if (ctrl.fogIndex.setFogForZone(zone.id, (Integer) fog.getValue())) {
				ctrl.fogIndex.write();
			}
			if (ctrl.flashIndex.setMapEffSkillForZone(zone.id, ZoneMapEffSkillIndex.ZoneMapEffSkillFlag.values()[flashType.getSelectedIndex()])) {
				ctrl.flashIndex.write();
			}
		}
	}

	@Override
	public void loadArea(VArea a) {
		area = a;

		if (a != null && a.header != null) {
			texID.setValue(a.header.texturesId);
			bldID.setValue(a.header.buildingsId);
			animeSRT.setValue(a.header.srtAnimeIdx);
			animePat.setValue(a.header.patAnimeIdx);
			isExterior.setSelected(a.header.isExterior);
			lights.setValue(a.header.lightIndex);
			outlines.setValue(a.header.outlineType);
			unknown3.setValue(a.header.unknown3);
		} else {
			ComponentUtils.clearComponents(texID, bldID, animeSRT, animePat, isExterior, lights, outlines, unknown3);
		}
	}

	@Override
	public String getTabName() {
		return "Zone Loader";
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
	 * code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        zoneLabel = new javax.swing.JLabel();
        zoneDropdown = new xstandard.gui.components.combobox.ComboBoxAndSpinner();
        mainSeparator = new javax.swing.JSeparator();
        resPanel = new javax.swing.JPanel();
        areaLabel = new javax.swing.JLabel();
        areaID = new javax.swing.JFormattedTextField();
        mtxLabel = new javax.swing.JLabel();
        mtxID = new javax.swing.JFormattedTextField();
        scrLabel = new javax.swing.JLabel();
        scrID = new javax.swing.JFormattedTextField();
        lvlScrLabel = new javax.swing.JLabel();
        lvlScrID = new javax.swing.JFormattedTextField();
        textLabel = new javax.swing.JLabel();
        textID = new javax.swing.JFormattedTextField();
        encID = new javax.swing.JFormattedTextField();
        encIDLabel = new javax.swing.JLabel();
        encIDLabel1 = new javax.swing.JLabel();
        entityID = new javax.swing.JFormattedTextField();
        camBoundsLabel = new javax.swing.JLabel();
        camBounds = new javax.swing.JFormattedTextField();
        npcInfoCacheLabel = new javax.swing.JLabel();
        npcInfoCache = new javax.swing.JFormattedTextField();
        battleBGLabel = new javax.swing.JLabel();
        battleBG = new javax.swing.JFormattedTextField();
        BGMPanel = new javax.swing.JPanel();
        bgmALabel = new javax.swing.JLabel();
        bgmSmLabel = new javax.swing.JLabel();
        bgmSm = new javax.swing.JFormattedTextField();
        bgmWLabel = new javax.swing.JLabel();
        bgmSpLabel = new javax.swing.JLabel();
        bgmW = new javax.swing.JFormattedTextField();
        bgmSp = new javax.swing.JFormattedTextField();
        bgmA = new javax.swing.JFormattedTextField();
        btnAllowSpecialBGM = new javax.swing.JCheckBox();
        locInfoPanel = new javax.swing.JPanel();
        mapTypeLabel = new javax.swing.JLabel();
        mapType = new javax.swing.JFormattedTextField();
        mapChgLabel = new javax.swing.JLabel();
        mapTransition = new javax.swing.JFormattedTextField();
        parentZoneIDLabel = new javax.swing.JLabel();
        locNameIDLabel = new javax.swing.JLabel();
        parentZoneID = new xstandard.gui.components.combobox.ComboBoxAndSpinner();
        locNameID = new xstandard.gui.components.combobox.ComboBoxAndSpinner();
        locNameDispTypeLabel = new javax.swing.JLabel();
        locNameDispType = new javax.swing.JFormattedTextField();
        flyLandPointPanel = new javax.swing.JPanel();
        flyX = new javax.swing.JFormattedTextField();
        flyXLabel = new javax.swing.JLabel();
        flyYLabel = new javax.swing.JLabel();
        flyY = new javax.swing.JFormattedTextField();
        flyZLabel = new javax.swing.JLabel();
        flyZ = new javax.swing.JFormattedTextField();
        btnSetFlyToPlayer = new javax.swing.JButton();
        areaPanel = new javax.swing.JPanel();
        bldIdLabel = new javax.swing.JLabel();
        bldID = new javax.swing.JFormattedTextField();
        texIDLabel = new javax.swing.JLabel();
        texID = new javax.swing.JFormattedTextField();
        anime1Label = new javax.swing.JLabel();
        animeSRT = new javax.swing.JFormattedTextField();
        unknown1Label = new javax.swing.JLabel();
        lights = new javax.swing.JFormattedTextField();
        unknown3Label = new javax.swing.JLabel();
        unknown3 = new javax.swing.JFormattedTextField();
        isExterior = new javax.swing.JCheckBox();
        animePat = new javax.swing.JFormattedTextField();
        anime2Label = new javax.swing.JLabel();
        unknown1Label1 = new javax.swing.JLabel();
        outlines = new javax.swing.JFormattedTextField();
        btnSave = new javax.swing.JButton();
        othersPanel = new javax.swing.JPanel();
        unk1Label = new javax.swing.JLabel();
        unk1 = new javax.swing.JFormattedTextField();
        diffLevelAdjustLabel = new javax.swing.JLabel();
        diffLevelAdjustment = new javax.swing.JFormattedTextField();
        nameIconLabel = new javax.swing.JLabel();
        nameIcon = new javax.swing.JFormattedTextField();
        gimmickLabel = new javax.swing.JLabel();
        gimmick = new javax.swing.JFormattedTextField();
        seasonLabel = new javax.swing.JLabel();
        seasonBox = new javax.swing.JComboBox<>();
        featuresPanel = new javax.swing.JPanel();
        btnIsRailsEnabled = new javax.swing.JCheckBox();
        btnIsDynCameraEnabled = new javax.swing.JCheckBox();
        flasgPanel = new javax.swing.JPanel();
        btnAllowCycling = new javax.swing.JCheckBox();
        btnAllowRunning = new javax.swing.JCheckBox();
        btnAllowEscapeRope = new javax.swing.JCheckBox();
        btnAllowFly = new javax.swing.JCheckBox();
        btnAllowEntralinkWarp = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        weather = new javax.swing.JFormattedTextField();
        weatherLabel = new javax.swing.JLabel();
        camera = new javax.swing.JFormattedTextField();
        cameraLabel = new javax.swing.JLabel();
        fogLabel = new javax.swing.JLabel();
        fog = new javax.swing.JFormattedTextField();
        flashTypeLabel = new javax.swing.JLabel();
        flashType = new javax.swing.JComboBox<>();
        projMatrixLabel = new javax.swing.JLabel();
        actorProjectionMatrix = new javax.swing.JComboBox<>();
        btnAddZoneData = new javax.swing.JButton();

        zoneLabel.setText("Zone:");

        zoneDropdown.setMaximumRowCount(35);

        resPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Resources"));

        areaLabel.setText("Area");

        areaID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        mtxLabel.setText("Matrix");

        mtxID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        scrLabel.setText("Scripts");

        scrID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        lvlScrLabel.setText("Level scripts");

        lvlScrID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        textLabel.setText("Text");

        textID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        encID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        encIDLabel.setText("Encounters");

        encIDLabel1.setText("Entities");

        entityID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        camBoundsLabel.setText("Cam boundaries");

        camBounds.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        npcInfoCacheLabel.setText("NPC Info cache");

        npcInfoCache.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        battleBGLabel.setText("Battle BG");

        battleBG.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        javax.swing.GroupLayout resPanelLayout = new javax.swing.GroupLayout(resPanel);
        resPanel.setLayout(resPanelLayout);
        resPanelLayout.setHorizontalGroup(
            resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resPanelLayout.createSequentialGroup()
                        .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(resPanelLayout.createSequentialGroup()
                                .addComponent(areaLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(areaID, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(resPanelLayout.createSequentialGroup()
                                .addComponent(mtxLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(mtxID, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(resPanelLayout.createSequentialGroup()
                                .addComponent(scrLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(scrID, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(resPanelLayout.createSequentialGroup()
                                .addComponent(lvlScrLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lvlScrID, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(resPanelLayout.createSequentialGroup()
                                .addComponent(textLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(textID, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(resPanelLayout.createSequentialGroup()
                                .addComponent(encIDLabel)
                                .addGap(33, 33, 33)
                                .addComponent(encID, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(resPanelLayout.createSequentialGroup()
                                .addComponent(encIDLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(entityID, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(resPanelLayout.createSequentialGroup()
                                .addComponent(camBoundsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(camBounds, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(resPanelLayout.createSequentialGroup()
                        .addComponent(npcInfoCacheLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(npcInfoCache, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(resPanelLayout.createSequentialGroup()
                        .addComponent(battleBGLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(battleBG, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        resPanelLayout.setVerticalGroup(
            resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(areaLabel)
                    .addComponent(areaID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mtxLabel)
                    .addComponent(mtxID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scrLabel)
                    .addComponent(scrID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lvlScrLabel)
                    .addComponent(lvlScrID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textLabel)
                    .addComponent(textID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encIDLabel)
                    .addComponent(encID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encIDLabel1)
                    .addComponent(entityID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(camBoundsLabel)
                    .addComponent(camBounds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(npcInfoCacheLabel)
                    .addComponent(npcInfoCache, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(battleBGLabel)
                    .addComponent(battleBG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        BGMPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("BGM"));

        bgmALabel.setText("Autumn");

        bgmSmLabel.setText("Summer");

        bgmSm.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        bgmWLabel.setText("Winter");

        bgmSpLabel.setText("Spring");

        bgmW.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        bgmSp.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        bgmA.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        btnAllowSpecialBGM.setText("Allow cycling/surf BGM");

        javax.swing.GroupLayout BGMPanelLayout = new javax.swing.GroupLayout(BGMPanel);
        BGMPanel.setLayout(BGMPanelLayout);
        BGMPanelLayout.setHorizontalGroup(
            BGMPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BGMPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(BGMPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(BGMPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(BGMPanelLayout.createSequentialGroup()
                            .addComponent(bgmSpLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bgmSp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(BGMPanelLayout.createSequentialGroup()
                            .addComponent(bgmSmLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bgmSm, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(BGMPanelLayout.createSequentialGroup()
                            .addComponent(bgmALabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bgmA, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(BGMPanelLayout.createSequentialGroup()
                            .addComponent(bgmWLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bgmW, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btnAllowSpecialBGM))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        BGMPanelLayout.setVerticalGroup(
            BGMPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BGMPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(BGMPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bgmSpLabel)
                    .addComponent(bgmSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BGMPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bgmSmLabel)
                    .addComponent(bgmSm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BGMPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bgmALabel)
                    .addComponent(bgmA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BGMPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bgmWLabel)
                    .addComponent(bgmW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAllowSpecialBGM)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        locInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Location info"));

        mapTypeLabel.setText("Map type");

        mapType.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        mapChgLabel.setText("Map transition");

        mapTransition.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        parentZoneIDLabel.setText("Parent Zone ID");

        locNameIDLabel.setText("Location name ID");

        parentZoneID.setMaximumRowCount(35);

        locNameID.setMaximumRowCount(35);

        locNameDispTypeLabel.setText("Display type");

        locNameDispType.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        javax.swing.GroupLayout locInfoPanelLayout = new javax.swing.GroupLayout(locInfoPanel);
        locInfoPanel.setLayout(locInfoPanelLayout);
        locInfoPanelLayout.setHorizontalGroup(
            locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(locInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(locInfoPanelLayout.createSequentialGroup()
                        .addComponent(parentZoneIDLabel)
                        .addGap(20, 20, 20)
                        .addComponent(parentZoneID, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE))
                    .addGroup(locInfoPanelLayout.createSequentialGroup()
                        .addGroup(locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mapChgLabel)
                            .addComponent(mapTypeLabel))
                        .addGap(25, 25, 25)
                        .addGroup(locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mapTransition, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mapType, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(locInfoPanelLayout.createSequentialGroup()
                        .addGroup(locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(locNameIDLabel)
                            .addGroup(locInfoPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(locNameDispTypeLabel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(locInfoPanelLayout.createSequentialGroup()
                                .addComponent(locNameDispType, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(locNameID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        locInfoPanelLayout.setVerticalGroup(
            locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(locInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mapTypeLabel)
                    .addComponent(mapType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mapChgLabel)
                    .addComponent(mapTransition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(parentZoneID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(parentZoneIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(locNameID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locNameIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(locInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locNameDispTypeLabel)
                    .addComponent(locNameDispType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        flyLandPointPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Fly landing point"));

        flyX.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        flyXLabel.setText("X");

        flyYLabel.setText("Y");

        flyY.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        flyZLabel.setText("Z");

        flyZ.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        btnSetFlyToPlayer.setText("Set to player position");
        btnSetFlyToPlayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetFlyToPlayerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout flyLandPointPanelLayout = new javax.swing.GroupLayout(flyLandPointPanel);
        flyLandPointPanel.setLayout(flyLandPointPanelLayout);
        flyLandPointPanelLayout.setHorizontalGroup(
            flyLandPointPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(flyLandPointPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(flyXLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flyX, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flyYLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flyY, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flyZLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flyZ, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, flyLandPointPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSetFlyToPlayer)
                .addContainerGap())
        );
        flyLandPointPanelLayout.setVerticalGroup(
            flyLandPointPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, flyLandPointPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(flyLandPointPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(flyLandPointPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(flyZLabel)
                        .addComponent(flyZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(flyLandPointPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(flyYLabel)
                        .addComponent(flyY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(flyLandPointPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(flyXLabel)
                        .addComponent(flyX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSetFlyToPlayer)
                .addContainerGap())
        );

        areaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Area resources"));

        bldIdLabel.setText("Buildings");

        bldID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        texIDLabel.setText("Textures");

        texID.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        anime1Label.setText("SRT Animations");

        animeSRT.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        unknown1Label.setText("Lights");

        lights.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        unknown3Label.setText("Actor materials");

        unknown3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        isExterior.setText("Exterior");

        animePat.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        anime2Label.setText("Pattern animations");

        unknown1Label1.setText("Outlines");

        outlines.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        javax.swing.GroupLayout areaPanelLayout = new javax.swing.GroupLayout(areaPanel);
        areaPanel.setLayout(areaPanelLayout);
        areaPanelLayout.setHorizontalGroup(
            areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(areaPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(areaPanelLayout.createSequentialGroup()
                        .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(texIDLabel)
                            .addComponent(bldIdLabel))
                        .addGap(65, 65, 65)
                        .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(texID, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bldID, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(areaPanelLayout.createSequentialGroup()
                        .addComponent(anime1Label)
                        .addGap(34, 34, 34)
                        .addComponent(animeSRT, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(areaPanelLayout.createSequentialGroup()
                        .addComponent(anime2Label)
                        .addGap(18, 18, 18)
                        .addComponent(animePat, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(isExterior)
                    .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(areaPanelLayout.createSequentialGroup()
                            .addComponent(unknown1Label)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lights, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(areaPanelLayout.createSequentialGroup()
                            .addComponent(unknown3Label)
                            .addGap(12, 12, 12)
                            .addComponent(unknown3, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(areaPanelLayout.createSequentialGroup()
                            .addComponent(unknown1Label1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(outlines, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        areaPanelLayout.setVerticalGroup(
            areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(areaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bldIdLabel)
                    .addComponent(bldID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(isExterior))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unknown1Label)
                    .addComponent(lights, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(texID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(texIDLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unknown1Label1)
                    .addComponent(outlines, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(animeSRT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(anime1Label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(unknown3Label)
                        .addComponent(unknown3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(areaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(animePat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(anime2Label)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnSave.setText("Save");
        btnSave.setToolTipText("");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        othersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Other"));

        unk1Label.setText("Unknown 1");

        unk1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        diffLevelAdjustLabel.setText("Difficulty level +-");
        diffLevelAdjustLabel.setToolTipText("The wild encounter level addend or subtrahend for Challenge and Easy mode respectively.");

        diffLevelAdjustment.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        nameIconLabel.setText("Name icon");

        nameIcon.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        gimmickLabel.setText("Gimmick");

        gimmick.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        javax.swing.GroupLayout othersPanelLayout = new javax.swing.GroupLayout(othersPanel);
        othersPanel.setLayout(othersPanelLayout);
        othersPanelLayout.setHorizontalGroup(
            othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(othersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(diffLevelAdjustLabel)
                    .addComponent(gimmickLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gimmick, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(diffLevelAdjustment, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49)
                .addGroup(othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(unk1Label)
                    .addComponent(nameIconLabel))
                .addGap(18, 18, 18)
                .addGroup(othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameIcon, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unk1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        othersPanelLayout.setVerticalGroup(
            othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(othersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(othersPanelLayout.createSequentialGroup()
                        .addGroup(othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameIcon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nameIconLabel)
                            .addComponent(gimmick, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(diffLevelAdjustLabel)
                                .addComponent(diffLevelAdjustment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(othersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(unk1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(unk1Label))))
                    .addComponent(gimmickLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        seasonLabel.setText("Season:");

        seasonBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Spring", "Summer", "Autumn", "Winter" }));

        featuresPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Features"));

        btnIsRailsEnabled.setText("Rails");

        btnIsDynCameraEnabled.setText("Dynamic camera");

        javax.swing.GroupLayout featuresPanelLayout = new javax.swing.GroupLayout(featuresPanel);
        featuresPanel.setLayout(featuresPanelLayout);
        featuresPanelLayout.setHorizontalGroup(
            featuresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(featuresPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnIsRailsEnabled)
                .addGap(61, 61, 61)
                .addComponent(btnIsDynCameraEnabled)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        featuresPanelLayout.setVerticalGroup(
            featuresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(featuresPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(featuresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnIsRailsEnabled)
                    .addComponent(btnIsDynCameraEnabled))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        flasgPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Flags"));

        btnAllowCycling.setText("Allow cycling");

        btnAllowRunning.setText("Allow running (unused)");

        btnAllowEscapeRope.setText("Allow Escape Rope");

        btnAllowFly.setText("Allow flying from here");

        btnAllowEntralinkWarp.setText("Allow Entralink warp");

        javax.swing.GroupLayout flasgPanelLayout = new javax.swing.GroupLayout(flasgPanel);
        flasgPanel.setLayout(flasgPanelLayout);
        flasgPanelLayout.setHorizontalGroup(
            flasgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(flasgPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(flasgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAllowCycling)
                    .addComponent(btnAllowRunning)
                    .addComponent(btnAllowEscapeRope)
                    .addComponent(btnAllowFly)
                    .addComponent(btnAllowEntralinkWarp))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        flasgPanelLayout.setVerticalGroup(
            flasgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(flasgPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAllowCycling)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAllowRunning)
                .addGap(18, 18, 18)
                .addComponent(btnAllowEscapeRope)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAllowFly)
                .addGap(18, 18, 18)
                .addComponent(btnAllowEntralinkWarp)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Environment"));

        weather.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        weatherLabel.setText("Weather");

        camera.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        cameraLabel.setText("Camera");

        fogLabel.setText("Fog");

        fog.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        flashTypeLabel.setText("Flash");

        flashType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        projMatrixLabel.setText("Object proj. matrix");

        actorProjectionMatrix.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Z += 0.075", "Z += 0.121" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fogLabel)
                    .addComponent(flashTypeLabel)
                    .addComponent(projMatrixLabel)
                    .addComponent(cameraLabel)
                    .addComponent(weatherLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fog)
                    .addComponent(weather)
                    .addComponent(camera)
                    .addComponent(flashType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(actorProjectionMatrix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(weather, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(weatherLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(camera, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cameraLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fogLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(flashType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(flashTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(actorProjectionMatrix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projMatrixLabel))
                .addContainerGap())
        );

        btnAddZoneData.setText("+");
        btnAddZoneData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddZoneDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mainSeparator)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnSave)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addComponent(resPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(flyLandPointPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(BGMPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                            .addComponent(featuresPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(areaPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addComponent(flasgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addComponent(locInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(othersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addGap(0, 0, 0)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(zoneLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zoneDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seasonLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seasonBox, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(342, 342, 342)
                        .addComponent(btnAddZoneData)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(seasonLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zoneDropdown, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zoneLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(seasonBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnAddZoneData)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mainSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(locInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(flasgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(BGMPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(flyLandPointPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(resPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(featuresPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(othersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(areaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		saveZone();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnSetFlyToPlayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetFlyToPlayerActionPerformed
		flyX.setValue(ctrl.player.playerMModel.NPCData.gposX);
		flyY.setValue(VGridObject.worldToTile(ctrl.player.playerMModel.NPCData.wposY));
		flyZ.setValue(ctrl.player.playerMModel.NPCData.gposZ);
    }//GEN-LAST:event_btnSetFlyToPlayerActionPerformed

    private void btnAddZoneDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddZoneDataActionPerformed
		if (ctrl != null) {
			if (zone != null && zone.header != null) {
				VZoneHeader newZoneHeader = new VZoneHeader(zone.header);
				int idx = ctrl.zoneTable.getZoneCount();
				ctrl.zoneTable.headers.add(newZoneHeader);
				ctrl.zoneTable.saveHeader(idx);
				zoneDropdown.addItem(getLocationName(idx));
				if (DialogUtils.showYesNoDialog(editors.getCTRMap(), "Load new zone?", "Would you like to load the newly added zone in the editor?")) {
					zoneDropdown.setValue(idx);
				}
			} else {
				DialogUtils.showInfoMessage(editors.getCTRMap(), "No zone loaded", "Adding a new zone will duplicate the currently loaded zone.\nPlease load one first.");
			}
		}
    }//GEN-LAST:event_btnAddZoneDataActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel BGMPanel;
    private javax.swing.JComboBox<String> actorProjectionMatrix;
    private javax.swing.JLabel anime1Label;
    private javax.swing.JLabel anime2Label;
    private javax.swing.JFormattedTextField animePat;
    private javax.swing.JFormattedTextField animeSRT;
    private javax.swing.JFormattedTextField areaID;
    private javax.swing.JLabel areaLabel;
    private javax.swing.JPanel areaPanel;
    private javax.swing.JFormattedTextField battleBG;
    private javax.swing.JLabel battleBGLabel;
    private javax.swing.JFormattedTextField bgmA;
    private javax.swing.JLabel bgmALabel;
    private javax.swing.JFormattedTextField bgmSm;
    private javax.swing.JLabel bgmSmLabel;
    private javax.swing.JFormattedTextField bgmSp;
    private javax.swing.JLabel bgmSpLabel;
    private javax.swing.JFormattedTextField bgmW;
    private javax.swing.JLabel bgmWLabel;
    private javax.swing.JFormattedTextField bldID;
    private javax.swing.JLabel bldIdLabel;
    private javax.swing.JButton btnAddZoneData;
    private javax.swing.JCheckBox btnAllowCycling;
    private javax.swing.JCheckBox btnAllowEntralinkWarp;
    private javax.swing.JCheckBox btnAllowEscapeRope;
    private javax.swing.JCheckBox btnAllowFly;
    private javax.swing.JCheckBox btnAllowRunning;
    private javax.swing.JCheckBox btnAllowSpecialBGM;
    private javax.swing.JCheckBox btnIsDynCameraEnabled;
    private javax.swing.JCheckBox btnIsRailsEnabled;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSetFlyToPlayer;
    private javax.swing.JFormattedTextField camBounds;
    private javax.swing.JLabel camBoundsLabel;
    private javax.swing.JFormattedTextField camera;
    private javax.swing.JLabel cameraLabel;
    private javax.swing.JLabel diffLevelAdjustLabel;
    private javax.swing.JFormattedTextField diffLevelAdjustment;
    private javax.swing.JFormattedTextField encID;
    private javax.swing.JLabel encIDLabel;
    private javax.swing.JLabel encIDLabel1;
    private javax.swing.JFormattedTextField entityID;
    private javax.swing.JPanel featuresPanel;
    private javax.swing.JPanel flasgPanel;
    private javax.swing.JComboBox<String> flashType;
    private javax.swing.JLabel flashTypeLabel;
    private javax.swing.JPanel flyLandPointPanel;
    private javax.swing.JFormattedTextField flyX;
    private javax.swing.JLabel flyXLabel;
    private javax.swing.JFormattedTextField flyY;
    private javax.swing.JLabel flyYLabel;
    private javax.swing.JFormattedTextField flyZ;
    private javax.swing.JLabel flyZLabel;
    private javax.swing.JFormattedTextField fog;
    private javax.swing.JLabel fogLabel;
    private javax.swing.JFormattedTextField gimmick;
    private javax.swing.JLabel gimmickLabel;
    private javax.swing.JCheckBox isExterior;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JFormattedTextField lights;
    private javax.swing.JPanel locInfoPanel;
    private javax.swing.JFormattedTextField locNameDispType;
    private javax.swing.JLabel locNameDispTypeLabel;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner locNameID;
    private javax.swing.JLabel locNameIDLabel;
    private javax.swing.JFormattedTextField lvlScrID;
    private javax.swing.JLabel lvlScrLabel;
    private javax.swing.JSeparator mainSeparator;
    private javax.swing.JLabel mapChgLabel;
    private javax.swing.JFormattedTextField mapTransition;
    private javax.swing.JFormattedTextField mapType;
    private javax.swing.JLabel mapTypeLabel;
    private javax.swing.JFormattedTextField mtxID;
    private javax.swing.JLabel mtxLabel;
    private javax.swing.JFormattedTextField nameIcon;
    private javax.swing.JLabel nameIconLabel;
    private javax.swing.JFormattedTextField npcInfoCache;
    private javax.swing.JLabel npcInfoCacheLabel;
    private javax.swing.JPanel othersPanel;
    private javax.swing.JFormattedTextField outlines;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner parentZoneID;
    private javax.swing.JLabel parentZoneIDLabel;
    private javax.swing.JLabel projMatrixLabel;
    private javax.swing.JPanel resPanel;
    private javax.swing.JFormattedTextField scrID;
    private javax.swing.JLabel scrLabel;
    private javax.swing.JComboBox<String> seasonBox;
    private javax.swing.JLabel seasonLabel;
    private javax.swing.JFormattedTextField texID;
    private javax.swing.JLabel texIDLabel;
    private javax.swing.JFormattedTextField textID;
    private javax.swing.JLabel textLabel;
    private javax.swing.JFormattedTextField unk1;
    private javax.swing.JLabel unk1Label;
    private javax.swing.JLabel unknown1Label;
    private javax.swing.JLabel unknown1Label1;
    private javax.swing.JFormattedTextField unknown3;
    private javax.swing.JLabel unknown3Label;
    private javax.swing.JFormattedTextField weather;
    private javax.swing.JLabel weatherLabel;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner zoneDropdown;
    private javax.swing.JLabel zoneLabel;
    // End of variables declaration//GEN-END:variables
}
