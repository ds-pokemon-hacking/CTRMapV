package ctrmap.formats.ntr.nitroreader.nsbta;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import java.util.ArrayList;
import java.util.List;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import xstandard.INamed;
import java.io.IOException;

public class NSBTAAnimation implements INamed {

	public String name;

	private int flags;
	public int frameCount;
	public List<NSBTATrack> transforms;

	public NSBTAAnimation(NTRDataIOStream data, String name) throws IOException {
		this.name = name;
		data.setBaseHere();
		String magic = data.readPaddedString(4);
		frameCount = data.readUnsignedShort();
		flags = data.read();
		data.skipBytes(1);
		NNSPatriciaTreeReader.Entry[] entries = NNSPatriciaTreeReader.readTree(data);
		transforms = new ArrayList(entries.length);
		for (NNSPatriciaTreeReader.Entry entry : entries) {
			transforms.add(new NSBTATrack(data, frameCount, entry));
		}
		data.resetBase();
	}

	public MaterialAnimation toGeneric() {
		MaterialAnimation mta = new MaterialAnimation();
		mta.frameCount = frameCount - 1;
		mta.name = name;

		for (NSBTATrack srt : transforms) {
			MatAnimBoneTransform bt = srt.toGeneric();
			mta.bones.add(bt);

			if ((flags & 2) != 0) {
				for (List<KeyFrame> kfl : bt.getAllKfLists()) {
					if (kfl.size() > 1) {
						KeyFrame end = new KeyFrame(frameCount, kfl.get(0).value);
						kfl.add(end);
					}
				}
			}
		}

		return mta;
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
