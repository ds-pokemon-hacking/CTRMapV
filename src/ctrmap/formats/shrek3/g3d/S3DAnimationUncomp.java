package ctrmap.formats.shrek3.g3d;

import java.io.IOException;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.BinarySerializer;
import xstandard.io.serialization.ICustomSerialization;
import xstandard.io.serialization.annotations.Ignore;
import xstandard.util.collections.IntList;

public class S3DAnimationUncomp extends S3DAnimation implements ICustomSerialization {

	public int rotationCount;
	public int translationCount;
	public int scaleCount;
	public int frameSize;
	public int animationEndFramesOffset;
	public int headerSize;
	
	@Ignore
	public int[] boneIndices;
	@Ignore
	public int[] animationStartFrames;
	@Ignore
	public byte[] frameData;

	@Override
	public void deserialize(BinaryDeserializer deserializer) throws IOException {
		boneIndices = new int[rotationCount + translationCount + scaleCount];
		for (int i = 0; i < boneIndices.length; i++) {
			boneIndices[i] = deserializer.baseStream.read();
		}
		deserializer.baseStream.seek(animationEndFramesOffset);
		IntList l = new IntList();
		int sf;
		while ((sf = deserializer.baseStream.readInt()) != 0) {
			l.add(sf);
		}
		animationStartFrames = l.toArray();
		deserializer.baseStream.seek(headerSize);
		frameData = deserializer.baseStream.readBytes(deserializer.baseStream.getLength() - deserializer.baseStream.getPosition());
	}

	@Override
	public boolean preSerialize(BinarySerializer serializer) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void postSerialize(BinarySerializer serializer) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
