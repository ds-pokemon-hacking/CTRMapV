package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra;

import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalTransformComponent;
import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalAnimationFlags;
import ctrmap.formats.ntr.nitrowriter.nsbca.JointAnimationResource;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformFrame;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformTrack;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.elements.TraFX32Elem;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.elements.TranslationElement;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.encoders.TraFX16Encoder;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.encoders.TraFX32Encoder;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.encoders.TranslationEncoder;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TranslationTrack extends TransformTrack {

	private boolean isFX16;
	private List<TranslationElement> elements = new ArrayList<>();

	public TranslationTrack(List<TransformFrame> frames, SkeletalTransformComponent comp, boolean isConstant) {
		super(comp, frames);
		this.comp = comp;
		this.isConstant = isConstant;
		if (isConstant) {
			if (frames.isEmpty()) {
				setIsBindPose();
			} else {
				elements.add(new TraFX32Elem(frames.get(0).getValueByComponent(comp)));
			}
		}
		else {
			TranslationEncoder enc = decideEncoder(frames, comp);
			isFX16 = enc instanceof TraFX16Encoder;
			elements = enc.getElements(frames, comp);
		}
	}
	
	@Override
	public int getInfoImpl(){
		return JointAnimationResource.setFlagIf(0, SkeletalAnimationFlags.TRACK_ENC_IS_FX16, isFX16);
	}
	
	public void writeElements(DataOutput out) throws IOException {
		for (TranslationElement e : elements){
			e.write(out);
		}
	}

	private static TranslationEncoder decideEncoder(List<TransformFrame> frames, SkeletalTransformComponent comp) {
		for (TransformFrame f : frames) {
			float val = f.getValueByComponent(comp);
			if (Math.abs(val) > FX.FX16_MAX){
				return new TraFX32Encoder();
			}
		}
		return new TraFX16Encoder();
	}

}
