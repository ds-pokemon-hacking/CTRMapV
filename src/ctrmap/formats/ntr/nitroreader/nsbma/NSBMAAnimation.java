package ctrmap.formats.ntr.nitroreader.nsbma;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.util.ArrayList;
import java.util.List;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import xstandard.INamed;
import java.io.IOException;

public class NSBMAAnimation implements INamed {

	public String name;
	public int frameCount;

	private List<NSBMATrack> tracks;

	public NSBMAAnimation(NTRDataIOStream data, String name) throws IOException {
		this.name = name;
		data.setBaseHere();
		String magic = data.readPaddedString(4);
		frameCount = data.readUnsignedShort();
		int flags = data.readUnsignedShort();
		NNSPatriciaTreeReader.Entry[] entries = NNSPatriciaTreeReader.readTree(data);
		tracks = new ArrayList(entries.length);
		for (NNSPatriciaTreeReader.Entry header : entries) {
			tracks.add(new NSBMATrack(data, frameCount, header));
		}
		data.resetBase();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public MaterialAnimation toGeneric() {
		MaterialAnimation a = new MaterialAnimation();
		a.name = name;
		a.frameCount = frameCount;
		for (NSBMATrack t : tracks) {
			a.bones.add(t.toGeneric());
		}
		return a;
	}
}
