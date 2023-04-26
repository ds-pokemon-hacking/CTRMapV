package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.encoders;

import ctrmap.formats.ntr.nitrowriter.common.math.RotAxisCompact;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.elements.RotAxisCompactElem;
import org.joml.Matrix3f;

public class RotAxisCompactEncoder extends RotationEncoder {

	private Matrix3f currentMtx = null;
	
	private RotAxisCompact currentRotation;

	@Override
	public boolean canEncodeMatrix(Matrix3f mat) {
		currentRotation = RotAxisCompact.tryMake(mat);
		currentMtx = mat;
		return currentRotation != null;
	}

	@Override
	public RotAxisCompactElem encodeMatrix(Matrix3f mat) {
		if (currentMtx != mat){
			currentRotation = RotAxisCompact.tryMake(mat);
		}
		
		RotAxisCompactElem e = new RotAxisCompactElem(currentRotation);
		encodedElements.add(e);
		return e;
	}

}
