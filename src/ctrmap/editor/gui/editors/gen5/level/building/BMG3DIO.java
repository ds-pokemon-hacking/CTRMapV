package ctrmap.editor.gui.editors.gen5.level.building;

import ctrmap.formats.generic.collada.DAE;
import ctrmap.formats.generic.interchange.AnimeUtil;
import ctrmap.formats.generic.source.SMD;
import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.formats.ntr.nitrowriter.nsbca.NSBCAWriter;
import ctrmap.formats.ntr.nitrowriter.nsbmd.NSBMDExportSettings;
import ctrmap.formats.ntr.nitrowriter.nsbmd.NSBMDWriter;
import ctrmap.formats.ntr.nitrowriter.nsbta.NSBTAWriter;
import ctrmap.formats.ntr.nitrowriter.nsbva.NSBVAWriter;
import ctrmap.formats.pokemon.gen5.buildings.AreaBuildingResource;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.gui.DialogUtils;
import xstandard.gui.file.ExtensionFilter;
import java.awt.Component;

public class BMG3DIO {

	public static NSBMDExportSettings getBMExportSettingsBMD(boolean polystrips) {
		return new NSBMDExportSettings(true, false, true, polystrips, -1, true, true);
	}

	public static Skeleton getBmRscSkel(AreaBuildingResource bmRsc) {
		if (!bmRsc.models.isEmpty()) {
			return bmRsc.models.get(0).skeleton;
		}
		return null;
	}

	public static byte[] convertNNSAnm(Skeleton skeleton, AbstractAnimation anm) {
		if (anm instanceof SkeletalAnimation && skeleton != null) {
			return new NSBCAWriter(skeleton, (SkeletalAnimation) anm).writeToMemory();
		} else if (anm instanceof MaterialAnimation) {
			return new NSBTAWriter(null, (MaterialAnimation) anm).writeToMemory();
		} else if (anm instanceof VisibilityAnimation) {
			return new NSBVAWriter(skeleton, (VisibilityAnimation) anm).writeToMemory();
		}
		return null;
	}

	public static ExtensionFilter[] getFiltersForAnm(AbstractAnimation a) {
		if (a instanceof SkeletalAnimation) {
			return new ExtensionFilter[]{DAE.EXTENSION_FILTER, SMD.EXTENSION_FILTER, AnimeUtil.SA_EXTENSION_FILTER};
		} else if (a instanceof MaterialAnimation) {
			return new ExtensionFilter[]{AnimeUtil.MA_EXTENSION_FILTER};
		} else if (a instanceof VisibilityAnimation) {
			return new ExtensionFilter[]{DAE.EXTENSION_FILTER};
		}
		return null;
	}

	public static byte[] convertModel(Component parent, G3DResource res) {
		boolean polystrips = DialogUtils.showYesNoDialog(parent, "Primitive strips", "Do you wish to auto-generate primitive strip sub-meshes?\nThis reduces vertex count greatly, but is impossible on highly detailed smooth skins.");
		NSBMDWriter bmdWriter = new NSBMDWriter(res, BMG3DIO.getBMExportSettingsBMD(polystrips), new NNSWriterLogger.DummyLogger());
		byte[] mdlData = bmdWriter.writeToMemory();
		if (mdlData == null) {
			throw new NullPointerException();
		}
		return mdlData;
	}
}
