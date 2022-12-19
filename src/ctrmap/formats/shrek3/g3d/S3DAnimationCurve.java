package ctrmap.formats.shrek3.g3d;

import java.io.IOException;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.annotations.Ignore;
import xstandard.io.serialization.annotations.PointerBase;
import xstandard.io.serialization.annotations.PointerValue;
import xstandard.io.serialization.annotations.Size;

@PointerBase
public abstract class S3DAnimationCurve {
	public short field_0;
	@Size(Short.BYTES)
	public int keyFrameCount;
	@Size(Short.BYTES)
	public int boneIndex;
	public byte keySize;
	public byte _padding;
	@PointerValue
	public int keyChunksOffset;
	@PointerValue
	public int keysOffset;
	
	@Ignore
	public int[] keyChunks;
	
	public abstract void readKeys(BinaryDeserializer deserializer);
	
	public void read(int animFrameCount, BinaryDeserializer deserializer) throws IOException {
		deserializer.baseStream.seek(keyChunksOffset);
		int keyChunkCount = animFrameCount >> 7;
		keyChunks = new int[keyChunkCount];
		for (int i = 0; i < keyChunkCount; i++) {
			keyChunks[i] = deserializer.baseStream.readInt();
		}
		deserializer.baseStream.seek(keysOffset);
		readKeys(deserializer);
	}
}
