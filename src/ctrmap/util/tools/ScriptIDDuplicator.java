package ctrmap.util.tools;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import xstandard.util.collections.IntList;

public class ScriptIDDuplicator {
	public static void main(String[] args) {
		FSFile inFile = new DiskFile("D:\\_REWorkspace\\pokescript_genv\\6\\6_1239.bin");
		FSFile outFile = new DiskFile("D:\\_REWorkspace\\pokescript_genv\\6\\6_1239_exp.bin");
		int newFirstEntryPointCount = 1000;
		
		DataIOStream in = inFile.getDataIOStream();
		DataOutStream out = outFile.getDataOutputStream();
		
		try {
			IntList offsets = new IntList();
			int max = in.getLength();
			int offs;
			while (in.getPosition() < max) {
				offs = in.readInt();
				if ((offs & 0xFFFF) == 0xFD13) {
					in.skipBytes(-2);
					break;
				}
				else {
					offsets.add(offs + in.getPosition());
				}
			}
			
			int oldOffsetCount = offsets.size();
			
			int refVal = offsets.get(0);
			int refValCount = offsets.size() - 1;
			while (refValCount >= 0) {
				if (offsets.get(refValCount) == refVal) {
					refValCount++;
					break;
				}
				refValCount--;
			}
			
			for (int i = refValCount; i < newFirstEntryPointCount; i++) {
				offsets.add(refVal);
			}
			int codeStartShift = (offsets.size() - oldOffsetCount) * Integer.BYTES;
			
			for (int i = 0; i < offsets.size(); i++) {
				out.writeInt((offsets.get(i) + codeStartShift) - (out.getPosition() + Integer.BYTES));
			}
			out.writeShort(0xFD13);
			
			FSUtil.transferStreams(in, out);
			in.close();
			out.close();
		} catch (IOException ex) {
			Logger.getLogger(ScriptIDDuplicator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
