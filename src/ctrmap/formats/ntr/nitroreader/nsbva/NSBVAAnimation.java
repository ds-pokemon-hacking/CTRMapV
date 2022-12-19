package ctrmap.formats.ntr.nitroreader.nsbva;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.util.ArrayList;
import java.util.List;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityBoneTransform;
import ctrmap.renderer.scene.model.Skeleton;
import xstandard.INamed;
import java.io.IOException;

public class NSBVAAnimation implements INamed {

	public String name;
	public int frameCount;

	public List<NSBVATrack> tracks;

	public NSBVAAnimation(NTRDataIOStream data, String name) throws IOException {
		data.setBaseHere();
		this.name = name;
		String magic = data.readPaddedString(4); //V\0AV
		frameCount = data.readUnsignedShort();
		int visGroupCount = data.readUnsignedShort();
		int size = data.readUnsignedShort();
		data.skipBytes(2);
		tracks = new ArrayList(visGroupCount);
		for (int i = 0; i < visGroupCount; i++) {
			tracks.add(new NSBVATrack(frameCount));
		}
		long visData = 0;
		int count = 0;
		for (int i = 0; i < frameCount; i++) {
			for (int j = 0; j < visGroupCount; j++) {
				if (count % 31 == 0) {
					visData = data.readInt();
				}
				tracks.get(j).addFrame((visData & 1) != 0);
				visData >>= 1;
				count++;
			}
		}
		data.resetBase();
	}
	
	public VisibilityAnimation toGeneric(Skeleton skl){
		VisibilityAnimation anm = new VisibilityAnimation();
		anm.name = name;
		anm.frameCount = frameCount;

		int count = Math.min(tracks.size(), skl.getJointCount());
		
		for (int i = 0; i < count; i++){
			VisibilityBoneTransform bt = new VisibilityBoneTransform();
			bt.target = VisibilityBoneTransform.Target.JOINT;
			bt.name = skl.getJoint(i).name;
			
			NSBVATrack t = tracks.get(i);

			for (int f = 0; f < t.animation.size(); f++){
				bt.isVisible.add(new KeyFrame(f, t.animation.get(f) ? 1f : 0f));
			}
			
			anm.tracks.add(bt);
		}
		
		return anm;
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
