package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;
import ctrmap.renderer.scene.model.Joint;
import java.io.IOException;
import xstandard.math.BitMath;

public class NODEDESC extends SBCCommand {

	private int jointId;
	private int parentJointId;
	
	//This will write the joint's inverse scale into the scaling cache for use with useSSC children
	private boolean storeSSCMatrix;
	private boolean useSSC;

	private int srcStackPos = -1;
	private int dstStackPos = -1;

	public NODEDESC(NTRDataIOStream data, int flags) throws IOException {
		jointId = data.read();
		parentJointId = data.read();
		int jointFlags = data.read();
		useSSC = BitMath.checkIntegerBit(jointFlags, 0);
		storeSSCMatrix = BitMath.checkIntegerBit(jointFlags, 1);

		if (BitMath.checkIntegerBit(flags, 0)) {
			dstStackPos = data.read() & 31;
		}
		if (BitMath.checkIntegerBit(flags, 1)) {
			srcStackPos = data.read() & 31;
		}
		//System.out.println("node " + NodeID + " parent " + ParentNodeID + " from " + SrcIdx + " to " + DestIdx);
	}

	@Override
	public void toGeneric(NSBMDModelConverter conv) {
		if (srcStackPos != -1) {
			conv.loadMatrix(srcStackPos);
		}

		Joint jnt = conv.getJointById(jointId);
		
		if (jnt == null){
			throw new NullPointerException("Could not resolve joint " + jointId + " (Model " + conv.getModelName() + ")");
		}

		//Pokemon uses parentID=thisID, FFIII Steam models use 255 for root bone
		if (parentJointId != 255 && parentJointId != jointId) {
			Joint pJnt = conv.getJointById(parentJointId);
			if (pJnt == null) {
				System.err.println("Could not resolve joint ID " + parentJointId + "!!");
			} else {
				jnt.parentName = pJnt.name;
			}
			if (useSSC) {
				//Not sure if the actual scale compensation technique is correct, but this flag seems to work most of the time
				jnt.setScaleCompensate(true);
			}
		}

		conv.setJointMatrix(jnt.getLocalMatrix(), jointId);
		conv.scaleExternalReset();

		if (dstStackPos != -1) {
			conv.storeMatrix(dstStackPos);
		}
	}
}
