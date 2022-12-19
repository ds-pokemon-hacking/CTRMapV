
package ctrmap.formats.qos.g3d;

import ctrmap.formats.qos.GeminiObject;
import xstandard.io.serialization.annotations.BitField;
import xstandard.io.serialization.annotations.Size;

public class GeminiTexture extends GeminiObject {
	@Size(Integer.BYTES)
	@BitField(startBit = 0, bitCount = 3)
	public GeminiGXTexFmt format;
	@BitField(startBit = 3, bitCount = 3)
	public GeminiGXTexSize sizeS;
	@BitField(startBit = 6, bitCount = 3)
	public GeminiGXTexSize sizeT;
	@BitField(startBit = 9, bitCount = 1)
	public boolean _isBeingStreamed;
	public GeminiFileReference imageData;
	public short[] paletteData;
	public int _refCount;
	public int _slotAddress;
	public int _paletteAddress;
}
