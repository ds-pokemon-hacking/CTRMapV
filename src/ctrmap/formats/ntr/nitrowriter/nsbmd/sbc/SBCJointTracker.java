package ctrmap.formats.ntr.nitrowriter.nsbmd.sbc;

import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.mtx.stack.MtxStkLoad;
import ctrmap.formats.ntr.nitrowriter.nsbmd.dl.DLSubMesh;
import ctrmap.formats.ntr.nitrowriter.nsbmd.dl.JointBinding;
import ctrmap.formats.ntr.nitrowriter.nsbmd.dl.NitroMeshResource;
import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands.NodeMtxApply;
import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.commands.NodeMtxBlendApply;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Skeleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

public class SBCJointTracker {

	public static final boolean SBCJOINTTRACKER_DEBUG = false;

	private Map<JointBinding, Integer> jointMtxBindings;
	private Map<Integer, JointBinding> jointMtxBindingsInv = new HashMap<>();

	private Map<JointBinding, List<MtxStkLoad>> mtxLoadCmds = new HashMap<>();

	private List<JointBinding> loadedJoints = new ArrayList<>();
	private List<JointBinding> jointsLastUsedHere = new ArrayList<>();

	private Skeleton skl;

	private String dbgShpName;

	public SBCJointTracker(NitroMeshResource shape, Skeleton skeleton) {
		jointMtxBindings = shape.jointMtxBindings;
		skl = skeleton;
		dbgShpName = shape.name;

		for (Map.Entry<JointBinding, Integer> e : jointMtxBindings.entrySet()) {
			jointMtxBindingsInv.put(e.getValue(), e.getKey());
		}

		for (GECommand cmd : shape.displayList) {
			if (cmd instanceof MtxStkLoad) {
				MtxStkLoad load = (MtxStkLoad) cmd;
				List<MtxStkLoad> list = mtxLoadCmds.get(jointMtxBindingsInv.get(load.pos));
				if (list == null) {
					list = new ArrayList<>();
					mtxLoadCmds.put(jointMtxBindingsInv.get(load.pos), list);
				}
				list.add(load);
			}
		}

		if (SBCJOINTTRACKER_DEBUG) {
			System.out.println("INITIALIZED JOINT TRACKER FOR SHAPE " + shape.name);
			System.out.println("Joint bindings: ");
			for (JointBinding jb : jointMtxBindings.keySet()) {
				System.out.println(jb.toString(skl));
			}
		}
	}

	public Set<JointBinding> getJBs() {
		return jointMtxBindings.keySet();
	}

	public boolean hasJB(JointBinding jb) {
		return jointMtxBindings.containsKey(jb);
	}

	public void setJBLastUsedHere(JointBinding jb) {
		jointsLastUsedHere.add(jb);
	}

	public boolean isJBLastUsedHere(JointBinding jb) {
		return jointsLastUsedHere.contains(jb);
	}

	public int getMaxIdx() {
		int out = -1;
		for (Integer idx : jointMtxBindings.values()) {
			out = Math.max(out, idx);
		}
		return out;
	}

	public void addToSBC(SBC sbc) {
		if (SBCJOINTTRACKER_DEBUG) {
			System.out.println("CREATING JOINT SBC FOR SHAPE " + dbgShpName);
		}
		List<JointBinding> jointsToLoad = new ArrayList<>(jointMtxBindings.keySet());

		//Sort the joints so that all parent joints precede the others.
		//Smooth skinned joints always go last (they're just really nice like that you know)
		List<JointBinding> jointsToBeLoadedSorted = getJBsByParentName(null, jointsToLoad, skl);

		//Now only smooth skinned joints should be left
		for (JointBinding jb : jointsToLoad) {
			if (jb.isSmoSk()) {
				jointsToBeLoadedSorted.add(jb);
			}
		}

		jointsToBeLoadedSorted.removeAll(loadedJoints);

		boolean isSingleJnt = jointMtxBindings.size() == 1;

		for (JointBinding jb : jointsToBeLoadedSorted) {
			if (SBCJOINTTRACKER_DEBUG) {
				System.out.println("Preparing joint " + jb.toString(skl));
			}
			if (jb.isRgdSk()) {
				NodeMtxApply cmd = new NodeMtxApply();
				//if (!isSingleJnt) {
					//If it IS a single joint binding, then it's pointless to store since the value remains in the register
					cmd.dstMtx = jointMtxBindings.get(jb);
				//}
				String parentName = skl.getJoint(jb.jointIds[0]).parentName;
				if (parentName != null) {
					Joint parent = skl.getJoint(parentName);
					cmd.parentJntId = parent.getIndex();
					JointBinding parentJB = new JointBinding(cmd.parentJntId);

					if (!jointMtxBindings.containsKey(parentJB)) {
						Stack<Integer> parentJntIdStack = new Stack<>();

						Joint p = parent;
						while (p != null) {
							int idx = p.getIndex();
							JointBinding testPIdxJB = new JointBinding(idx);
							parentJntIdStack.push(idx);
							if (jointMtxBindings.containsKey(testPIdxJB)) {
								break;
							}
							p = p.getParent();
						}

						int lastPIdx = 0;
						int lastExistingStkIdx = -1;
						int dstMtx = jointMtxBindings.get(jb);
						
						System.out.println("needs setup parent for " + jb.toString(skl) + " submesh " + dbgShpName);
						while (!parentJntIdStack.isEmpty()) {
							int pIdx = parentJntIdStack.pop();
							JointBinding pJB = new JointBinding(pIdx);
							if (!jointMtxBindings.containsKey(pJB)) {
								NodeMtxApply pCmd = new NodeMtxApply();
								pCmd.parentJntId = lastPIdx;
								pCmd.dstMtx = dstMtx;
								pCmd.srcMtx = lastExistingStkIdx;
								pCmd.jntId = pIdx;
								lastExistingStkIdx = pCmd.dstMtx;
								sbc.addCommand(pCmd);
								System.out.println("add setup command srcmtx " + pCmd.srcMtx + " jntid " + pCmd.jntId + " dstmtx " + pCmd.dstMtx);
							} else {
								lastExistingStkIdx = jointMtxBindings.get(pJB);
							}
							lastPIdx = pIdx;
						}
						
						cmd.srcMtx = dstMtx;
						cmd.dstMtx = dstMtx;
					} else {
						try {
							cmd.srcMtx = getJBIndexForLoading(cmd.parentJntId);
						} catch (NullPointerException ex) {
							throw new NullPointerException("Matrix not preloaded ! ! - joint " + parent.name + "(idx " + cmd.parentJntId + ")\n"
								+ "Needed by: " + skl.getJoint(jb.jointIds[0]).name + "(idx " + jb.jointIds[0] + ")");
						}
					}
				}
				cmd.jntId = jb.jointIds[0];
				if (SBCJOINTTRACKER_DEBUG) {
					System.out.println("joint " + cmd.jntId + " matrix registered at stack pos " + cmd.dstMtx);
				}
				sbc.addCommand(cmd);
			} else {
				NodeMtxBlendApply cmd = new NodeMtxBlendApply();
				cmd.dstIdx = jointMtxBindings.get(jb);

				for (int i = 0; i < jb.jointIds.length; i++) {
					NodeMtxBlendApply.NodeMtxBlendSource src = new NodeMtxBlendApply.NodeMtxBlendSource();
					src.jntId = jb.jointIds[i];
					src.weight = jb.weights[i];
					src.srcIdx = getJBIndexForLoading(src.jntId);
					cmd.sources.add(src);
					if (SBCJOINTTRACKER_DEBUG) {
						System.out.println("creating joint blend source " + src.jntId + " from matrix " + src.srcIdx);
					}
				}
				if (SBCJOINTTRACKER_DEBUG) {
					System.out.println("blend matrix registered at stack pos " + cmd.dstIdx);
				}

				sbc.addCommand(cmd);
			}
		}
	}

	private List<JointBinding> getJBsByParentName(String parentName, List<JointBinding> jbs, Skeleton skl) {
		List<JointBinding> l = new ArrayList<>();

		for (Joint jnt : skl.getJoints()) {
			if (Objects.equals(jnt.parentName, parentName)) {
				int idx = jnt.getIndex();

				for (JointBinding jb : jbs) {
					if (jb.isRgdSk() && jb.jointIds[0] == idx) {
						l.add(jb);
					}
				}

				l.addAll(getJBsByParentName(jnt.name, jbs, skl));
			}
		}

		return l;
	}

	public void setJBIsLoaded(JointBinding jb) {
		loadedJoints.add(jb);
	}

	public boolean isJBLoaded(JointBinding jb) {
		return loadedJoints.contains(jb);
	}

	public boolean hasNonLoadedJBs() {
		return loadedJoints.size() != jointMtxBindings.size();
	}

	public int getJBIndexForLoading(JointBinding jb) {
		return jointMtxBindings.get(jb);
	}

	public int getJBIndexForLoading(int jointIndex) {
		JointBinding jb = new JointBinding(jointIndex);
		if (!jointMtxBindings.containsKey(jb)) {
			System.err.println(" ------------- ERROR ---------------");
			System.err.println("Available joints: ");
			for (Map.Entry<JointBinding, Integer> e : jointMtxBindings.entrySet()) {
				for (int i = 0; i < e.getKey().jointIds.length; i++) {
					if (i != 0) {
						System.err.print("/");
					}
					System.err.print(skl.getJoint(e.getKey().jointIds[i]).name + "(" + e.getKey().jointIds[i] + ")");
				}

				System.err.print(" : at : ");
				System.err.println(e.getValue());
			}

			throw new NullPointerException("Could not find joint binding for joint " + jointIndex);
		}
		return jointMtxBindings.get(jb);
	}

	private void remapJBToExisting(JointBinding jb, int newIndex) {
		int oldIndex = jointMtxBindings.get(jb);
		remapJBToExisting(jb, newIndex, oldIndex);
	}

	private void remapJBToExisting(JointBinding jb, int newIndex, int oldIndex) {
		if (jointMtxBindingsInv.containsKey(newIndex)) {
			if (isJBLoaded(jointMtxBindingsInv.get(newIndex))) {
				throw new UnsupportedOperationException("Can not swap a pre-loaded matrix! - " + newIndex + " (" + jointMtxBindingsInv.get(newIndex).toString(skl) + ")");
			} else {
				JointBinding swap = jointMtxBindingsInv.get(newIndex);
				jointMtxBindingsInv.put(oldIndex, swap);
				jointMtxBindings.put(swap, oldIndex);
				changeRestoreMtxCommands(swap, oldIndex);
			}
		} else {
			jointMtxBindingsInv.remove(oldIndex);
		}
		jointMtxBindingsInv.put(newIndex, jb);
		jointMtxBindings.put(jb, newIndex);
		if (SBCJOINTTRACKER_DEBUG) {
			System.out.println("put index " + newIndex + " for jb " + jb.toString(skl));
		}
		changeRestoreMtxCommands(jb, newIndex);
	}

	private void changeRestoreMtxCommands(JointBinding key, int to) {
		if (mtxLoadCmds.containsKey(key)) {
			if (SBCJOINTTRACKER_DEBUG) {
				System.out.println("remapping joint " + key.jointIds[0] + " to stk pos " + to);
			}
			for (MtxStkLoad load : mtxLoadCmds.get(key)) {
				load.pos = to;
			}
		} else {
			if (SBCJOINTTRACKER_DEBUG) {
				System.out.println("No mtx restore commands for joint " + key.toString(skl) + ", would remap to stkidx " + to);
			}
		}
	}

	public static final boolean SBCJOINTTRACKER_SYNCDUMP = false;

	public void syncToLastTracker(SBCJointTracker tracker) {
		if (SBCJOINTTRACKER_DEBUG) {
			System.out.println("SYNCING TRACKER " + dbgShpName + " WITH TRACKER " + tracker.dbgShpName);
		}

		if (SBCJOINTTRACKER_SYNCDUMP) {
			for (Map.Entry<JointBinding, Integer> readyBinding : tracker.jointMtxBindings.entrySet()) {
				System.out.println(readyBinding.getKey().toString(skl) + " was last at " + readyBinding.getValue());
			}
		}

		List<Map.Entry<JointBinding, Integer>> readyBindings = new ArrayList<>(tracker.jointMtxBindings.entrySet());
		List<Map.Entry<JointBinding, Integer>> smoSkBindings = new ArrayList<>();

		//WORKAROUND for janky sorting algorithm
		for (Map.Entry<JointBinding, Integer> b : readyBindings) {
			if (b.getKey().isSmoSk()) {
				smoSkBindings.add(b);
			}
		}

		readyBindings.removeAll(smoSkBindings);

		readyBindings.sort((Map.Entry<JointBinding, Integer> o1, Map.Entry<JointBinding, Integer> o2) -> {
			JointBinding jb1 = o1.getKey();
			JointBinding jb2 = o2.getKey();
			if (jb1.isNoJoint()) {
				return -1;
			}
			if (jb2.isNoJoint()) {
				return 1;
			}
			/*if (jb1.isSmoSk()) {
				return 1;
			}
			if (jb2.isSmoSk()) {
				return -1;
			}*/
			return jb1.jointIds[0] - jb2.jointIds[0];
		});

		readyBindings.addAll(smoSkBindings);

		for (Map.Entry<JointBinding, Integer> readyBinding : readyBindings) {
			if (jointMtxBindings.containsKey(readyBinding.getKey())) {
				if (SBCJOINTTRACKER_DEBUG) {
					System.out.println("transferring existing matrix... " + readyBinding.getKey().jointIds[0] + "(stkidx " + readyBinding.getValue() + ")");
				}
				remapJBToExisting(readyBinding.getKey(), readyBinding.getValue());
				setJBIsLoaded(readyBinding.getKey());
			}
		}

		for (Map.Entry<JointBinding, Integer> readyBinding : readyBindings) {
			if (!jointMtxBindings.containsKey(readyBinding.getKey()) && !tracker.isJBLastUsedHere(readyBinding.getKey())) {
				//find unoccupied index if any
				int reqIndex = readyBinding.getValue();

				if (loadedJoints.contains(jointMtxBindingsInv.get(reqIndex))) {
					//Can not override an already allocated stack pos
					continue;
				}

				for (int i = 0; i < DLSubMesh.SUBMESH_STACK_MAX; i++) {
					if (!jointMtxBindingsInv.containsKey(i)) {
						if (SBCJOINTTRACKER_DEBUG) {
							System.out.println("transferring existing prophetic matrix... " + readyBinding.getKey().toString(skl) + " (reqIndex " + reqIndex + ", replIndex " + i + ")");
						}
						remapJBToExisting(readyBinding.getKey(), reqIndex, i);
						setJBIsLoaded(readyBinding.getKey());
						break;
					}
				}
			}
		}
	}
}
