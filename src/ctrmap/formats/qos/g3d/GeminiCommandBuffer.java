
package ctrmap.formats.qos.g3d;

import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GECommandDecoder;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.BinarySerializer;
import xstandard.io.serialization.ICustomSerialization;
import xstandard.io.serialization.annotations.Ignore;
import xstandard.io.serialization.annotations.Inline;
import java.io.IOException;
import java.util.List;

public class GeminiCommandBuffer implements ICustomSerialization {
	@Inline
	private transient byte[] rawBuffer;
	@Ignore
	public List<GECommand> displayList;

	@Override
	public void deserialize(BinaryDeserializer deserializer) throws IOException {
		try (DataInStream bufStream = new DataInStream(rawBuffer)) {
			displayList = GECommandDecoder.decodePacked(bufStream, rawBuffer.length);
		}
	}

	@Override
	public boolean preSerialize(BinarySerializer serializer) throws IOException {
		return true;
	}

	@Override
	public void postSerialize(BinarySerializer serializer) throws IOException {
	}

}
