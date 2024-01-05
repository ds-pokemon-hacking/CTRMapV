package ctrmap.formats.ntr.nitroreader.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.impl.ext.data.DataIOStream;

public class NNSPatriciaTreeReader {

	public static Entry[] readTree(DataInputEx in) throws IOException {
		int treeStart = in.getPosition();
		in.read();
		int count = in.read();
		int dataSize = in.readUnsignedShort();
		//On FFIII Steam port models, this field is 0. thus we have to read the structure
		//accurately as there is no tree present (was this allowed on the original format!?)
		int nodesOffset = in.readUnsignedShort();
		int entriesOffset = in.readUnsignedShort();
		in.seekNext(treeStart + entriesOffset);
		int entriesStart = in.getPosition();
		int entrySize = in.readUnsignedShort();
		int entryWordCount = entrySize >> 2;
		int namesOffset = in.readUnsignedShort();
		Entry[] entries = new Entry[count];
		for (int j = 0; j < count; j++) {
			entries[j] = new Entry(in, entryWordCount);
		}
		in.seekNext(entriesStart + namesOffset);
		for (int j = 0; j < count; j++) {
			entries[j].name = in.readPaddedString(16);
		}
		return entries;
	}

	public static <T> List<T> processOffsetTree(DataIOStream io, OffsetProcFunc<T> procFunc) throws IOException {
		Entry[] entries = readTree(io);
		List<T> list = new ArrayList<>(entries.length);
		for (Entry e : entries) {
			io.seek(e.getParam(0));
			list.add(procFunc.read(io, e));
		}
		return list;
	}
	
	public static interface OffsetProcFunc<T> {
		public T read(DataIOStream io, Entry entry) throws IOException;
	}

	public static class Entry {

		private String name;
		private final int[] params;

		private Entry(DataInputEx in, int wordCount) throws IOException {
			params = new int[wordCount];
			for (int i = 0; i < params.length; i++) {
				params[i] = in.readInt();
			}
		}

		public String getName() {
			return name;
		}

		public int getParam(int index) {
			return params[index];
		}
	}
}
