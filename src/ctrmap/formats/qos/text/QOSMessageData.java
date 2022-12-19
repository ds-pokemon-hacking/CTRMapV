
package ctrmap.formats.qos.text;

import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QOSMessageData {
	public List<String> lines = new ArrayList<>();
	
	public QOSMessageData(FSFile fsf) {
		try {
			byte[] data = new byte[fsf.length() + 1];
			DataInStream in = fsf.getDataInputStream();
			in.read(data);
			in.close();
			
			in = new DataInStream(data);
			
			int len = in.getLength();
			while (in.getPosition() < len) {
				lines.add(in.readString());
			}
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(QOSMessageData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void dump(FSFile dest) {
		PrintStream out = new PrintStream(dest.getNativeOutputStream());
		for (int i = 0; i < lines.size(); i++) {
			out.println(i + ": " + lines.get(i));
		}
		out.close();
	}
	
	public static void main(String[] args) {
		new QOSMessageData(new DiskFile("D:\\Emugames\\DS\\2934 - 007 - Quantum of Solace (USA) (En,Fr)_Extracted\\data_uncomp\\localize.English.bin"))
			.dump(new DiskFile("D:\\Emugames\\DS\\2934 - 007 - Quantum of Solace (USA) (En,Fr)_Extracted\\research\\message.txt"));
	}
}
