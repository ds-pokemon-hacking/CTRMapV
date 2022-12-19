
import ctrmap.CTRMapVResources;
import ctrmap.Launc;
import ctrmap.editor.CTRMapMenuActions;
import ctrmap.editor.gui.editors.gen5.level.VLevelEditor;
import ctrmap.editor.gui.editors.gen5.level.VZoneEditor;
import ctrmap.editor.gui.editors.gen5.level.building.VPropEditor;
import ctrmap.editor.gui.editors.gen5.level.camera.VCameraEditor;
import ctrmap.editor.gui.editors.gen5.level.entities.VEventEditor;
import ctrmap.editor.gui.editors.gen5.level.entities.VProxyEditor;
import ctrmap.editor.gui.editors.gen5.level.entities.VNPCEditor;
import ctrmap.editor.gui.editors.gen5.level.entities.VScriptingAssistant;
import ctrmap.editor.gui.editors.gen5.level.entities.VTriggerEditor;
import ctrmap.editor.gui.editors.gen5.level.entities.VWarpEditor;
import ctrmap.editor.gui.editors.gen5.level.extra.VExtrasPanel;
import ctrmap.editor.gui.editors.gen5.level.rail.VRailEditor;
import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.editor.gui.workspace.ROMExportDialog;
import ctrmap.editor.system.juliet.CTRMapPluginInterface;
import ctrmap.editor.system.juliet.ICTRMapPlugin;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.ntr.rom.srl.NDSROM;
import ctrmap.util.tools.cont.ContainerUtil;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import rtldr.JRTLDRCore;
import xstandard.fs.FSFile;
import xstandard.fs.VFSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.fs.accessors.MemoryFile;
import xstandard.fs.accessors.ProxyFile;
import xstandard.fs.accessors.arc.ArcFile;
import xstandard.fs.accessors.arc.ArcInput;
import xstandard.gui.DialogUtils;

public class CTRMapPlugin implements ICTRMapPlugin {

	public CTRMapPlugin() {
		System.out.println("Loading CTRMapV...");
		CTRMapVResources.load();
	}

	public static void main(String[] args) {
		JRTLDRCore.suppressDebugPluginByFileName("CTRMapV.jar");
		JRTLDRCore.addDebugSelfClassLoader(CTRMapPlugin.class.getProtectionDomain().getCodeSource());
		Launc.main(null);
	}

	@Override
	public void registPerspectives(CTRMapPluginInterface j) {
		j.rmoRegistPerspective(VLevelEditor.class);
	}

	@Override
	public void registEditors(CTRMapPluginInterface j) {
		j.rmoRegistToolbarEditors(VLevelEditor.class,
			VProxyEditor.class,
			VNPCEditor.class,
			VWarpEditor.class,
			VTriggerEditor.class,
			VPropEditor.class,
			VScriptingAssistant.class,
			VRailEditor.class,
			VCameraEditor.class,
			ScenegraphExplorer.class
		);
		j.rmoRegistTabbedEditors(VLevelEditor.class,
			VZoneEditor.class,
			VEventEditor.class,
			VExtrasPanel.class
		);
	}

	private void loadVFS(MemoryFile destDir, VFSFile src) {
		for (VFSFile child : src.listFiles()) {
			FSFile ov = child.getOvFile();
			FSFile base = child.getBaseFile();
			if (base == null || !base.exists()) {
				destDir.linkChild(new ProxyFile(ov, ov.getPathRelativeTo(src.getVFS().getOvFSRoot()))); //use entire overlay file directly
			} else {
				//merge ovfs into basefs
				if (!ov.exists()) {
					destDir.linkChild(new ProxyFile(base, base.getPathRelativeTo(src.getVFS().getBaseFSRoot())));
				} else {
					if (base instanceof ArcFile) {
						ArcFile arc = (ArcFile) base;
						ArcInput[] inputs = src.getVFS().getArcInputs(ov, ov).toArray(new ArcInput[0]);
						if (inputs.length > 0) {
							MemoryFile newArc = new MemoryFile(arc.getName(), arc.getBytes());
							ArcFile newArcFileObj = new ArcFile(newArc, src.getVFS().getArcFileAccessor());
							src.getVFS().getArcFileAccessor().writeToArcFile(newArcFileObj, null, inputs);
							arc = newArcFileObj;
						}
						destDir.linkChild(new ProxyFile(arc.getSource(), base.getPathRelativeTo(src.getVFS().getBaseFSRoot())));
					} else if (base.isDirectory()) {
						MemoryFile subDir = destDir.createChildDir(base.getName());
						loadVFS(subDir, child);
					} else {
						destDir.linkChild(new ProxyFile(ov, ov.getPathRelativeTo(src.getVFS().getOvFSRoot())));
					}
				}
			}
		}
	}

	@Override
	public void registUI(CTRMapPluginInterface j, GameInfo game) {
		if (game.isGenV()) {
			//Export ROM
			j.rmoAddMenuItem("File", "Export ROM", (cm) -> {
				if (cm.getGame().isGenV()) {
					if (cm.saveData()) {
						ROMExportDialog dlg = new ROMExportDialog(cm, true);
						CTRMapProject proj = cm.getProject();
						String last = proj.getAttribute("LastROMPath");
						if (last != null) {
							dlg.setSelectedPath(last);
						}
						dlg.setVisible(true);
						String path = dlg.getSelectedPath();
						if (path != null) {
							proj.setAttribute("LastROMPath", path);
							proj.saveProjectData();

							MemoryFile romRoot = new MemoryFile("__ROM");
							loadVFS(romRoot, proj.wsfs.vfs.getVFSRoot());

							try {
								NDSROM.buildROM(romRoot, new DiskFile(path));
							} catch (IOException ex) {
								Logger.getLogger(CTRMapMenuActions.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					}
				} else {
					DialogUtils.showErrorMessage(cm, "Not supported", "ROM exporting is an NDS exclusive feature,");
				}
			}).setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));

			//GFContainer editor
			//READ ME: The reason why this is in the Gen V plug-in instead of the core (even though GFContainer
			//support is part of the main CM-CE codebase) is that as far as the plug-in goes, we explicitly set the
			//ContentIdentifier to null here as Gen V doesn't have one programmed. That's indeed okay in the scope
			//of the plug-in, but wouldn't be nowhere near as okay if strapped into the core in a case where another
			//plug-in would have their own ContentIdentifier that they couldn't use without adding a duplicate button,
			//since core features are non-removable and non-modifiable.
			//Example: if someone were to resurrect CTRMap as a Gen VI editor, which ContainerUtil was originally
			//adapted from, they could use the handy file magics that XY/ORAS's formats use everywhere to assist  
			//the utility in detecting content, and as such would have their own menu option that would bind their
			//improved ContentIdentifier accordingly. Needless to say, that most likely will not happen, but 
			//abstracting stuff away is always better than removing semi-working code.
			j.rmoAddMenuItem("Tools", "GFContainer editor", (cm) -> {
				ContainerUtil ut = new ContainerUtil();
				ut.setContentIdentifier(null);
				ut.setLocationRelativeTo(cm);
				ut.setVisible(true);
			});

			j.rmoAddAboutDialogCredits(
				"Martin Korth - GBATek Nintendo DS hardware documentation",
				"Scurest - Apicula / NDS 3D file format research",
				"CUE - NDS de/compression code",
				"devkitPro - NDS ROM format documentation",
				"Kaphotics - Zone/entity research",
				"Gonhex - RailSystem structure foundation"
			);
			j.rmoAddAboutDialogSpecialThanks(
				"Bond697 - initial scripting research",
				"Turtleisaac - proxy nomenclature",
				"Dmitry - a deal on IDA I couldn't refuse"
			);
		}
	}
}
