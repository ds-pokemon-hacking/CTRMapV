package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.encoders;

import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalTransformComponent;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformFrame;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements.ScaFX32Elem;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements.ScaleElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ScaFX32Encoder implements ScaleEncoder {

	@Override
	public List<ScaleElement> getElements(List<TransformFrame> frames, SkeletalTransformComponent comp) {
		List<ScaleElement> l = new ArrayList<>();
		for (TransformFrame f : frames){
			l.add(new ScaFX32Elem(f.getValueByComponent(comp)));
		}
		return l;
	}

}
