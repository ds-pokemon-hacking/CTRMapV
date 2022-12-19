package ctrmap.formats.ntr.nitroreader.nsbma;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.common.gfx.Nitroshader;
import ctrmap.formats.ntr.nitroreader.common.NNSAnimationTrackFlags;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import ctrmap.formats.ntr.nitroreader.common.Utils;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import ctrmap.renderer.scene.animation.material.RGBAKeyFrameGroup;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import xstandard.math.vec.RGBA;
import xstandard.util.collections.IntList;
import java.io.IOException;
import xstandard.math.BitMath;

public class NSBMATrack {

	public final String materialName;

	private NSBMAChannel diffuse;
	private NSBMAChannel ambient;
	private NSBMAChannel specular;
	private NSBMAChannel emission;
	private NSBMAChannel alpha;

	public NSBMATrack(NTRDataIOStream data, int frameCount, NNSPatriciaTreeReader.Entry header) throws IOException {
		this.materialName = header.getName();
		int difHeader = header.getParam(0);
		int ambHeader = header.getParam(1);
		int spcHeader = header.getParam(2);
		int emiHeader = header.getParam(3);
		int vtxAlphaHeader = header.getParam(4);
		diffuse = readColorChannel(data, frameCount, difHeader);
		ambient = readColorChannel(data, frameCount, ambHeader);
		specular = readColorChannel(data, frameCount, spcHeader);
		emission = readColorChannel(data, frameCount, emiHeader);
		alpha = readAlphaChannel(data, frameCount, vtxAlphaHeader);
	}

	private NSBMAChannel readColorChannel(NTRDataIOStream data, int frameCount, int header) throws IOException {
		NSBMAChannel c = new NSBMAChannel();
		IntList values;
		if (NNSAnimationTrackFlags.isConstant(header)) {
			values = new IntList(1);
			values.add(Utils.GXColorToRGB((short) (header & 0xFFFF)));
		} else {
			int step = NNSAnimationTrackFlags.getFrameStep(header);
			c.step = step;
			int offset = header & 0xFFFF;
			int valueCount = Utils.getValueCount(frameCount, step);
			values = new IntList(valueCount);
			data.seek(offset);
			for (int i = 0; i < valueCount; i++) {
				values.add(Utils.GXColorToRGB(data.readShort()));
			}
		}
		c.values = values;
		return c;
	}

	private NSBMAChannel readAlphaChannel(NTRDataIOStream data, int numFrame, int header) throws IOException {
		NSBMAChannel c = new NSBMAChannel();
		IntList values;
		if (NNSAnimationTrackFlags.isConstant(header)) {
			values = new IntList(1);
			values.add(header & 0xFF);
		} else {
			int step = NNSAnimationTrackFlags.getFrameStep(header);
			c.step = step;
			int offset = header & 0xFFFF;
			int valueCount = Utils.getValueCount(numFrame, step);
			values = new IntList(valueCount);
			data.seek(offset);
			for (int i = 0; i < valueCount; i++) {
				values.add(data.read());
			}
		}
		c.values = values;
		return c;
	}

	public MatAnimBoneTransform toGeneric() {
		MatAnimBoneTransform bt = new MatAnimBoneTransform();
		bt.name = materialName;

		convChannelRgb(bt, MaterialColorType.AMBIENT, ambient);
		convChannelRgb(bt, MaterialColorType.DIFFUSE, diffuse);
		convChannelRgb(bt, MaterialColorType.EMISSION, emission);
		convChannelRgb(bt, MaterialColorType.SPECULAR0, specular);
		
		convChannelAlpha(bt, Nitroshader.NSH_RESERVE_MATERIAL_ALPHA_CCOL, alpha);

		return bt;
	}
	
	private static final float _1_255 = 1 / 255f;

	private void convChannelRgb(MatAnimBoneTransform dest, MaterialColorType type, NSBMAChannel source) {
		RGBAKeyFrameGroup kfg = dest.materialColors[type.ordinal()];

		RGBA rgbaTemp = new RGBA();

		for (int index = 0, frame = 0; index < source.values.size(); index++, frame += source.step) {
			rgbaTemp.setARGB(Utils.GXColorToRGB((short) source.values.get(index)));

			kfg.addColor(frame, rgbaTemp);
		}
	}
	
	private void convChannelAlpha(MatAnimBoneTransform dest, MaterialColorType type, NSBMAChannel source) {
		RGBAKeyFrameGroup kfg = dest.materialColors[type.ordinal()];

		for (int index = 0, frame = 0; index < source.values.size(); index++, frame += source.step) {
			kfg.a.add(new KeyFrame(frame, GXColor.bit5to8(source.values.get(index)) * _1_255));
		}
	}

	private static class NSBMAChannel {

		public int step;
		public IntList values;
	}
}
