package ctrmap.formats.ntr.nitrowriter.nsbmd.dl;

import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import xstandard.util.collections.FloatList;
import xstandard.util.collections.IntList;
import java.util.Arrays;

public class JointBinding {

	public int[] jointIds;
	public short[] weights;

	public JointBinding(int idx) {
		jointIds = new int[]{idx};
		weights = new short[]{255};
		if (idx == 255){
			throw new RuntimeException("That JointBinding is a lil' sussy.\n (No, seriously, a joint ID can not be 255, because that's the maximum joint count and we are 0-indexed.");
		}
	}

	public JointBinding(Vertex vtx) {
		float weightSum = 0;

		IntList jointIDList = new IntList();
		FloatList weightList = new FloatList();

		for (int i = 0; i < vtx.boneIndices.size(); i++) {
			int id = vtx.boneIndices.get(i);
			if (id == -1) {
				break;
			}
			if (i < vtx.weights.size()) {
				if (vtx.weights.get(i) != 0f) {
					float weight = vtx.weights.get(i);
					weightList.add(weight);
					weightSum += weight;
					jointIDList.add(id);
				}
			} else {
				weightList.add((1 - weightSum) / (vtx.boneIndices.size() - vtx.weights.size()));
				jointIDList.add(id);
			}
		}

		jointIds = jointIDList.toArray();
		weights = new short[weightList.size()];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = (short) Math.round(weightList.get(i) * 255);
		}
	}

	public String toString(Skeleton skl) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < jointIds.length; i++) {
			if (i != 0) {
				sb.append(" / ");
			}
			sb.append(skl.getJoint(jointIds[i]).name);
			sb.append("(");
			sb.append(jointIds[i]);
			sb.append(")");
		}

		return sb.toString();
	}

	public boolean isNoJoint() {
		return jointIds.length == 0;
	}

	public boolean isRgdSk() {
		return jointIds.length == 1;
	}

	public boolean isSmoSk() {
		return jointIds.length > 1;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o != null && o instanceof JointBinding) {
			JointBinding jb = (JointBinding) o;
			return Arrays.equals(jointIds, jb.jointIds) && Arrays.equals(weights, jb.weights);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + Arrays.hashCode(jointIds);
		hash = 67 * hash + Arrays.hashCode(weights);
		return hash;
	}

}
