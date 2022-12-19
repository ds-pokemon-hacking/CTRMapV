package ctrmap.formats.ntr.narc;

import xstandard.fs.accessors.arc.ArcInput;
import xstandard.fs.accessors.arc.DotArc;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.fs.accessors.MemoryFile;
import xstandard.fs.accessors.arc.ArcFile;
import xstandard.io.util.StringIO;
import xstandard.util.ProgressMonitor;
import xstandard.fs.TempFileAccessor;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.WriteableStream;
import xstandard.io.base.impl.access.MemoryStream;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.impl.ext.SubInputStream;
import xstandard.text.StringEx;
import ctrmap.formats.ntr.narc.blocks.*;

public class DirectNARC {

	public static boolean isNARC(FSFile fsf) {
		if (!fsf.exists()) {
			return false;
		}
		boolean r = false;

		try {
			DataInStream dis = fsf.getDataInputStream();
			if (StringIO.checkMagic(dis, NARC.MAGIC)) {
				r = true;
			}
			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(DirectNARC.class.getName()).log(Level.SEVERE, null, ex);
		}
		return r;
	}

	public static boolean isNARC(DataIOStream io) throws IOException {
		return StringIO.checkMagic(io, NARC.MAGIC);
	}

	public static int getDataMax(FSFile fsf) {
		try {
			DataInStream dis = fsf.getDataInputStream();

			dis.skipBytes(0x18);
			int dataMax = dis.readInt();

			dis.close();
			return dataMax;
		} catch (IOException ex) {
			Logger.getLogger(DirectNARC.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}

	public static int getDataMax(DataIOStream io) throws IOException {
		io.seek(0x18);
		return io.readInt();
	}

	public static int getUncompDataSize(FSFile arcFile, String path) {
		try {
			String[] pathElems = path.split("/");
			Integer fileId = Integer.parseInt(pathElems[0]);

			DataIOStream io = arcFile.getDataIOStream();
			NARC garc = new NARC(io);

			io.seek(garc.sectionSize + 0xC + fileId * FATB.DataInfo.BYTES);
			FATB.DataInfo di = new FATB.DataInfo(io);
			int len = di.endOffset - di.startOffset;

			io.close();
			return len;
		} catch (IOException ex) {
			Logger.getLogger(DirectNARC.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}

	private static FATB.DataInfo getDataInfoForPath(FSFile arcFile, DataInStream in, String path) throws IOException {
		int entryIdx = Integer.parseInt(StringEx.splitOnecharFast(path, '/')[0]);

		NARC garc = new NARC(in);
		in.seekNext(garc.sectionSize);
		int fatbStart = in.getPosition();
		FATB fatb = new FATB(in);
		if (entryIdx < 0 || entryIdx >= fatb.entries.size()) {
			in.close();
			throw new RuntimeException("NARC data index out of bounds! (" + entryIdx + " for archive " + arcFile + ")");
		}
		in.seekNext(fatbStart + fatb.sectionSize);
		int fntbStart = in.getPosition();
		FNTB fntb = new FNTB(in);
		in.seekNext(fntbStart + fntb.sectionSize);
		int fimbStart = in.getPosition();
		FIMG fimb = new FIMG(in);
		in.seekNext(fimbStart + FIMG.HEADER_BYTES);
		FATB.DataInfo dataInfo = fatb.entries.get(entryIdx);
		return dataInfo;
	}

	public static byte[] getData(FSFile arcFile, String path) {
		try {
			DataInStream in = arcFile.getDataInputStream();
			FATB.DataInfo dataInfo = getDataInfoForPath(arcFile, in, path);
			in.skipBytes(dataInfo.startOffset);
			byte[] buf = new byte[dataInfo.endOffset - dataInfo.startOffset];
			in.read(buf);

			in.close();
			return buf;
		} catch (IOException ex) {
			Logger.getLogger(DirectNARC.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static void transformInputsByDotArc(DotArc da, ArcInput... inputs) {
		DotArc.CompressorBehavior defaultBhv = da.defaultCompressionDirective;
		for (ArcInput in : inputs) {
			DotArc.CompressorBehavior bhv = da.compressionDirectives.get(in.targetPath);
			if (!in.compressAuto) {
				da.compressionDirectives.put(in.targetPath, in.compressLZ ? DotArc.CompressorBehavior.COMPRESS : DotArc.CompressorBehavior.DO_NOT_COMPRESS);
			} else {
				if (bhv == null) {
					bhv = defaultBhv;
				} else {
					System.out.println("DotArc config for file " + in.targetPath + " overriding automatic compression: " + bhv);
				}
				switch (bhv) {
					case AUTO:
						in.compressAuto = true;
						break;
					case COMPRESS:
						in.compressAuto = false;
						in.compressLZ = true;
						break;
					case DO_NOT_COMPRESS:
						in.compressAuto = false;
						in.compressLZ = false;
						break;
				}
			}
		}
	}

	public static void setData(FSFile arcFile, List<ArcInput> inputs) {
		setData(arcFile, inputs.toArray(new ArcInput[inputs.size()]));
	}

	public static void setData(FSFile arcFile, ArcInput... inputs) {
		setData(arcFile, null, inputs);
	}

	public static void setData(FSFile arcFile, ProgressMonitor monitor, ArcInput... inputs) {
		DotArc dotArc = null;
		int hiddenCount = 0;

		for (ArcInput in : inputs) {
			if (in.targetPath.startsWith(".")) {
				hiddenCount++;
				if (in.targetPath.equals(DotArc.DOT_ARC_SIGNATURE)) {
					dotArc = new DotArc(in.data);
					break;
				}
			}
		}

		if (dotArc == null) {
			FSFile dotArcCand = arcFile.getChild(DotArc.DOT_ARC_SIGNATURE);
			if (dotArcCand != null) {
				dotArc = new DotArc(dotArcCand);
			}
		}

		if (dotArc != null) {
			transformInputsByDotArc(dotArc, inputs);
			dotArc.updateAndWrite();
		}

		if (inputs == null || inputs.length - hiddenCount == 0) {
			return;
		}

		if (monitor == null) {
			monitor = new ProgressMonitor.DummyProgressMonitor();
		}
		try {
			monitor.setProgressPercentage(0);
			monitor.setProgressSubTitle("Preparing...");

			//We can use the OS's filesystem infrastructure instead of costly buffer reads in case of memory files
			DiskFile df = null;
			if (arcFile instanceof DiskFile) {
				df = (DiskFile) arcFile;
			} else if (arcFile instanceof ArcFile) {
				ArcFile af = (ArcFile) arcFile;
				FSFile ff = af.getSource();
				if (ff instanceof DiskFile) {
					df = (DiskFile) ff;
				}
			}

			File tempFile;

			if (df != null) {
				tempFile = new File(df.getFile().getAbsoluteFile().getParent() + "/garc_repack_" + arcFile.getName() + UUID.randomUUID());
			} else {
				tempFile = TempFileAccessor.createTempFile("garc_repack_" + arcFile.getName());
			}

			FSFile tempOutFile = new DiskFile(tempFile);

			DataIOStream in = arcFile.getDataIOStream();
			DataIOStream out = tempOutFile.getDataIOStream();
			NARC garc = new NARC(in);
			in.seek(garc.sectionSize);
			int fatbStart = in.getPosition();
			FATB fatb = new FATB(in);
			in.seek(fatbStart + fatb.sectionSize);
			int fntbStart = in.getPosition();
			FNTB fntb = new FNTB(in);
			in.seek(fntbStart + fntb.sectionSize);
			int fimbStart = in.getPosition();
			FIMG fimb = new FIMG(in);
			in.seek(fimbStart + FIMG.HEADER_BYTES);

			insertEntries(in, out, garc, fatb, fntb, fimb, monitor, inputs);

			monitor.setProgressPercentage(100);
			monitor.setProgressSubTitle("Done.");

			in.close();
			out.close();

			if (df != null) {
				Files.move(tempFile.toPath(), df.getFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else {
				FSUtil.copy(tempOutFile, arcFile);
				tempOutFile.delete();
			}
		} catch (IOException ex) {
			Logger.getLogger(DirectNARC.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void insertEntries(DataIOStream inFile, DataIOStream outFile, NARC garc, FATB fatb, FNTB fntb, FIMG fimb, ProgressMonitor monitor, ArcInput... inputs) throws IOException {
		int oldDataStart = garc.sectionSize + fntb.sectionSize + fatb.sectionSize + FIMG.HEADER_BYTES;

		Map<FATB.DataInfo, ArcInput> customEntries = new HashMap<>();

		//Update FATB and store the changed entries
		int idx = 0;
		for (ArcInput in : inputs) {
			monitor.setProgressPercentage((int) ((float) (idx) / inputs.length * 75));
			monitor.setProgressSubTitle("Preparing " + in.targetPath);
			idx++;
			if (in.targetPath.startsWith(".")) {
				continue; //special directives, don't insert
			}
			FATB.DataInfo e = fatb.insertEntry(in);
			customEntries.put(e, in);
		}

		monitor.setProgressSubTitle("Rebuilding tables...");

		monitor.setProgressSubTitle("Allocating data...");

		outFile.seek(0);
		byte[] tempAlloc = new byte[garc.sectionSize + fntb.sectionSize + fatb.sectionSize + FIMG.HEADER_BYTES];
		outFile.write(tempAlloc);
		int dataStart = outFile.getPosition();

		List<FATB.DataInfo> sortedEntries = new ArrayList<>(fatb.entries);
		sortedEntries.sort((FATB.DataInfo o1, FATB.DataInfo o2) -> o1.startOffset - o2.startOffset);

		idx = 0;
		for (FATB.DataInfo e : sortedEntries) {
			monitor.setProgressPercentage(75 + (int) (((float) idx) / sortedEntries.size()) * 25);
			monitor.setProgressSubTitle("Writing entry " + (idx + 1) + " of " + sortedEntries.size());
			//We have to do a sequential copy instead of several bigger block copies since the archive could be realllly big

			//Store the new data offset to the offset table
			FATB.DataInfo di = e;
			int oldStartOffset = di.startOffset;
			di.startOffset = outFile.getPosition() - dataStart;
			if (customEntries.containsKey(di)) {
				outFile.write(FSUtil.readFileToBytes(customEntries.get(di).data));
				di.endOffset = outFile.getPosition() - dataStart;
				while (outFile.getPosition() % 4 != 0) {
					outFile.write(0xFF);
				}
			} else {
				inFile.seek(oldStartOffset + oldDataStart);
				byte[] buf = new byte[di.endOffset - oldStartOffset];
				inFile.read(buf);
				outFile.write(buf);
				di.endOffset = outFile.getPosition() - dataStart;
				while (outFile.getPosition() % 4 != 0) {
					outFile.write(0xFF);
				}
			}
		}

		monitor.setProgressSubTitle("Finalizing headers...");

		//Rebuild FATB from updated FIMB
		//fatb.buildFromFIMBOffsetMap(newOffsets, garc);
		//Update section metadata
		fimb.partitionSize = outFile.getPosition() - dataStart + FIMG.HEADER_BYTES;
		garc.arcSize = outFile.getPosition();

		//Write headers to the new GARC
		outFile.seek(0);
		garc.write(outFile);
		fatb.write(outFile);
		fntb.write(outFile);
		fimb.write(outFile);

		monitor.setProgressSubTitle("Done.");
	}

	public static ReadableStream createInputStream(FSFile arc, String path) {
		try {
			DataInStream in = arc.getDataInputStream();
			FATB.DataInfo dataInfo = getDataInfoForPath(arc, in, path);
			
			return new SubInputStream(in, dataInfo.startOffset, dataInfo.endOffset);
		} catch (IOException ex) {
			Logger.getLogger(DirectNARC.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static WriteableStream createOutputStream(FSFile arc, String path) {
		ArcInput ai = new ArcInput(path, (byte[]) null);

		return new MonitoredRABA(arc, ai);
	}

	public static IOStream createIO(FSFile arc, String path) {
		ArcInput ai = new ArcInput(path, getData(arc, path));

		return new MonitoredRABA(arc, ai);
	}

	public static class MonitoredRABA extends MemoryStream {

		private FSFile arc;
		private ArcInput in;

		public MonitoredRABA(FSFile arc, ArcInput input) {
			super(FSUtil.readFileToBytes(input.data));
			this.arc = arc;
			in = input;
		}

		@Override
		public void close() {
			in.data = new MemoryFile(in.targetPath, toByteArray());
			DirectNARC.setData(arc, in);
		}
	}
}
