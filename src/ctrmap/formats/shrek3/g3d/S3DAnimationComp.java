package ctrmap.formats.shrek3.g3d;

import java.io.IOException;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.BinarySerializer;
import xstandard.io.serialization.ICustomSerialization;
import xstandard.io.serialization.annotations.Ignore;

public class S3DAnimationComp extends S3DAnimation implements ICustomSerialization {

	public int frameCount;
	public int rotationCount;
	public int translationCount;
	public int scaleCount;
	
	@Ignore
	public S3DAnimationCurveQuat[] rotCurves;
	@Ignore
	public S3DAnimationCurveVec[] transCurves;
	@Ignore
	public S3DAnimationCurveVec[] scaleCurves;

	@Override
	public void deserialize(BinaryDeserializer deserializer) throws IOException {
		int[] curveOffsets = new int[rotationCount + translationCount + scaleCount];
		for (int i = 0; i < curveOffsets.length; i++) {
			curveOffsets[i] = deserializer.baseStream.readInt();
		}
		transCurves = new S3DAnimationCurveVec[translationCount];
		scaleCurves = new S3DAnimationCurveVec[scaleCount];
		rotCurves = new S3DAnimationCurveQuat[rotationCount];
		for (int curveIndex = 0; curveIndex < curveOffsets.length; curveIndex++) {
			deserializer.baseStream.seek(curveOffsets[curveIndex]);
			if (curveIndex < rotationCount) {
				//rotation curve
				S3DAnimationCurveQuat c = deserializer.deserialize(S3DAnimationCurveQuat.class);
				c.read(frameCount, deserializer);
				rotCurves[curveIndex] = c;
			}
			else if (curveIndex < translationCount) {
				//translation curve
				S3DAnimationCurveVec c = deserializer.deserialize(S3DAnimationCurveVec.class);
				c.read(frameCount, deserializer);
				transCurves[curveIndex - rotationCount] = c;
			}
			else {
				//scale curve
				S3DAnimationCurveVec c = deserializer.deserialize(S3DAnimationCurveVec.class);
				c.read(frameCount, deserializer);
				scaleCurves[curveIndex - (rotationCount + translationCount)] = c;
			}
		}
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
