package ctrmap.util.tools.qos;

import ctrmap.formats.ntr.rom.srl.newlib.NDSROMFile;
import ctrmap.formats.ntr.rom.srl.newlib.NTRFSFile;
import ctrmap.formats.ntr.rom.srl.newlib.NTRFSFileInfo;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QOSFileSystemFinalizer {
	
	public static void main(String[] args) {
		FSFile rom = new DiskFile("D:\\Emugames\\DS\\qos.nds");
		try {
			fixROM(rom);
		} catch (IOException ex) {
			Logger.getLogger(QOSFileSystemFinalizer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void fixROM(FSFile rom) throws IOException {
		NDSROMFile file = new NDSROMFile(rom);
		FSFile dataDir = file.getChild("data");
		FSFile fsIndexFile = file.getChild("data/fsindex-US.bin");
		if (fsIndexFile instanceof NTRFSFile) {
			DataIOStream io = rom.getDataIOStream();
			NTRFSFile ntrFsIndexFile = (NTRFSFile) fsIndexFile;
			NTRFSFileInfo rawInfo = ntrFsIndexFile.getFileInfo();
			io.seek(rawInfo.offset);
			QOSFSIndex fsIndex = new QOSFSIndex(io);
			fixFiles(fsIndex, dataDir, dataDir);
			io.seek(rawInfo.offset);
			fsIndex.write(io);
			io.close();
		}
	}

	private static void fixFiles(QOSFSIndex fsIndex, FSFile dir, FSFile root) {
		for (FSFile child : dir.listFiles()) {
			if (child.isDirectory()) {
				fixFiles(fsIndex, child, root);
			} else {
				String path = child.getPathRelativeTo(root);
				QOSFSIndex.QOSFSFile entry = fsIndex.getEntry(path);
				if (entry != null) {
					entry.romOffset = ((NTRFSFile) child).getFileInfo().offset;
				} else {
					System.out.println("Warn: entry for file " + path + " not found.");
				}
			}
		}
	}
}
