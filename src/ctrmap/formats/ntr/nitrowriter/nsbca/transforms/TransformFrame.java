
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms;

import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.ScaleTrack;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.TranslationTrack;
import org.joml.Matrix3f;

public class TransformFrame {
	public float tx;
	public float ty;
	public float tz;
	
	public float sx;
	public float sy;
	public float sz;
	
	public Matrix3f rotation = new Matrix3f();
	
	public float getValueByComponent(SkeletalTransformComponent comp){
		switch (comp){
			case TX:
				return tx;
			case TY:
				return ty;
			case TZ:
				return tz;
			case SX:
				return sx;
			case SY:
				return sy;
			case SZ:
				return sz;
		}
		return 0f;
	}
}
