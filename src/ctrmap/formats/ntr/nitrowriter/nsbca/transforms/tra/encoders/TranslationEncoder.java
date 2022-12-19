
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.encoders;

import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformFrame;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalTransformComponent;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.TranslationTrack;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.elements.TranslationElement;
import java.util.List;

public interface TranslationEncoder {
	public List<TranslationElement> getElements(List<TransformFrame> frames, SkeletalTransformComponent comp);
}
