package ctrmap.formats.ntr.nitrowriter.nsbmd.dl;

import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.SBCJointTracker;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Skeleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DLSubMeshManager implements Iterable<DLSubMesh> {

	private MeshAttributes meshAttributes;

	private List<DLSubMesh> subMeshes = new ArrayList<>();

	public DLSubMeshManager(MeshAttributes meshAttributes) {
		this.meshAttributes = meshAttributes;
		subMeshes.add(createSubMesh());
	}

	private DLSubMesh createSubMesh() {
		return new DLSubMesh(meshAttributes);
	}
	
	public void removeOverSubMeshes(int count){
		for (int i = 0; i < count; i++){
			if (!subMeshes.isEmpty()){
				subMeshes.remove(subMeshes.size() - 1);
			}
		}
	}

	public void notifyJoints(Skeleton skeleton, JointBinding... bindings) {	
		DLSubMesh sm = getCurrentSubMesh();

		List<Integer> requiredIDs = new ArrayList<>();
		List<JointBinding> requiredSmoSkBindings = new ArrayList<>();

		for (JointBinding binding : bindings) {
			if (sm.getJointMtxIdExistOnly(binding) == -1) {

				if (binding.isSmoSk()) {
					requiredSmoSkBindings.add(binding);
				}

				for (int jntIdx : binding.jointIds) {
					Joint j = skeleton.getJoint(jntIdx);
					while (j != null) {
						int idx = j.getIndex();
						if (!sm.hasMtxForIndex(idx) && !requiredIDs.contains(idx)) {
							requiredIDs.add(idx);
						}
						j = skeleton.getJoint(j.parentName);
						//j = null;
					}
				}
			}
		}

		int indexCount = requiredIDs.size() + requiredSmoSkBindings.size();

		if (!sm.canFitIndicesCount(indexCount)) {
			subMeshes.add(createSubMesh());

			if (!getCurrentSubMesh().canFitIndicesCount(indexCount)) {
				throw new RuntimeException("Could not create inconsistent primitive sub-mesh! Too many required joint indices (" + indexCount + ")");
			}

			notifyJoints(skeleton, bindings); //need to rebuild dependency tree for new submesh
			return;
		}

		if (SBCJointTracker.SBCJOINTTRACKER_DEBUG) {
			//System.out.println("Loading dependency joints " + requiredIDs + " for " + binding.toString(skeleton));
		}

		for (Integer reqId : requiredIDs) {
			sm.getJointMtxId(new JointBinding(reqId));
		}

		for (JointBinding jb : requiredSmoSkBindings) {
			sm.getJointMtxId(jb);
		}
	}

	public DLSubMesh getCurrentSubMesh() {
		return subMeshes.get(subMeshes.size() - 1);
	}

	public int getSubMeshCount() {
		return subMeshes.size();
	}

	@Override
	public Iterator<DLSubMesh> iterator() {
		return subMeshes.iterator();
	}

}
