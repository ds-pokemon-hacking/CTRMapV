package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;
import xstandard.math.vec.Matrix4;
import xstandard.util.collections.FloatList;
import xstandard.util.collections.IntList;
import java.io.IOException;

public class NODEMIX extends SBCCommand {

	private int destIdx;
	private int[][] matrices;

	public NODEMIX(NTRDataIOStream data) throws IOException {
		destIdx = data.read();
		int influenceCount = data.read();
		matrices = new int[influenceCount][3];
		for (int i = 0; i < influenceCount; i++) {
			matrices[i][0] = data.read(); //srcIdx
			matrices[i][1] = data.read(); //jointId
			matrices[i][2] = data.read(); //weight
		}
	}

	@Override
	public void toGeneric(NSBMDModelConverter conv) {
		IntList jointMtxIndices = new IntList();
		FloatList jointWeights = new FloatList();

		float weightSum = 0f;
		int influenceCount = matrices.length;
		for (int i = 0; i < influenceCount; i++) {
			IntList srcJiList = conv.jointIds[matrices[i][0]];
			if (srcJiList.size() != 1 || srcJiList.get(0) != matrices[i][1]) {
				throw new RuntimeException("Want joint " + matrices[i][1] + ", got " + srcJiList.get(0));
			}
			jointMtxIndices.add(matrices[i][1]);
			float weight = matrices[i][2] / 255f;
			weight = Math.min(weight, 1f - weightSum);
			jointWeights.add(weight);
			weightSum += weight;
		}

		Matrix4 skinnedMtx = new Matrix4();
		skinnedMtx.zero();
		Matrix4[] mt = new Matrix4[influenceCount];
		for (int mi = 0; mi < influenceCount; mi++) {
			Matrix4 src = conv.getMvStack(matrices[mi][0]);
			Matrix4 dst = new Matrix4(src);
			dst.mul(conv.mdl.invBindPoseMatrices.get(matrices[mi][1]));
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					dst.set(i, j, dst.get(i, j) * (matrices[mi][2] / 255f));
				}
			}
			mt[mi] = dst;
		}
		for (Matrix4 m : mt){
			skinnedMtx.add(m);
		}
		//afaik there's no need to actually calculate the skinning since this is just a converter for the data
		//in theory, the matrices calculated with these commands can be used further
		// - such is the case when calculating parent transforms
		//however, afaik you can't exactly parent a bone to a blended matrix, so we are sure that
		//the matrix at destIdx will be left unused

		conv.loadIdentity();
		conv.setMatrixCacheDataFromJoint(destIdx, skinnedMtx, jointMtxIndices, jointWeights);
		conv.setJointDataFromMtxCache(destIdx);

		conv.scaleExternalReset();
	}
}
