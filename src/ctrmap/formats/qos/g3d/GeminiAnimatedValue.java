
package ctrmap.formats.qos.g3d;

import ctrmap.formats.qos.GeminiObject;
import xstandard.io.serialization.annotations.BitField;
import xstandard.io.serialization.annotations.Size;
import java.util.ArrayList;
import java.util.List;

public class GeminiAnimatedValue extends GeminiObject {
	public String name;
	public List<Integer> fixups = new ArrayList<>();
	public int idHash;
	@Size(Integer.BYTES)
	@BitField(startBit = 0, bitCount = 8)
	public GeminiChannelMeaning meaning;
}
