package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc;

import ctrmap.formats.ntr.nitrowriter.nsbmd.NSBMDExportSettings;
import ctrmap.formats.ntr.nitrowriter.nsbmd.dl.JointBinding;
import ctrmap.formats.ntr.nitrowriter.nsbmd.dl.NitroMeshResource;
import ctrmap.formats.ntr.nitrowriter.nsbmd.mat.NitroMaterialResource;
import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands.EndSBC;
import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands.MaterialApply;
import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands.MtxStackLoad;
import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands.VisGroupApply;
import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands.PosScaleMul;
import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands.RendererCommand;
import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands.ShapeDraw;
import ctrmap.renderer.scene.model.Skeleton;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SBC {

	public static final boolean SBC_DEBUG = false;

	public int mtxStackTop = -1;

	private List<RendererCommand> commands = new ArrayList<>();

	public SBC(List<NitroMaterialResource> materials, List<NitroMeshResource> shapes, Skeleton skeleton, NSBMDExportSettings settings) {
		shapes = sortShapesByMaterial(shapes);

		int currentVisGroup = -1;
		NitroMaterialResource currentMaterial = null;
		SBCJointTracker lastJointTracker = null;

		SBCJointTracker[] trackers = new SBCJointTracker[shapes.size()];
		for (int i = 0; i < shapes.size(); i++) {
			trackers[i] = new SBCJointTracker(shapes.get(i), skeleton);
		}

		List<JointBinding> usedJBs = new ArrayList<>();

		for (int trkIdx = trackers.length - 1; trkIdx >= 0; trkIdx--) {
			for (JointBinding jb : trackers[trkIdx].getJBs()) {
				if (!usedJBs.contains(jb)) {
					usedJBs.add(jb);

					trackers[trkIdx].setJBLastUsedHere(jb);
				}
			}
		}

		boolean lastMeshModifiesMtx = false;

		for (int i = 0; i < Math.min(255, shapes.size()); i++) {
			NitroMeshResource shape = shapes.get(i);
			if (SBC_DEBUG) {
				System.out.println("sbc for shp " + shape.name);
			}

			//VisGroup binding for visibility animations
			int visGroupNo = 0;
			if (settings.jointsAsVisGroups) {
				visGroupNo = shape.isSingleJointRgdSk() ? shape.getSingleJointRgdSkJntNo() : 0;
			} else {
				visGroupNo = shape.visGroup;
			}

			SBCJointTracker trk = trackers[i];
			if (lastJointTracker != null) {
				trk.syncToLastTracker(lastJointTracker);
			}
			lastJointTracker = trk;

			boolean wasMtxRegisterUsed = false;

			if (currentVisGroup != visGroupNo) {
				addCommand(new VisGroupApply(visGroupNo, true));
			}

			JointBinding mainJointBinding = new JointBinding(shape.getSingleJointRgdSkJntNo());
			boolean isMainJointNewlyLoaded = !trk.isJBLoaded(mainJointBinding);

			if (lastMeshModifiesMtx || trk.hasNonLoadedJBs()) {
				wasMtxRegisterUsed = true;
			}
			if (trk.hasNonLoadedJBs()) {
				trk.addToSBC(this);
			}
			lastMeshModifiesMtx = shape.modifiesMatrixReg();
			mtxStackTop = Math.max(mtxStackTop, trk.getMaxIdx());

			currentVisGroup = visGroupNo;

			//The original models seem to call POSSCALE(true) sometimes before calling NODEDESC
			//This could be to scale back the current matrix to an object matrix loaded to the register before
			//IMO this is a very minor optimization that only frees up one matrix stack index.
			//Since we partly have the stack structure decided when we generate the display lists, I don't think I'll be implementing this functionality.
			if (wasMtxRegisterUsed && shape.isSingleJointRgdSk()) {
				if (!isMainJointNewlyLoaded) {
					addCommand(new MtxStackLoad(trk.getJBIndexForLoading(mainJointBinding)));
				}
				addCommand(new PosScaleMul(false));
			}

			//Material
			if (currentMaterial != shape.material) {
				int matIdx = materials.indexOf(shape.material);
				if (matIdx > 255) {
					System.err.println("WARNING: Omitting material " + 255 + " (ID too high)");
				} else {
					addCommand(new MaterialApply(matIdx));
					currentMaterial = shape.material;
				}
			}

			//Shape
			addCommand(new ShapeDraw(i));
		}

		if (shapes.size() > 255) {
			System.err.println("WARNING: Shape count over 255. Ignored " + (shapes.size() - 255) + " shapes.");
		}

		addCommand(new EndSBC());
	}

	public void write(DataOutput out) throws IOException {
		for (RendererCommand cmd : commands) {
			out.write(cmd.getOpCode().ordinal() | (cmd.calcFlags() << 5));
			cmd.writeParams(out);
		}
	}

	public final void addCommand(RendererCommand cmd) {
		commands.add(cmd);
	}

	private static List<NitroMeshResource> sortShapesByMaterial(List<NitroMeshResource> shapes) {
		List<NitroMeshResource> shapesSorted = new ArrayList<>();
		for (NitroMeshResource shp : shapes) {
			if (!shapesSorted.contains(shp)) {
				shapesSorted.add(shp);

				for (NitroMeshResource shp2 : shapes) {
					if (shp2.material == shp.material) {
						if (!shapesSorted.contains(shp2)) {
							shapesSorted.add(shp2);
						}
					}
				}
			}
		}
		return shapesSorted;
	}
}
