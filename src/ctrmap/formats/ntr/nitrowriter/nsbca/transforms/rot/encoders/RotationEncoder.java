
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.encoders;

import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.elements.RotationElement;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix3f;

public abstract class RotationEncoder {
	public final List<RotationElement> encodedElements = new ArrayList<>();
	
	public abstract boolean canEncodeMatrix(Matrix3f mat);
	public abstract RotationElement encodeMatrix(Matrix3f mat);
}
