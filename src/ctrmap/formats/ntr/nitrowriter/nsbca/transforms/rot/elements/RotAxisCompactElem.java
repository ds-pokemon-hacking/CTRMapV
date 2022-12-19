
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.elements;

import ctrmap.formats.ntr.nitrowriter.common.math.RotAxisCompact;
import xstandard.math.BitMath;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class RotAxisCompactElem implements RotationElement {
	
	private int flags;
	
	private short a;
	private short b;
	
	public RotAxisCompactElem(RotAxisCompact rotation){
		flags = rotation.getIdxOne();
		flags = BitMath.setIntegerBit(flags, 4, rotation.invOne);
		flags = BitMath.setIntegerBit(flags, 5, rotation.invC);
		flags = BitMath.setIntegerBit(flags, 6, rotation.invD);
		a = rotation.a;
		b = rotation.b;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeShort(flags);
		out.writeShort(a);
		out.writeShort(b);
	}

	@Override
	public boolean equals(Object o){
		if (o != null && o instanceof RotAxisCompactElem){
			RotAxisCompactElem e = (RotAxisCompactElem)o;
			return e.flags == flags && (Math.abs(e.a - a) < 4) && (Math.abs(e.b - b) < 4);
		}
		return false;
	}
}
