package ctrmap.formats.shrek3.g3d;

import ctrmap.formats.ntr.common.FX;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.annotations.Ignore;
import xstandard.io.serialization.annotations.Inline;

public class S3DAnimationCurveVec extends S3DAnimationCurve {

	@Ignore
	public KeyFrameList x = new KeyFrameList();
	@Ignore
	public KeyFrameList y = new KeyFrameList();
	@Ignore
	public KeyFrameList z = new KeyFrameList();
	
	@Override
	public void readKeys(BinaryDeserializer deserializer) {
		switch (keySize) {
			case 1:
				x.add(new KeyFrame(0f, 0f));
				y.add(new KeyFrame(0f, 0f));
				z.add(new KeyFrame(0f, 0f));
				break;
			case 8:
				for (int i = 0; i < keyFrameCount; i++) {
					AnimKeyVecComp key = deserializer.deserialize(AnimKeyVecComp.class);
					x.add(new KeyFrame(key.frame, key.convValue(key.x)));
					y.add(new KeyFrame(key.frame, key.convValue(key.y)));
					z.add(new KeyFrame(key.frame, key.convValue(key.z)));
				}
				break;
			case 16:
				for (int i = 0; i < keyFrameCount; i++) {
					AnimKeyVec32 key = deserializer.deserialize(AnimKeyVec32.class);
					x.add(new KeyFrame(key.frame, key.x));
					y.add(new KeyFrame(key.frame, key.y));
					z.add(new KeyFrame(key.frame, key.z));
				}
				break;
		}
	}
	
	@Inline
	public static class AnimKeyVecComp extends S3DAnimKey {
		public byte x;
		public byte y;
		public byte z;
		public byte shift;
		
		public float convValue(byte value) {
			return (value << shift) / FX.FX_DEFAULT_PRECISION;
		}
	}
	
	@Inline
	public static class AnimKeyVec32 extends S3DAnimKey {
		public float x;
		public float y;
		public float z;
	}
}
