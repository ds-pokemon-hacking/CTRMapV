
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.encoders;

import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalTransformComponent;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformFrame;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements.ScaleElement;
import java.util.List;

public interface ScaleEncoder {
	public List<ScaleElement> getElements(List<TransformFrame> frames, SkeletalTransformComponent comp);
}
