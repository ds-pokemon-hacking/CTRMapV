package ctrmap.formats.pokemon.gen5.sequence.rel;

import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RelocatableWriter extends DataIOStream {

	private List<Integer> pointers = new ArrayList<>();

	public RelocatableWriter(IOStream io) {
		super(io);
	}

	public void writePointer(int ptr) throws IOException {
		if (ptr != 0) {
			pointers.add(getPosition());
		}
		writeInt(ptr);
	}

	public void writeRelocationTable() throws IOException {
		writeInt(pointers.size());
		for (Integer ptr : pointers) {
			writeInt(ptr);
		}
		pointers.clear();
	}

	public static class RelocatableOffset extends TemporaryOffset {

		public RelocatableOffset(RelocatableWriter dos) throws IOException {
			super(dos);
		}

		@Override
		protected void writePointer(int ptr) throws IOException {
			((RelocatableWriter)dosref).writePointer(ptr);
		}
	}
}
