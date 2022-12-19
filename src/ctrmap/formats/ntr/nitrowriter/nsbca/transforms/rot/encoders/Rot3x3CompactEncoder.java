
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.encoders;

import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.elements.Rot3x3CompactElem;
import xstandard.math.vec.Vec3f;
import org.joml.Matrix3f;

public class Rot3x3CompactEncoder extends RotationEncoder {

	private final Vec3f COL1_TEMP = new Vec3f();
	private final Vec3f COL2_TEMP = new Vec3f();
	private final Vec3f COL3_TEMP = new Vec3f();

	@Override
	public boolean canEncodeMatrix(Matrix3f mat) {
		//I think this should always evaluate to true, but just to be sure
		mat.getColumn(0, COL1_TEMP);
		mat.getColumn(1, COL2_TEMP);
		mat.getColumn(2, COL3_TEMP);
		COL1_TEMP.cross(COL2_TEMP);
		return COL1_TEMP.equalsImprecise(COL3_TEMP, 0.001f);
	}

	@Override
	public Rot3x3CompactElem encodeMatrix(Matrix3f mat) {
		Rot3x3CompactElem e = new Rot3x3CompactElem(mat);
		encodedElements.add(e);
		return e;
	}

}
