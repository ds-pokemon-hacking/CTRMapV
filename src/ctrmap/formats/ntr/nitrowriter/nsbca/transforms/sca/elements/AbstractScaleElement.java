
package ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.elements;

import ctrmap.formats.ntr.common.FX;

public abstract class AbstractScaleElement implements ScaleElement {

	protected int value_fx;
	protected int invValue_fx;
	
	protected AbstractScaleElement(float value) {
		value_fx = FX.fx32(value);
		invValue_fx = FX.fx32(1f / value);
	}
}
