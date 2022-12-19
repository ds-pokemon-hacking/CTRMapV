package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca;

import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalAnimationFlags;
import ctrmap.formats.ntr.nitrowriter.nsbca.JointAnimationResource;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalTransformComponent;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformFrame;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformTrack;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements.ScaFX32Elem;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements.ScaleElement;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.encoders.ScaFX16Encoder;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.encoders.ScaFX32Encoder;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.encoders.ScaleEncoder;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ScaleTrack extends TransformTrack {

	private boolean isFX16;
	private List<ScaleElement> elements = new ArrayList<>();

	public ScaleTrack(List<TransformFrame> frames, SkeletalTransformComponent comp, boolean isConstant) {
		super(comp, frames);
		this.isConstant = isConstant;
		if (isConstant) {
			if (frames.isEmpty()) {
				setIsBindPose();
			} else {
				elements.add(new ScaFX32Elem(frames.get(0).getValueByComponent(comp)));
			}
		}
		else {
			ScaleEncoder enc = decideEncoder(frames, comp);
			isFX16 = enc instanceof ScaFX16Encoder;
			elements = enc.getElements(frames, comp);
		}
	}
	
	@Override
	public int getInfoImpl(){
		return JointAnimationResource.setFlagIf(0, SkeletalAnimationFlags.TRACK_ENC_IS_FX16, isFX16);
	}
	
	public void writeElements(DataOutput out) throws IOException {
		for (ScaleElement e : elements){
			e.write(out);
		}
	}

	private static ScaleEncoder decideEncoder(List<TransformFrame> frames, SkeletalTransformComponent comp) {
		for (TransformFrame f : frames) {
			float val = f.getValueByComponent(comp);
			if (Math.abs(val) > FX.FX16_MAX){
				return new ScaFX32Encoder();
			}
		}
		return new ScaFX16Encoder();
	}
}
