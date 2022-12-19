package ctrmap.formats.qos.level;

import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.BinarySerializer;
import xstandard.io.serialization.ICustomSerialization;
import xstandard.io.serialization.annotations.Define;
import xstandard.io.serialization.annotations.DefinedArraySize;
import xstandard.io.serialization.annotations.Ignore;
import xstandard.io.serialization.annotations.Inline;
import xstandard.io.serialization.annotations.MagicStr;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KDTTree implements ICustomSerialization {

	public static final String KDT_MAGIC = "KDT\u0001";

	@MagicStr(KDT_MAGIC)
	public String magic;
	
	@Define("nodeCount")
	private int nodeCount;
	
	@Inline
	public Vec3f minVector;
	@Inline
	public Vec3f maxVector;
	
	@Inline
	@DefinedArraySize("nodeCount")
	public List<KDTTreeNode> nodes;

	@Ignore
	public List<Integer> indices = new ArrayList<>();

	public List<Integer> getIndicesForNode(KDTTreeNode n) {
		if (n.isIndex()) {
			int startIdx = n.getIndex();
			if (startIdx >= indices.size()) {
				throw new IndexOutOfBoundsException("Node 0x" + Integer.toHexString(nodes.indexOf(n)) + " index invalid! (raw data: 0x" + Integer.toHexString(n.value) + ")");
			}
			int endIdx = startIdx;
			for (; endIdx < indices.size(); endIdx++) {
				int value = indices.get(endIdx);
				if (value == 0xFFFF) {
					break;
				}
			}
			return indices.subList(startIdx, endIdx);
		} else {
			throw new RuntimeException("Not an index node.");
		}
	}

	@Override
	public void deserialize(BinaryDeserializer deserializer) throws IOException {
		//The length is non-deterministic, but usually ends at EOF or at end of KDT section
		int maxCount = (deserializer.baseStream.getLength() - deserializer.baseStream.getPosition()) >> 1;
		int lastIndex = -1;
		for (int i = 0; i < maxCount; i++) {
			int index = deserializer.baseStream.readUnsignedShort();
			if (index == 0x154 && lastIndex == 0x444B) {
				deserializer.baseStream.skipBytes(-4); //next KDT section
				break;
			}
			indices.add(index);
			lastIndex = index;
		}
	}

	@Override
	public boolean preSerialize(BinarySerializer serializer) throws IOException {
		nodeCount = nodes.size();
		return false;
	}

	@Override
	public void postSerialize(BinarySerializer serializer) throws IOException {
		for (Integer index : indices) {
			serializer.baseStream.writeShort(index);
		}
	}

	@Inline
	public static class KDTTreeNode {

		private int value;

		public boolean isIndex() {
			return value < 0;
		}

		public int getIndex() {
			return value & 0x7FFFFFFF;
		}

		public int getFailLeafIndex() {
			return (value >> 16) & 0x7FFF;
		}

		public int getRefValue() {
			return (short) (value & 0xFFFF);
		}

		@Override
		public String toString() {
			return isIndex() ? "[" + getIndex() + "]" : getRefValue() + " ?!-> " + getFailLeafIndex();
		}
	}
}
