package ctrmap.formats.qos.g3d;

import ctrmap.formats.qos.GeminiObject;
import xstandard.io.serialization.annotations.BitField;
import xstandard.io.serialization.annotations.Size;

public class GeminiTextureInstance extends GeminiObject {

	@Size(Integer.BYTES)
	@BitField(startBit = 0, bitCount = 1)
	public GeminiGXTexPlttColor0 color0;
	@BitField(startBit = 1, bitCount = 2)
	public GeminiGXTexFlip flip;
	@BitField(startBit = 3, bitCount = 2)
	public GeminiGXTexRepeat repeat;
	public int[] texImageParamFixups;
	public GeminiTexture texture;
}
