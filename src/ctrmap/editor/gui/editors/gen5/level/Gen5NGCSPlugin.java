package ctrmap.editor.gui.editors.gen5.level;

import ctrmap.creativestudio.nitroplugin.NSBMDExportDialog;
import ctrmap.creativestudio.nitroplugin.NSBMDImportDialog;
import ctrmap.creativestudio.ngcs.io.CSG3DIOContentType;
import ctrmap.creativestudio.ngcs.io.DefaultG3DFormatHandler;
import ctrmap.creativestudio.ngcs.io.DefaultG3DFormatHandlerSklImEx;
import ctrmap.creativestudio.ngcs.io.DefaultG3DImportOnlyFormatHandler;
import ctrmap.creativestudio.ngcs.io.FormatDetectorInput;
import ctrmap.creativestudio.ngcs.io.G3DIOProvider;
import ctrmap.creativestudio.ngcs.io.IG3DFormatExHandler;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT_EXDATA_NEEDS_CAMERA;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT_EXDATA_NEEDS_SKELETON;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT_HAS_EXCONFIG;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_IMPORT;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_IMPORT_HAS_EXCONFIG;
import ctrmap.creativestudio.nitroplugin.NGCSNNSWriterLogger;
import ctrmap.creativestudio.ngcs.rtldr.NGCSContentAccessor;
import ctrmap.creativestudio.ngcs.rtldr.NGCSJulietIface;
import ctrmap.formats.ntr.nitroreader.nsbca.NSBCA;
import ctrmap.formats.ntr.nitroreader.nsbma.NSBMA;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMD;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDImportSettings;
import ctrmap.formats.ntr.nitroreader.nsbta.NSBTA;
import ctrmap.formats.ntr.nitroreader.nsbtp.NSBTP;
import ctrmap.formats.ntr.nitroreader.nsbtx.NSBTX;
import ctrmap.formats.ntr.nitroreader.nsbva.NSBVA;
import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.formats.ntr.nitrowriter.nsbca.NSBCAWriter;
import ctrmap.formats.ntr.nitrowriter.nsbmd.NSBMDExportSettings;
import ctrmap.formats.ntr.nitrowriter.nsbmd.NSBMDWriter;
import ctrmap.formats.ntr.nitrowriter.nsbta.NSBTAWriter;
import ctrmap.formats.ntr.nitrowriter.nsbtx.NSBTXWriter;
import ctrmap.formats.ntr.nitrowriter.nsbva.NSBVAWriter;
import ctrmap.formats.pokemon.gen5.gfbca.GFBCA;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.camera.CameraBoneTransform;
import ctrmap.renderer.scene.animation.camera.CameraViewpointBoneTransform;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.gui.ActionSelector;
import xstandard.gui.DialogUtils;
import xstandard.gui.file.XFileDialog;
import xstandard.gui.file.ExtensionFilter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import ctrmap.creativestudio.ngcs.rtldr.INGCSPlugin;
import ctrmap.creativestudio.nitroplugin.NSBCAExportDialog;
import ctrmap.formats.common.FormatIOExConfig;
import ctrmap.formats.ntr.nitrowriter.nsbca.NSBCAExportSettings;

public class Gen5NGCSPlugin implements INGCSPlugin {

	public static final IG3DFormatHandler CSNNS_BMD = new IG3DFormatExHandler<NSBMDImportSettings, NSBMDExportSettings>() {

		@Override
		public int getAttributes() {
			return G3DFMT_IMPORT | G3DFMT_EXPORT | G3DFMT_IMPORT_HAS_EXCONFIG | G3DFMT_EXPORT_HAS_EXCONFIG;
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NSBMD.MAGIC);
		}

		@Override
		public ExtensionFilter getExtensionFilter() {
			return NSBMDWriter.EXTENSION_FILTER;
		}

		@Override
		public NSBMDImportSettings popupImportExConfigDialog(Frame parent) {
			return NSBMDImportDialog.getImportSettings(parent);
		}

		@Override
		public NSBMDExportSettings popupExportExConfigDialog(Frame parent) {
			return NSBMDExportDialog.getExportSettings(parent);
		}

		@Override
		public G3DResource importFileEx(FSFile file, G3DIOProvider exData, NSBMDImportSettings config) {
			return new NSBMD(file).toGeneric(config);
		}

		@Override
		public void exportResourceEx(G3DResource rsc, G3DIOProvider exData, FSFile target, NSBMDExportSettings config) {
			NSBMDWriter writer = new NSBMDWriter(rsc, config, new NNSWriterLogger.DummyLogger());
			writer.write(target);
		}
	};

	public static final IG3DFormatHandler CSNNS_BCA = new IG3DFormatExHandler<FormatIOExConfig, NSBCAExportSettings>() {
		@Override
		public FormatIOExConfig popupImportExConfigDialog(Frame parent) {
			throw new UnsupportedOperationException();
		}

		@Override
		public NSBCAExportSettings popupExportExConfigDialog(Frame parent) {
			return NSBCAExportDialog.getExportSettings(parent);
		}

		@Override
		public G3DResource importFile(FSFile file, G3DIOProvider exData) {
			return importFileEx(file, exData, null);
		}

		@Override
		public G3DResource importFileEx(FSFile file, G3DIOProvider exData, FormatIOExConfig config) {
			return new NSBCA(file).toGeneric(exData.getSkeleton());
		}

		@Override
		public void exportResourceEx(G3DResource res, G3DIOProvider exData, FSFile target, NSBCAExportSettings config) {
			NSBCAWriter writer = new NSBCAWriter(config, exData.getSkeleton(), res.skeletalAnimations.toArray(new SkeletalAnimation[res.skeletalAnimations.size()]));
			writer.write(target);
		}

		@Override
		public int getAttributes() {
			return G3DFMT_IMPORT | G3DFMT_EXPORT | G3DFMT_EXPORT_HAS_EXCONFIG | G3DFMT_IMPORT_EXDATA_NEEDS_SKELETON | G3DFMT_EXPORT_EXDATA_NEEDS_SKELETON;
		}

		@Override
		public ExtensionFilter getExtensionFilter() {
			return NSBCAWriter.EXTENSION_FILTER;
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NSBCA.MAGIC);
		}
	};

	public static final IG3DFormatHandler CSNNS_BTA = new DefaultG3DFormatHandler(NSBTAWriter.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NSBTA.MAGIC);
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new NSBTA(fsf).toGeneric();
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			NSBTAWriter writer = new NSBTAWriter(new NNSWriterLogger.DummyLogger(), res.materialAnimations.toArray(new MaterialAnimation[res.materialAnimations.size()]));
			writer.write(target);
		}
	};

	public static final IG3DFormatHandler CSNNS_BTX = new DefaultG3DFormatHandler(NSBTXWriter.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NSBTX.MAGIC);
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new NSBTX(fsf).TEX0.toGeneric();
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			NSBTXWriter writer = new NSBTXWriter(res.textures, new NNSWriterLogger.DummyLogger());
			writer.write(target);
		}
	};

	public static final IG3DFormatHandler CSNNS_BVA = new DefaultG3DFormatHandlerSklImEx(NSBVAWriter.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NSBVA.MAGIC);
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new NSBVA(fsf).toGeneric(exData.getSkeleton());
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			NSBVAWriter w = new NSBVAWriter(exData.getSkeleton(), res.visibilityAnimations.toArray(new VisibilityAnimation[res.visibilityAnimations.size()]));
			w.write(target);
		}
	};

	public static final IG3DFormatHandler CSNNS_BTP = new DefaultG3DImportOnlyFormatHandler(new ExtensionFilter("Nitro System Binary Texture Pattern Animation", "*.nsbtp")) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NSBTP.MAGIC);
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new NSBTP(fsf).toGeneric();
		}
	};

	public static final IG3DFormatHandler CSNNS_BMA = new DefaultG3DImportOnlyFormatHandler(new ExtensionFilter("Nitro System Binary Material Animation", "*.nsbma")) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NSBMA.MAGIC);
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new NSBMA(fsf).toGeneric();
		}
	};

	private static enum GFBCAImportMode implements FormatIOExConfig {
		CAMERA,
		SKELETAL
	}

	public static final IG3DFormatHandler GFBCA_ALL = new IG3DFormatExHandler<GFBCAImportMode, FormatIOExConfig>() {

		@Override
		public GFBCAImportMode popupImportExConfigDialog(Frame parent) {
			ActionSelector asel = new ActionSelector(parent, true, "Import as...", new ActionSelector.ASelAction("Camera animation", "cam"), new ActionSelector.ASelAction("Object animation", "skl"));
			asel.setVisible(true);
			String result = (String) asel.getSelectedUserObj();
			if (result == null) {
				return null;
			}
			return result.equals("cam") ? GFBCAImportMode.CAMERA : GFBCAImportMode.SKELETAL;
		}

		@Override
		public FormatIOExConfig popupExportExConfigDialog(Frame parent) {
			return null;
		}

		@Override
		public G3DResource importFileEx(FSFile file, G3DIOProvider exData, GFBCAImportMode config) {
			GFBCAImportMode mode = config;
			if (mode != null) {
				GFBCA bca = new GFBCA(file);
				switch (mode) {
					case CAMERA:
						return bca.toGenericCameraAnimation();
					case SKELETAL:
						return bca.toGenericSkeletalAnimation();
				}
			}
			return null;
		}

		@Override
		public void exportResourceEx(G3DResource rsc, G3DIOProvider exData, FSFile target, FormatIOExConfig config) {

		}

		@Override
		public int getAttributes() {
			return G3DFMT_IMPORT | G3DFMT_IMPORT_HAS_EXCONFIG;
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return GFBCA.isGFBCA(input.stream, true, false);
		}

		@Override
		public ExtensionFilter getExtensionFilter() {
			return GFBCA.EXTENSION_FILTER;
		}
	};

	public static final IG3DFormatHandler GFBCA_CAMERA = new DefaultG3DImportOnlyFormatHandler(GFBCA.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return GFBCA.isGFBCA(input.stream, true, false);
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new GFBCA(fsf).toGenericCameraAnimation();
		}
	};

	public static final IG3DFormatHandler GFBCA_SKELETAL = new DefaultG3DImportOnlyFormatHandler(GFBCA.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return GFBCA.isGFBCA(input.stream, true, false);
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new GFBCA(fsf).toGenericSkeletalAnimation();
		}
	};

	public static final IG3DFormatHandler GFBCA_CAMERA_TRANSFORM = new DefaultG3DFormatHandler(GFBCA.EXTENSION_FILTER, G3DFMT_IMPORT | G3DFMT_EXPORT | G3DFMT_EXPORT_EXDATA_NEEDS_CAMERA) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return GFBCA.isGFBCA(input.stream, true, false);
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new GFBCA(fsf).toGenericCameraAnimation();
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			for (CameraAnimation anm : res.cameraAnimations) {
				for (CameraBoneTransform bt : anm.transforms) {
					if (bt instanceof CameraViewpointBoneTransform) {
						Camera cam = exData.getCameraByName(bt.getName());

						new GFBCA(cam, anm).write(target);

						return;
					}
				}
			}
		}

		@Override
		public String getExportTargetName(G3DResource rsc) {
			for (CameraAnimation anm : rsc.cameraAnimations) {
				for (CameraBoneTransform bt : anm.transforms) {
					if (bt instanceof CameraViewpointBoneTransform) {
						return bt.getName();
					}
				}
			}
			return null;
		}
	};

	public static final IG3DFormatHandler GFBCA_SKELETAL_TRANSFORM = new DefaultG3DFormatHandler(GFBCA.EXTENSION_FILTER, G3DFMT_IMPORT | G3DFMT_EXPORT | G3DFMT_EXPORT_EXDATA_NEEDS_SKELETON) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return GFBCA.isGFBCA(input.stream, true, false);
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new GFBCA(fsf).toGenericSkeletalAnimation();
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			for (SkeletalAnimation anm : res.skeletalAnimations) {
				if (!anm.bones.isEmpty()) {
					new GFBCA(exData.getSkeleton(), anm).write(target);
					return;
				}
			}
		}
	};

	@Override
	public void registerFormats(NGCSJulietIface j) {
		j.registFormatSupport(CSG3DIOContentType.MODEL, CSNNS_BMD);
		j.registFormatSupport(CSG3DIOContentType.TEXTURE, CSNNS_BTX);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_SKL, CSNNS_BCA, GFBCA_SKELETAL);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_CAM, GFBCA_CAMERA);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_CURVE_SKL, GFBCA_SKELETAL_TRANSFORM);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_CURVE_CAM, GFBCA_CAMERA_TRANSFORM);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_MULTI_EX, GFBCA_ALL);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_VIS, CSNNS_BVA);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_MAT, CSNNS_BTA, CSNNS_BTP, CSNNS_BMA);
	}

	@Override
	public void registerUI(NGCSJulietIface j, Frame uiParent, NGCSContentAccessor contentAccessor) {
		JMenuItem item = new JMenuItem("NNS Resource");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ActionSelector exportTypeChoice = new ActionSelector(uiParent, true,
					new ActionSelector.ASelAction("Models/Textures", "bmd"),
					new ActionSelector.ASelAction("Joint Animations", "bca"),
					new ActionSelector.ASelAction("Material Animations (SRT)", "bta"),
					new ActionSelector.ASelAction("Material Animations (Pattern)", "btp"),
					new ActionSelector.ASelAction("Visibility animations", "bva")
				);
				exportTypeChoice.setTitle("Select export type");
				exportTypeChoice.setVisible(true);

				String choice = (String) exportTypeChoice.getSelectedUserObj();

				if (choice != null) {
					switch (choice) {
						case "bmd":
							csnns_ExportBMD(uiParent, contentAccessor);
							break;
						case "bca":
							csnns_ExportBCA(uiParent, contentAccessor);
							break;
						case "bva":
							csnns_ExportBVA(uiParent, contentAccessor);
							break;
						case "bta":
							csnns_ExportBTA(uiParent, contentAccessor);
							break;
					}
				}
			}
		});
		j.addMenuItem("Export", item);
	}

	private static void csnns_ExportBMD(Frame uiParent, NGCSContentAccessor contentAccessor) {
		NSBMDExportDialog dialog = new NSBMDExportDialog(uiParent, true, true);
		dialog.setVisible(true);

		NSBMDExportSettings settings = dialog.getResult();

		if (settings != null) {
			NGCSNNSWriterLogger log = new NGCSNNSWriterLogger();

			if (!settings.includeModels) {
				FSFile f = XFileDialog.openSaveFileDialog(NSBTXWriter.EXTENSION_FILTER);

				if (f != null) {
					new NSBTXWriter(contentAccessor.getTextures(), log).write(f);
				}
			} else {
				FSFile f = XFileDialog.openSaveFileDialog(NSBMDWriter.EXTENSION_FILTER);

				if (f != null) {
					try {
						new NSBMDWriter(contentAccessor.getResource(), settings, log).write(f);
					} catch (Exception ex) {
						DialogUtils.showErrorMessage(uiParent, "An error occured", ex.getMessage());
						ex.printStackTrace();
					}
				}
			}

			if (!log.errors.isEmpty()) {
				log.popupErrDialog(uiParent);
				for (String err : log.errors) {
					System.err.println(err);
				}
			}
		}
	}

	private void csnns_ExportBCA(Frame uiParent, NGCSContentAccessor contentAccessor) {
		Model skelMdl;
		if ((skelMdl = contentAccessor.getSupplementaryModelForExport(true)) != null) {
			NSBCAExportDialog dialog = new NSBCAExportDialog(uiParent, true);
			dialog.setVisible(true);

			NSBCAExportSettings settings = dialog.getResult();

			if (settings != null) {
				FSFile f = XFileDialog.openSaveFileDialog(NSBCAWriter.EXTENSION_FILTER);

				if (f != null) {
					List<SkeletalAnimation> sklAnm = contentAccessor.getSklAnime();

					new NSBCAWriter(settings, skelMdl.skeleton, sklAnm.toArray(new SkeletalAnimation[sklAnm.size()])).write(f);
				}
			}
		}
	}

	private void csnns_ExportBVA(Frame uiParent, NGCSContentAccessor contentAccessor) {
		Model skelMdl;
		if ((skelMdl = contentAccessor.getSupplementaryModelForExport(true)) != null) {
			FSFile f = XFileDialog.openSaveFileDialog(NSBVAWriter.EXTENSION_FILTER);

			if (f != null) {
				List<VisibilityAnimation> visAnm = contentAccessor.getVisAnime();

				new NSBVAWriter(skelMdl.skeleton, visAnm.toArray(new VisibilityAnimation[visAnm.size()])).write(f);
			}
		}
	}

	private static void csnns_ExportBTA(Frame uiParent, NGCSContentAccessor contentAccessor) {
		FSFile f = XFileDialog.openSaveFileDialog(NSBTAWriter.EXTENSION_FILTER);

		if (f != null) {
			List<MaterialAnimation> matAnm = new ArrayList<>();
			for (MaterialAnimation a : contentAccessor.getMatAnime()) {
				boolean hasNonPatternTrack = false;
				for (MatAnimBoneTransform bt : a.bones) {
					if (bt.hasCoordinator(0)) {
						hasNonPatternTrack = true;
						break;
					}
				}
				if (hasNonPatternTrack) {
					matAnm.add(a);
				}
			}
			NGCSNNSWriterLogger log = new NGCSNNSWriterLogger();
			new NSBTAWriter(log, matAnm.toArray(new MaterialAnimation[matAnm.size()])).write(f);
			log.popupErrDialog(uiParent);
		}
	}
}
