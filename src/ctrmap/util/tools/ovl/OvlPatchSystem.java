package ctrmap.util.tools.ovl;

import xstandard.fs.FSFile;
import xstandard.io.InvalidMagicException;
import xstandard.io.structs.PointerTable;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.util.StringIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rpm.elfconv.ELF2RPM;
import rpm.elfconv.ExternalSymbolDB;
import rpm.format.rpm.RPM;
import xstandard.io.base.impl.ext.data.DataIOStream;

/**
 *
 */
public class OvlPatchSystem {

	public static final int OVLDATA_HDRLEN = 0x10;
	public static final String OVLDATA_MAGIC = "OVL0";

	private FSFile source;
	private int baseOffset;

	public List<OvlCodeEntry> codeInfo = new ArrayList<>();

	private RPMSplitManager rpmSplitMng;

	public OvlPatchSystem(FSFile overlay) {
		this(overlay, true);
	}

	private OvlPatchSystem(FSFile overlay, boolean read) {
		source = overlay;
		if (read) {
			read();
		}
	}

	public static boolean isPatchSystemInitialized(FSFile overlay) {
		if (overlay == null || !overlay.isFile()) {
			return false;
		}
		try {
			DataIOStream io = overlay.getDataIOStream();
			io.seek(io.getLength() - OVLDATA_HDRLEN);
			if (StringIO.checkMagic(io, OVLDATA_MAGIC)) {
				io.close();
				return true;
			} else {
				io.close();
				return false;
			}
		} catch (IOException ex) {
			Logger.getLogger(OvlPatchSystem.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	public static OvlPatchSystem initPatchSystem(FSFile overlay, int ovlBaseOffset) {
		OvlPatchSystem ps = new OvlPatchSystem(overlay, false);
		ps.baseOffset = ovlBaseOffset;
		return ps;
	}

	public void bindRPMSplitManager(RPMSplitManager mng) {
		this.rpmSplitMng = mng;
		if (mng != null) {
			for (OvlCodeEntry e : codeInfo) {
				mergeSplitRPM(e);
			}
		}
	}

	public int getBaseOffset() {
		return baseOffset;
	}

	private boolean checkFileSizeNewPatchFit(OvlCodeEntry newPatch) {
		OvlCodeEntry existing = getPatchByName(newPatch.name);
		int startOffset = 0;
		int changedSize = newPatch.getCodeRPM(rpmSplitMng).getByteSize(true);
		if (existing != null) {
			startOffset = existing.offset;
			int idx = codeInfo.indexOf(existing) + 1;
			for (; idx < codeInfo.size(); idx++) {
				changedSize += codeInfo.get(idx).getCodeRPM(rpmSplitMng).getByteSize(true);
			}
		}
		else if (!codeInfo.isEmpty()) {
			startOffset = codeInfo.get(codeInfo.size() - 1).offset;
		}
		int bottomMax = startOffset + changedSize;
		
		int psdLength = OVLDATA_HDRLEN;
		for (OvlCodeEntry i : codeInfo) {
			psdLength += i.getStructSize();
		}
		psdLength += Integer.BYTES * (1 + codeInfo.size()); //pointer table

		int topMin = (source.length() - psdLength) & 0xFFFFFFF0;

		return bottomMax <= topMin;
	}

	private OvlCodeEntry queueFilePatchImpl(RPM rpm, String name) {
		OvlCodeEntry ce = new OvlCodeEntry(rpm);
		ce.name = name;
		if (!checkFileSizeNewPatchFit(ce)) {
			throw new RuntimeException("Patch " + name + " won't fit into the overlay!");
		}
		OvlCodeEntry existing = getPatchByName(ce.name);
		if (existing != null) {
			int idx = codeInfo.indexOf(existing);
			codeInfo.remove(existing);
			if (idx < codeInfo.size()) {
				codeInfo.get(idx).updated = true; //update all next entries
			}
		}
		codeInfo.add(ce);
		return ce;
	}

	public void removePatch(OvlCodeEntry e) {
		int index = codeInfo.indexOf(e);
		codeInfo.remove(e);
		if (index < codeInfo.size()) {
			codeInfo.get(index).updated = true;
		}
		if (rpmSplitMng != null) {
			rpmSplitMng.removeSymbolRPMForCodeRPM(e.getCodeRPM(rpmSplitMng));
		}
	}

	public OvlCodeEntry getPatchByName(String name) {
		for (OvlCodeEntry e : codeInfo) {
			if (e.name.equals(name)) {
				return e;
			}
		}
		return null;
	}

	public OvlCodeEntry queueElfFilePatchAndWrite(FSFile f, FSFile esdb) {
		return queueElfFilePatchAndWrite(f, new ExternalSymbolDB(esdb));
	}

	public OvlCodeEntry queueElfFilePatchAndWrite(FSFile f, ExternalSymbolDB esdb) {
		OvlCodeEntry ce = queueElfFilePatch(f, esdb);
		write();
		return ce;
	}

	public OvlCodeEntry queueElfFilePatch(FSFile f, ExternalSymbolDB esdb) {
		RPM rpm = ELF2RPM.getRPM(f, esdb);
		return queueFilePatchImpl(rpm, f.getName());
	}

	public OvlCodeEntry queueRPMFilePatch(FSFile f) {
		return queueFilePatchImpl(new RPM(f), f.getName());
	}

	public OvlCodeEntry queueRPMFilePatch(RPM rpm, String name) {
		return queueFilePatchImpl(rpm, name);
	}

	public OvlCodeEntry queueRPMFilePatchAndWrite(FSFile f) {
		OvlCodeEntry ce = queueRPMFilePatch(f);
		write();
		return ce;
	}

	private void read() {
		if (source != null) {
			try {
				DataIOStream io = source.getDataIOStream();

				io.seek(io.getLength() - OVLDATA_HDRLEN);
				if (!StringIO.checkMagic(io, OVLDATA_MAGIC)) {
					io.close();
					throw new InvalidMagicException("OvlPatchSystem magic not found.");
				}
				baseOffset = io.readInt();

				int patchSystemDataOffset = io.readInt();
				io.seek(patchSystemDataOffset);

				int patchCount = io.readInt();
				int[] patchInfoPtrs = new int[patchCount];
				for (int i = 0; i < patchCount; i++) {
					patchInfoPtrs[i] = io.readInt();
				}
				for (int pip : patchInfoPtrs) {
					io.seek(pip);
					OvlCodeEntry e = new OvlCodeEntry(io);
					mergeSplitRPM(e);
					codeInfo.add(e);
				}

				io.close();
			} catch (IOException ex) {
				Logger.getLogger(OvlPatchSystem.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private void mergeSplitRPM(OvlCodeEntry e) {
		if (rpmSplitMng != null) {
			RPM symRpm = rpmSplitMng.getSymbolRPMForCodeRPM(e.getCodeRPM(rpmSplitMng));

			if (symRpm != null) {
				e.getFullRPM().merge(symRpm);
			}
		}
	}

	public void write() {
		if (source != null) {
			try {
				DataIOStream io = source.getDataIOStream();

				io.seek(io.getLength() - OVLDATA_HDRLEN);
				io.writeStringUnterminated(OVLDATA_MAGIC);
				io.writeInt(baseOffset);
				TemporaryOffset codeInfoTableOffset = new TemporaryOffset(io);

				int psdLength = OVLDATA_HDRLEN;
				for (OvlCodeEntry i : codeInfo) {
					psdLength += i.getStructSize();
				}
				psdLength += Integer.BYTES * (1 + codeInfo.size()); //pointer table

				int targetTblOffs = io.getLength() - psdLength;
				targetTblOffs -= targetTblOffs % 16;
				io.seek(targetTblOffs);
				codeInfoTableOffset.setHere();
				List<TemporaryOffset> codeInfoPtrs = PointerTable.allocatePointerTable(codeInfo.size(), io);

				boolean writeAllRPMNext = false;
				int lastRPMEndPos = 0;

				for (int i = 0; i < codeInfo.size(); i++) {
					OvlCodeEntry ci = codeInfo.get(i);
					codeInfoPtrs.get(i).setHere();
					int codeInfoPos = io.getPosition();
					if (ci.updated) {
						writeAllRPMNext = true;

						lastRPMEndPos = (i > 0) ? codeInfo.get(i - 1).getEndOffset() : 0;
					}
					if (writeAllRPMNext) {
						io.seek(lastRPMEndPos);

						RPM codeRpm = ci.getCodeRPM(rpmSplitMng);
						ci.offset = lastRPMEndPos;
						System.out.println("Module " + ci.name + " new base address: 0x" + Integer.toHexString(baseOffset + ci.offset));
						ci.setBaseAddrNoUpdateBytes(baseOffset + ci.offset);

						if (rpmSplitMng != null) {
							rpmSplitMng.writeSymbolRPM(ci.getSymbolRPM(rpmSplitMng));
						}

						io.seek(io.getPosition() + codeRpm.getByteSize(true)); //allocate
						ci.length = io.getPosition() - ci.offset;

						io.pad(0x10);
						lastRPMEndPos = io.getPosition();
						io.seek(codeInfoPos);
					}
					codeInfo.get(i).write(io);
				}

				writeAllRPMNext = false;

				for (OvlCodeEntry ci : codeInfo) {
					if (ci.updated) {
						writeAllRPMNext = true;
					}
					if (writeAllRPMNext) {
						io.seek(ci.offset);

						RPM fullRPM = ci.getFullRPM();
						System.out.println("Applying internal relocations of " + ci.name + " for base " + Integer.toHexString(fullRPM.getCodeSegmentBase()));

						fullRPM.updateCodeImageForBaseAddr();
						io.write(ci.getCodeRPM(rpmSplitMng).getBytes(true));
						ci.updated = false;
					}
				}

				io.close();
			} catch (IOException ex) {
				Logger.getLogger(OvlPatchSystem.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
