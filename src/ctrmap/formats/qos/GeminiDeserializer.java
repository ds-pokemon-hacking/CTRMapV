package ctrmap.formats.qos;

import ctrmap.formats.ntr.common.NDSDeserializer;
import ctrmap.formats.qos.g3d.GeminiScene;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.ReferenceType;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeminiDeserializer extends NDSDeserializer {

	public GeminiDeserializer(IOStream baseStream) {
		super(baseStream, ReferenceType.ABSOLUTE_POINTER);
	}

	@Override
	protected int readArrayLength(Field field) throws IOException {
		int val = super.readArrayLength(field);
		return val & 0x7FFFFFFF;
	}

	public static void main(String[] args) {
		GeminiScene file = GeminiDeserializer.deserializeFileStatic(GeminiScene.class, 
			new DiskFile("D:\\Emugames\\DS\\2934 - 007 - Quantum of Solace (USA) (En,Fr)_Extracted\\data_uncomp\\gun_gold.gcb")
		);
		YamlReflectUtil.serializeObjectAsYml(file).writeToFile(
			new DiskFile("D:\\Emugames\\DS\\2934 - 007 - Quantum of Solace (USA) (En,Fr)_Extracted\\research\\gun_gold.gcb.yml")
		);
	}

	public static <T extends GeminiObject> T deserializeFileStatic(Class<T> cls, FSFile file) {
		try {
			IOStream baseStream = file.getIO();
			BinaryDeserializer deserializer = new GeminiDeserializer(baseStream);
			T retval = deserializer.deserialize(cls);
			baseStream.close();
			return retval;
		} catch (IOException ex) {
			Logger.getLogger(GeminiDeserializer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
