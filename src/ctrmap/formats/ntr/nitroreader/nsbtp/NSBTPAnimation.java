package ctrmap.formats.ntr.nitroreader.nsbtp;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import java.util.ArrayList;
import java.util.List;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import xstandard.INamed;
import java.io.IOException;

public class NSBTPAnimation implements INamed {

	public String name;
	public List<NSBTPTrack> tracks;
	public int frameCount;

	public NSBTPAnimation(NTRDataIOStream data, String name) throws IOException {
		this.name = name;
		data.setBaseHere();
		String magic = data.readPaddedString(4);
		frameCount = data.readUnsignedShort();
		int texNameCount = data.read();
		int paletteNameCount = data.read();
		int texNamesOffset = data.readUnsignedShort();
		int paletteNamesOffset = data.readUnsignedShort();
		NNSPatriciaTreeReader.Entry[] entries = NNSPatriciaTreeReader.readTree(data);
		List<String> textureNames = new ArrayList(texNameCount);
		List<String> paletteNames = new ArrayList(paletteNameCount);
		data.seek(texNamesOffset);
		for (int i = 0; i < texNameCount; i++) {
			textureNames.add(data.readPaddedString(16));
		}
		data.seek(paletteNamesOffset);
		for (int i = 0; i < paletteNameCount; i++) {
			paletteNames.add(data.readPaddedString(16));
		}
		tracks = new ArrayList(entries.length);
		for (NNSPatriciaTreeReader.Entry entry : entries) {
			int keyframeCount = entry.getParam(0) & 0xFFFF;
			int offset = (entry.getParam(1) >> 16);
			data.seek(offset);
			tracks.add(new NSBTPTrack(data, keyframeCount, entry.getName(), textureNames, paletteNames));
		}
		data.resetBase();
	}

	public MaterialAnimation toGeneric() {
		MaterialAnimation mta = new MaterialAnimation();
		mta.name = name;
		mta.frameCount = frameCount;

		for (NSBTPTrack pta : tracks) {
			mta.bones.add(pta.toGeneric());
		}
		return mta;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
