
package ctrmap.formats.ntr.nitrowriter.nsbmd;

import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;
import ctrmap.formats.ntr.nitrowriter.common.math.RotAxisCompact;
import ctrmap.renderer.scene.model.Joint;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.math.BitMath;
import java.io.IOException;
import org.joml.Matrix3f;


public class NitroJointResource extends NNSG3DResource {
		
	private boolean isTraZero;
	private boolean isRotZero;
	private boolean isScaOne;
	
	private boolean hasCompactRotation;
	
	private FX.VecFX32 translation;
	private FX.VecFX32 scale;
	private FX.VecFX32 invScale;
	
	private FX.Mat3x3FX16 rotation3x3;
	private RotAxisCompact rotationCompact;

	public NitroJointResource(Joint j){
		name = j.name;
		translation = new FX.VecFX32(j.position);
		scale = new FX.VecFX32(j.scale);
		invScale = new FX.VecFX32(j.scale.getInverse());
		
		Matrix3f rotMtx = new Matrix3f().rotateZYX(j.rotation);
		rotation3x3 = new FX.Mat3x3FX16(rotMtx);
		rotationCompact = RotAxisCompact.tryMake(rotMtx);
		
		isRotZero = j.rotation.length() == 0;
		isTraZero = translation.equals(FX.ZERO_VEC3);
		isScaOne = scale.equals(FX.ONE_VEC3);
		hasCompactRotation = rotationCompact != null && !isRotZero;
	}
	
	@Override
	public byte[] getBytes() throws IOException {
		DataIOStream out = new DataIOStream();
		int flags = 0;
		flags = BitMath.setIntegerBit(flags, 0, isTraZero);
		flags = BitMath.setIntegerBit(flags, 1, isRotZero);
		flags = BitMath.setIntegerBit(flags, 2, isScaOne);
		flags = BitMath.setIntegerBit(flags, 3, hasCompactRotation);
		
		if (hasCompactRotation){
			flags = BitMath.setIntegerBits(flags, 4, 4, rotationCompact.getIdxOne());
			flags = BitMath.setIntegerBit(flags, 8, rotationCompact.invOne);
			flags = BitMath.setIntegerBit(flags, 9, rotationCompact.invC);
			flags = BitMath.setIntegerBit(flags, 10, rotationCompact.invD);
		}
		
		out.writeShort(flags);
		
		if (!hasCompactRotation && !isRotZero){
			out.writeShort(rotation3x3.get(0));
		}
		else {
			out.writeShort(0);
		}
		
		if (!isTraZero){
			translation.write(out);
		}
		
		if (!isRotZero) {
			if (!hasCompactRotation){
				for (int i = 1; i < 9; i++){
					out.writeShort(rotation3x3.get(i));
				}
			}
			else {
				out.writeShort(rotationCompact.a);
				out.writeShort(rotationCompact.b);
			}
		}
		
		if (!isScaOne){
			scale.write(out);
			invScale.write(out);
		}
		
		return out.toByteArray();
	}
}
