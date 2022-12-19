
package ctrmap.formats.qos.g3d;

import ctrmap.formats.qos.GeminiObject;
import xstandard.io.serialization.annotations.Inline;
import java.util.List;

public class GeminiAttachment extends GeminiObject {
	public String name;
	public int hash;
	public List<GeminiAnimatedValue> animatedValues;
	@Inline
	public GeminiMatrix4x3 transform;
	public float scaleX;
	public float scaleY;
	public float scaleZ;
}
