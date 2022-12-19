
package ctrmap.formats.pokemon.gen5.mapreplace;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import xstandard.util.ReflectionHashIgnore;

public class VMapReplaceTable {
	
	public static final int CONDITION_SEASON = 0;
	public static final int CONDITION_GAME_VERSION = 1;
	public static final int CONDITION_DRIFTVEIL_MARKET = 2;
	public static final int CONDITION_WORK_LO = 3;
	public static final int CONDITION_WORK_HI = 0xFE;
	public static final int CONDITION_WORK_INVALID = 0xFF;
	
	@ReflectionHashIgnore
	private final FSFile source;
	
	public final List<Entry> entries = new ArrayList<>();
	
	public VMapReplaceTable(FSFile fsf) {
		this.source = fsf;
		try {
			DataInStream in = fsf.getDataInputStream();
			
			int count = in.getLength() / Entry.BYTES;
			for (int i = 0; i < count; i++) {
				entries.add(new Entry(in));
			}
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(VMapReplaceTable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void write() {
		if (source != null) {
			try {
				DataOutStream out = source.getDataOutputStream();
				for (Entry e : entries) {
					e.write(out);
				}
				out.close();
			} catch (IOException ex) {
				Logger.getLogger(VMapReplaceTable.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	public static class Entry {
		
		public static final int BYTES = 0x10;
		
		public int matrixID;
		public boolean typeIsMatrix;
		public int replacementCondition;
		public final int[] replacementValues = new int[5];
		
		public Entry(DataInput in) throws IOException {
			matrixID = in.readUnsignedShort();
			typeIsMatrix = in.readBoolean();
			replacementCondition = in.readUnsignedByte();
			for (int i = 0; i < replacementValues.length; i++) {
				replacementValues[i] = in.readUnsignedShort();
			}
			in.readShort(); //padding = 0xFFFF
		}
		
		public void write(DataOutput out) throws IOException {
			out.writeShort(matrixID);
			out.writeBoolean(typeIsMatrix);
			out.writeByte(replacementCondition);
			for (int rv : replacementValues) {
				out.writeShort(rv);
			}
			out.writeShort(0xFFFF); //padding
		}
	}
}
