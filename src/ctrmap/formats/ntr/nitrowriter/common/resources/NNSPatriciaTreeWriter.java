package ctrmap.formats.ntr.nitrowriter.common.resources;

import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffsetShort;
import xstandard.io.util.StringIO;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NNSPatriciaTreeWriter {

	private static int calcSize(int entryCount, int entrySize) {
		return 8 + (entryCount + 1) * 4 + 4 + entryCount * (entrySize + 16);
		//header + nodes + entry header + entries
	}

	public static void writeNNSPATRICIATreeNames(DataOutput out, List<? extends PatriciaTreeNode> nodes) throws IOException {
		for (PatriciaTreeNode n : nodes) {
			StringIO.writePaddedString(out, n.name, 16);
		}
	}

	public static void writeNNSPATRICIATree(DataIOStream out, List<? extends PatriciaTreeNode> nodes, int entrySize) throws IOException {
		int base = -out.getPosition();
		writePATRICIATreeHeader(out, nodes.size(), entrySize);
		TemporaryOffsetShort treeOffset = new TemporaryOffsetShort(out, base);
		TemporaryOffsetShort entriesOffset = new TemporaryOffsetShort(out, base);
		//actual patricia tree
		treeOffset.setHere();
		List<PatriciaTreeNode> patriciaTree = createPATRICIATreeList(nodes);
		for (PatriciaTreeNode e : patriciaTree) {
			e.writeNode(out);
		}
		entriesOffset.setHere();
		out.writeShort(entrySize); //entry size
		out.writeShort(nodes.size() * entrySize + 4); //strings offset - entry block length
	}

	public static List<PatriciaTreeNode> createPATRICIATreeList(List<? extends PatriciaTreeNode> nodes) {
		List<PatriciaTreeNode> l = new ArrayList<>();
		boolean hasRootNode = false;
		PatriciaTreeNode root = null;
		for (PatriciaTreeNode n : nodes) {
			if (n.refBit == 127 && n.name == null) {
				hasRootNode = true;
				root = n;
				l.add(n);
				break;
			}
		}
		if (!hasRootNode) {
			l.add(createRootNode());
		}
		for (PatriciaTreeNode n : nodes) {
			if (n != root) {
				PatriciaTree.insert(l, n, 16);
			}
		}
		return l;
	}

	public static PatriciaTreeNode createRootNode() {
		PatriciaTreeNode e = new PatriciaTreeNode();
		e.refBit = 127; //at least how Pokemon BW2 has them
		e.ridx = 1;
		e.lidx = 0;
		e.dataEntryNo = 0;
		return e;
	}

	public static void writePATRICIATreeHeader(DataOutput out, int entryCount, int entrySize) throws IOException {
		out.write(0); //dummy?
		out.write(entryCount);
		out.writeShort(calcSize(entryCount, 4));
	}

}
