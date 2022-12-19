
package ctrmap.formats.pokemon.gen5.sequence.rel;

import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

/**
 *
 */
public class StreamRelocator {
	public static void relocate(DataIOStream io, int dataPtr) throws IOException {
		int relocationCount = io.readInt();
		
		int[] ptrs = new int[relocationCount];
		for (int i = 0; i < relocationCount; i++){
			ptrs[i] = io.readInt();
		}
		
		for (int ptr : ptrs){
			io.seek(ptr);
			int value = io.readInt();
			io.seek(ptr);
			io.writeInt(value + dataPtr);
		}
	}
}
