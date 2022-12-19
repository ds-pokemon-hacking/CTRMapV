package ctrmap.editor.gui.editors.gen5.level.building;

import xstandard.util.ListenableList;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildingResource;
import javax.swing.ListSelectionModel;

public class VBuildingSelectJList extends javax.swing.JList {

	private ListenableList<AreaBuildingResource> list = new ListenableList<>();

	private DefaultListModel<String> bmListModel = new DefaultListModel<>();

	private List<BuildingSelectionListener> listeners = new ArrayList<>();

	private boolean loaded = false;

	private final ListenableList.ElementChangeListener listListener = new ListenableList.ElementChangeListener() {
		@Override
		public void onEntityChange(ListenableList.ElementChangeEvent evt) {
			switch (evt.type) {
				case ADD:
					bmListModel.add(evt.index, ((AreaBuildingResource) evt.element).getBmName());
					break;
				case REMOVE:
				{
					int selIdx = getSelectedIndex();
					bmListModel.remove(evt.index);
					if (selIdx == evt.index) {
						setSelectedIndex(Math.min(selIdx, bmListModel.getSize()));
					}
					break;
				}
				case MODIFY:
				{
					int selIdx = getSelectedIndex();
					bmListModel.remove(evt.index);
					bmListModel.add(evt.index, ((AreaBuildingResource) evt.element).getBmName());
					if (evt.index == selIdx) {
						setSelectedIndex(selIdx);
					}
					break;
				}
			}
		}
	};

	public VBuildingSelectJList() {
		setModel(bmListModel);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() && loaded) {
					int selBld = getSelectedBuildingUID();

					for (BuildingSelectionListener l : listeners) {
						l.onBuildingRscSelected(selBld);
					}
				}
			}
		});
	}

	public void addBuildingSelectionListener(BuildingSelectionListener l) {
		listeners.add(l);
	}

	public void loadBuildingList(ListenableList<AreaBuildingResource> buildings) {
		loaded = false;
		list.removeListener(listListener);
		buildings.addListener(listListener);
		list = buildings;

		bmListModel.clear();

		for (AreaBuildingResource rsc : list) {
			bmListModel.addElement(rsc.getBmName());
		}

		loaded = true;
	}

	public void setSelectedBuilding(AreaBuildingResource rsc) {
		int index = list.indexOf(rsc);
		if (index == -1) {
			clearSelection();
		} else {
			setSelectedIndex(index);
			ensureIndexIsVisible(index);
		}
	}

	public AreaBuildingResource getSelectedBuilding() {
		return list.getOrDefault(getSelectedIndex(), null);
	}

	public int getSelectedBuildingUID() {
		AreaBuildingResource rsc = getSelectedBuilding();
		return rsc != null ? rsc.uid : -1;
	}

	public static interface BuildingSelectionListener {

		public void onBuildingRscSelected(int selectedBmResID);
	}
}
