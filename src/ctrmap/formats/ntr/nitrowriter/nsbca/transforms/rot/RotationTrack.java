package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot;

import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalTransformComponent;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformFrame;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformTrack;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.elements.RotationElement;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.encoders.RotAxisCompactEncoder;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.encoders.RotationEncoder;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.encoders.Rot3x3CompactEncoder;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix3f;

public class RotationTrack extends TransformTrack {

	private RotationEncoder ENCODER_3X3COMP = new Rot3x3CompactEncoder();
	private RotationEncoder ENCODER_AXISCOMP = new RotAxisCompactEncoder();
	
	private List<RotationElement> elements = new ArrayList<>();
	
	public RotationTrack(List<TransformFrame> frames, boolean isConstant) {
		super(SkeletalTransformComponent.R, frames);
		this.isConstant = isConstant;
		fillEncoders(frames);
	}
	
	@Override
	public int getInfoImpl(){
		return 0;
	}
	
	private void fillEncoders(List<TransformFrame> frames){
		if (isConstant){
			if (!frames.isEmpty()){
				Matrix3f constantMtx = frames.get(0).rotation;
				elements.add(decideEncoder(constantMtx).encodeMatrix(constantMtx));
			}
			else {
				setIsBindPose();
			}
		}
		else {
			for (TransformFrame f : frames){
				elements.add(decideEncoder(f.rotation).encodeMatrix(f.rotation));
			}
		}
	}
	
	public List<RotationElement> getAxisRots(){
		return ENCODER_AXISCOMP.encodedElements;
	}
	
	public List<RotationElement> getBasisRots(){
		return ENCODER_3X3COMP.encodedElements;
	}
	
	public List<RotationElement> getElements(){
		return elements;
	}

	public RotationEncoder decideEncoder(Matrix3f mat) {
		if (ENCODER_AXISCOMP.canEncodeMatrix(mat)){
			return ENCODER_AXISCOMP;
		}
		if (ENCODER_3X3COMP.canEncodeMatrix(mat)){
			return ENCODER_3X3COMP;
		}
		throw new IllegalArgumentException("Non-encodeable matrix.");
	}
}
