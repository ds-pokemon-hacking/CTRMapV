package ctrmap.formats.shrek3.g3d;

import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.annotations.Ignore;
import xstandard.io.serialization.annotations.Inline;
import xstandard.io.serialization.annotations.Size;

public class S3DAnimationCurveQuat extends S3DAnimationCurve {

	@Ignore
	public KeyFrameList x = new KeyFrameList();
	@Ignore
	public KeyFrameList y = new KeyFrameList();
	@Ignore
	public KeyFrameList z = new KeyFrameList();
	@Ignore
	public KeyFrameList w = new KeyFrameList();
	
	@Override
	public void readKeys(BinaryDeserializer deserializer) {
		switch (keySize) {
			case 1:
				x.add(new KeyFrame(0f, 0f));
				y.add(new KeyFrame(0f, 0f));
				z.add(new KeyFrame(0f, 0f));
				w.add(new KeyFrame(0f, 1f));
				break;
			case 8:
				for (int i = 0; i < keyFrameCount; i++) {
					AnimKeyQuat8 key = deserializer.deserialize(AnimKeyQuat8.class);
					x.add(new KeyFrame(key.frame, key.convValue(key.x)));
					y.add(new KeyFrame(key.frame, key.convValue(key.y)));
					z.add(new KeyFrame(key.frame, key.convValue(key.z)));
					w.add(new KeyFrame(key.frame, key.convValue(key.w)));
				}
				break;
			case 12:
				for (int i = 0; i < keyFrameCount; i++) {
					AnimKeyQuat16 key = deserializer.deserialize(AnimKeyQuat16.class);
					x.add(new KeyFrame(key.frame, key.x));
					y.add(new KeyFrame(key.frame, key.y));
					z.add(new KeyFrame(key.frame, key.z));
					w.add(new KeyFrame(key.frame, key.w));
				}
				break;
			case 20:
				for (int i = 0; i < keyFrameCount; i++) {
					AnimKeyQuat32 key = deserializer.deserialize(AnimKeyQuat32.class);
					x.add(new KeyFrame(key.frame, key.x));
					y.add(new KeyFrame(key.frame, key.y));
					z.add(new KeyFrame(key.frame, key.z));
					w.add(new KeyFrame(key.frame, key.w));
				}
				break;
		}
	}
	
	@Inline
	public static class AnimKeyQuat8 extends S3DAnimKey {
		public byte x;
		public byte y;
		public byte z;
		public byte w;
		
		public float convValue(byte val) {
			return val / 128f;
		}
	}
	
	@Inline
	public static class AnimKeyQuat16 extends S3DAnimKey {
		@Size(Short.BYTES)
		public float x;
		@Size(Short.BYTES)
		public float y;
		@Size(Short.BYTES)
		public float z;
		@Size(Short.BYTES)
		public float w;
	}
	
	@Inline
	public static class AnimKeyQuat32 extends S3DAnimKey {
		public float x;
		public float y;
		public float z;
		public float w;
	}
}
