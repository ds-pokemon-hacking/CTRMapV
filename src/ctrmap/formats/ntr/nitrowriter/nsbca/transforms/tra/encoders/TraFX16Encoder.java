package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.encoders;

import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformFrame;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalTransformComponent;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.TranslationTrack;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.elements.TraFX16Elem;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.elements.TranslationElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TraFX16Encoder implements TranslationEncoder {

	@Override
	public List<TranslationElement> getElements(List<TransformFrame> frames, SkeletalTransformComponent comp) {
		List<TranslationElement> l = new ArrayList<>();
		for (TransformFrame f : frames){
			l.add(new TraFX16Elem(f.getValueByComponent(comp)));
		}
		return l;
	}

}
