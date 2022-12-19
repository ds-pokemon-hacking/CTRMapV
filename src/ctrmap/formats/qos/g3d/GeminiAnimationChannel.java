
package ctrmap.formats.qos.g3d;

import ctrmap.formats.qos.GeminiObject;
import xstandard.io.serialization.annotations.BitField;
import xstandard.io.serialization.annotations.Size;

public class GeminiAnimationChannel extends GeminiObject {
	public int fixupSize;
	public int idHash;
	public int offset;
	@Size(Integer.BYTES)
	@BitField(startBit = 0, bitCount = 4)
	public GeminiChannelType datatype;
	@BitField(startBit = 4, bitCount = 8)
	public GeminiChannelMeaning meaning;
}
