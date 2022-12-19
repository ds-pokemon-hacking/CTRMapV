package ctrmap.util.tools.qos;

import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QOSFSIndexVerifier {

	public static void main(String[] args) {
		try {
			String rootPath = "D:\\Emugames\\DS\\2934 - 007 - Quantum of Solace (USA) (En,Fr)_Extracted\\data";
			FSFile root = new DiskFile(rootPath);
			FSFile fsIndex = root.getChild("fsindex-US.bin");
			DataIOStream io = fsIndex.getDataIOStream();
			QOSFSIndex fsIdx = new QOSFSIndex(io);
			io.close();
			int foundCount = scanDir(fsIdx, root, root);
			System.out.println("Found " + foundCount + " files out of " + fsIdx.files.size());
		} catch (IOException ex) {
			Logger.getLogger(QOSFSIndexVerifier.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static int scanDir(QOSFSIndex fsIdx, FSFile dir, FSFile root) {
		int foundCount = 0;
		for (FSFile subFile : dir.listFiles()) {
			if (!subFile.isDirectory()) {
				String name = subFile.getPathRelativeTo(root);
				if (!name.equals("fsindex-US.bin")) {
					QOSFSIndex.QOSFSFile entry = fsIdx.getEntry(name);
					if (entry != null) {
						foundCount++;
					} else {
						System.out.println("Didn't find file in index! Name: " + name);
					}
				}
			}
			else {
				foundCount += scanDir(fsIdx, subFile, root);
			}
		}
		return foundCount;
	}
}
