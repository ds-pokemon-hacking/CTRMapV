package ctrmap.formats.ntr.nitroreader.nsbca;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.util.ArrayList;
import java.util.List;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Skeleton;
import xstandard.INamed;
import java.io.IOException;

public class NSBCAAnimation implements INamed {

	public String name;
	private int frameCount;
	private List<NSBCATransform> nodes;

	public NSBCAAnimation(NTRDataIOStream data, String name) throws IOException {
		this.name = name;
		int JAC_offset = data.getPosition();
		String magic = data.readPaddedString(4);
		frameCount = data.readUnsignedShort();
		int jntCount = data.readUnsignedShort();
		int anmFlag = data.readInt();
		int mtx3Offset = data.readInt();
		int mtx5Offset = data.readInt();
		List<Integer> nodeOffsets = new ArrayList(jntCount);
		for (int i = 0; i < jntCount; i++) {
			nodeOffsets.add(data.readUnsignedShort());
		}
		data.align(4);
		this.nodes = new ArrayList(jntCount);
		for (Integer node_offset : nodeOffsets) {
			data.seek(node_offset + JAC_offset);
			nodes.add(new NSBCATransform(data, new NSBCATransformHeader(data), JAC_offset, mtx3Offset, mtx5Offset, frameCount));
		}
	}

	SkeletalAnimation toGeneric(Skeleton skl) {
		SkeletalAnimation anm = new SkeletalAnimation();
		anm.frameCount = frameCount;
		anm.name = name;

		for (int i = 0; i < nodes.size(); i++) {
			NSBCATransform transform = nodes.get(i);
			SkeletalBoneTransform bt = transform.toGeneric();
			Joint j = skl.getJoint(transform.header.jointId);
			if (j != null) {
				bt.name = j.name;
				anm.bones.add(bt);
			}
		}

		return anm;
	}

	public List<NSBCATransform> getNodes() {
		return this.nodes;
	}

	public int getNumFrames() {
		return this.frameCount;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
