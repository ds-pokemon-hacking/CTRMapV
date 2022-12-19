package ctrmap.formats.qos;

import java.util.List;

public class GeminiFile extends GeminiObject {

	public List<AssetIdentifier> assetIDList;
	public List<ArrayIdentifier> arrayList;
	public List<GeminiObject> objects;

	public static class AssetIdentifier extends GeminiObject {

		public int id;
		public GeminiObject object;
	}

	public static class ArrayIdentifier extends GeminiObject {

		public int id;
		public int elementSize;
		public int size;
		public int capacity;
	}
}
