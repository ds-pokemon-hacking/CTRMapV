package ctrmap.formats.ntr.nitrowriter.common.resources;

import java.util.List;

/**
 * SPICA.Formats.Common.PatriciaTree github.com/gdkchan/SPICA
 */
public class PatriciaTree {

	private static final String DUPLICATE_KEYS_EX = "Tree shouldn't contain duplicate keys!";
	
	public static <T extends PatriciaTreeNode> void insert(List<T> nodes, T value, int maxNameLength) {
		int bit = (maxNameLength * 8) - 1;

		String refName = nodes.get(traverse(value.name, nodes).index).name;

		while (getStrBit(refName, bit) == getStrBit(value.name, bit)) {
			if (--bit == -1) {
				throw new IllegalArgumentException(DUPLICATE_KEYS_EX + ": " + value.name);
			}
		}

		value.refBit = bit;
		TraverseResult traverseResult = traverse(value.name, nodes, bit);

		if (getStrBit(value.name, bit)) {
			value.lidx = traverseResult.index;
			value.ridx = nodes.size();
		} else {
			value.lidx = nodes.size();
			value.ridx = traverseResult.index;
		}

		if (getStrBit(value.name, traverseResult.parent.refBit)) {
			traverseResult.parent.ridx = nodes.size();
		} else {
			traverseResult.parent.lidx = nodes.size();
		}
		
		value.dataEntryNo = Math.max(nodes.size() - 1, 0); //root node should have index -1, but has to be 0 on NNS
		
		nodes.add(value);
	}

	private static TraverseResult traverse(String name, List<? extends PatriciaTreeNode> Nodes) {
		return traverse(name, Nodes, 0);
	}

	private static TraverseResult traverse(String name, List<? extends PatriciaTreeNode> nodes, int bit) {
		PatriciaTreeNode parent = nodes.get(0);

		PatriciaTreeNode left = nodes.get(parent.lidx);

		int output = parent.lidx;

		while (parent.refBit > left.refBit && left.refBit > bit) {
			if (getStrBit(name, left.refBit)) {
				output = left.ridx;
			} else {
				output = left.lidx;
			}

			parent = left;
			left = nodes.get(output);
		}

		TraverseResult r = new TraverseResult();
		r.index = output;
		r.parent = parent;

		return r;
	}

	private static boolean getStrBit(String name, int bit) {
		int pos = bit >> 3;
		int charBit = bit & 7;

		if (name != null && pos < name.length()) {
			return ((name.charAt(pos) >> charBit) & 1) != 0;
		} else {
			return false;
		}
	}

	private static class TraverseResult {

		public PatriciaTreeNode parent;
		public int index;
	}
}
