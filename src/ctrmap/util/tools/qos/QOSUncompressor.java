package ctrmap.util.tools.qos;

import ctrmap.formats.ntr.common.compression.LZ1X;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.iface.WriteableStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class QOSUncompressor {

	private static final int QOS_PACKET_TYPE_END = 'E';
	private static final int QOS_PACKET_TYPE_LZ = 'L';
	private static final int QOS_PACKET_TYPE_ZLIB = 'Z';

	public static void main(String[] args) {
		DiskFile src = new DiskFile("D:\\Emugames\\DS\\2934 - 007 - Quantum of Solace (USA) (En,Fr)_Extracted\\data");
		DiskFile dst = new DiskFile("D:\\Emugames\\DS\\2934 - 007 - Quantum of Solace (USA) (En,Fr)_Extracted\\data_uncomp");
		try {
			uncompressAll(src, dst);
		} catch (IOException ex) {
			Logger.getLogger(QOSUncompressor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void uncompressAll(FSFile sourceDir, FSFile destDir) throws IOException {
		FSFile fsIndexFile = sourceDir.getChild("fsindex-US.bin");
		DataInStream fsIdxStream = fsIndexFile.getDataInputStream();
		QOSFSIndex fsIndex = new QOSFSIndex(fsIdxStream);
		fsIdxStream.close();
		destDir.mkdirs();
		uncompressDir(fsIndex, sourceDir, destDir, sourceDir);
	}

	private static void uncompressDir(QOSFSIndex idx, FSFile src, FSFile dst, FSFile root) throws IOException {
		for (FSFile child : src.listFiles()) {
			if (child.isDirectory()) {
				FSFile outDir = dst.getChild(child.getName());
				outDir.mkdir();
				uncompressDir(idx, child, outDir, root);
			} else {
				FSFile outFile = dst.getChild(child.getName());
				String relPath = child.getPathRelativeTo(root);
				QOSFSIndex.QOSFSFile entry = idx.getEntry(relPath);
				boolean compressed = false;
				if (entry != null) {
					compressed = entry.isCompressed;
				}
				if (!compressed) {
					child.copyTo(outFile);
				} else {
					DataInStream comp = child.getDataInputStream();
					WriteableStream uncompOut = outFile.getOutputStream();
					try {
						uncompress(comp, uncompOut);
						comp.close();
						uncompOut.close();
					}
					catch (Exception ex) {
						System.err.println("Error uncompressing file " + child);
						throw new RuntimeException(ex);
					}
				}
			}
		}
	}

	public static void uncompress(DataInStream in, WriteableStream out) throws IOException {
		List<QOSPacket> packets = new ArrayList<>();
		QOSPacket readPacket;
		do {
			readPacket = new QOSPacket(in);
			packets.add(readPacket);
		} while (readPacket.type != QOS_PACKET_TYPE_END);

		for (int i = 0; i < packets.size() - 1; i++) {
			QOSPacket packet = packets.get(i);
			QOSPacket nextPacket = packets.get(i + 1);
			in.seekNext(packet.chunkStart);
			int readSize = nextPacket.chunkStart - packet.chunkStart;
			switch (packet.type) {
				case QOS_PACKET_TYPE_END:
					throw new RuntimeException("End packet not expected here!");
				case QOS_PACKET_TYPE_ZLIB:
					Inflater inflater = new Inflater();
					InflaterInputStream compStream = new InflaterInputStream(in.getInputStream(), inflater, readSize);
					byte[] buffer = new byte[0x4000];

					int read;
					while ((read = compStream.read(buffer)) != -1) {
						out.write(buffer, 0, read);
					}
					break;
				case QOS_PACKET_TYPE_LZ:
					byte[] uncompressed = LZ1X.decompress(in);
					out.write(uncompressed);
					break;
			}
		}
	}

	private static class QOSPacket {

		public int type;
		public int chunkStart;

		public QOSPacket(DataInStream in) throws IOException {
			type = in.readUnsignedByte();
			chunkStart = in.readUnsignedInt24();
		}
	}
}
