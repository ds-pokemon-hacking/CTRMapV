package ctrmap.formats.ntr.nitrowriter.common.resources;

import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffsetShort;
import xstandard.io.util.StringIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class NNSG3DAssociatedNameTree {

	private List<AssociatedName> entries = new ArrayList<>();
	
	public void write(DataIOStream out, int blkPtrBase) throws IOException {
		NNSPatriciaTreeWriter.writeNNSPATRICIATree(out, entries, 4);

		List<TemporaryOffsetShort> datIdPtrs = new ArrayList<>();
		
		for (AssociatedName name : entries){
			datIdPtrs.add(new TemporaryOffsetShort(out, blkPtrBase));
			out.write(name.dataIDs.size());
			out.write(0);
		}
		
		for (AssociatedName name : entries) {
			StringIO.writePaddedString(out, name.name, 16);
		}

		for (int i = 0; i < entries.size(); i++) {
			datIdPtrs.get(i).setHere();
			for (Integer dataID : entries.get(i).dataIDs){
				out.write(dataID);
			}
			out.pad(4);
		}
	}

	public void addNameBinding(String name, int id){
		if (name == null){
			return;
		}
		for (AssociatedName n : entries){
			if (Objects.equals(n.name, name)){
				n.dataIDs.add(id);
				return;
			}
		}
		
		AssociatedName n = new AssociatedName();
		n.name = name;
		n.dataIDs.add(id);
		entries.add(n);
	}
	
	public static class AssociatedName extends PatriciaTreeNode {
		public List<Integer> dataIDs = new ArrayList<>();
	}
}
