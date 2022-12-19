package ctrmap.formats.ntr.nitrowriter.common.resources;

import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.PointerTable;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.util.StringIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class NNSG3DResourceTree implements Iterable<NNSG3DResource> {

	private List<NNSG3DResource> resources = new ArrayList<>();

	public void write(DataIOStream out) throws IOException {
		write(out, 0);
	}

	public void write(DataIOStream out, int blkPtrBase) throws IOException {
		NNSG3DResourceTree.renameDuplicates(resources);
		NNSPatriciaTreeWriter.writeNNSPATRICIATree(out, resources, 4);

		//entries
		List<TemporaryOffset> blockPtrs = PointerTable.allocatePointerTable(resources.size(), out, blkPtrBase, false);
		for (NNSG3DResource blk : resources) {
			StringIO.writePaddedString(out, blk.name, 16);
		}

		for (int i = 0; i < resources.size(); i++) {
			blockPtrs.get(i).setHere();
			out.write(resources.get(i).getBytes());
		}
	}

	public void addResource(NNSG3DResource rsc) {
		if (resources.size() < 256) {
			resources.add(rsc);
		}
	}

	public static void renameDuplicates(List<? extends PatriciaTreeNode> nodes) {
		renameDuplicates(nodes, null);
	}

	public static void renameDuplicates(List<? extends PatriciaTreeNode> nodes, Map<String, String> renameMap) {
		List<String> usedNames = new ArrayList<>();

		for (PatriciaTreeNode node : nodes) {
			String name = node.name.substring(0, Math.min(16, node.name.length()));

			String newName = name;

			int idx = 1;
			while (usedNames.contains(newName)) {
				newName = String.format("%02d_%s", idx, name);
				newName = newName.substring(0, Math.min(16, newName.length()));
				idx++;
			}

			if (renameMap != null) {
				renameMap.put(node.name, newName);
			}

			node.name = newName;
			usedNames.add(newName);
		}
	}

	@Override
	public Iterator<NNSG3DResource> iterator() {
		return resources.iterator();
	}
}
