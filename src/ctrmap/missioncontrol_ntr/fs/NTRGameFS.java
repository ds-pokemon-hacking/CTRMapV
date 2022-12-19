package ctrmap.missioncontrol_ntr.fs;

import xstandard.fs.FSFile;
import xstandard.fs.FSManager;
import ctrmap.formats.common.GameInfo;
import xstandard.fs.accessors.MemoryFile;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.formats.ntr.common.compression.BLZ;
import ctrmap.formats.ntr.rom.OverlayTable;
import ctrmap.formats.ntr.rom.srl.newlib.SRLHeader;

public class NTRGameFS {

	private SRLHeader romHeader;

	private FSManager fs;
	private GameInfo game;

	private OverlayTable overlayTable9;

	private ArcMode arcMode = ArcMode.PROCEDURAL;
	private NARCListingTable narcTable;

	public NTRGameFS(FSManager fs, GameInfo game) {
		this.fs = fs;
		this.game = game;

		overlayTable9 = new OverlayTable(fs.getFsFile(":y9:"));

		try {
			romHeader = new SRLHeader(getHdr());
		} catch (IOException ex) {
			Logger.getLogger(NTRGameFS.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void initAccurateArcFileSupport() {
		if (NARCListingTable.PTR_OFFSET.get(game) != -1) {
			narcTable = new NARCListingTable(this, game);
			arcMode = ArcMode.ACCURATE;
		}
	}

	public FSManager getFSManager() {
		return fs;
	}

	public int NARCGetDataMax(NARCRef ref) {
		FSFile arc = NARCGetArchive(ref);
		int max = arc.getVisibleChildCount();
		return max;
	}

	public FSFile NARCGetArchive(NARCRef ref) {
		return getDataFile(NARCGetArchivePath(ref));
	}

	public FSFile NARCGetArchive(int ARCID) {
		return getDataFile(NARCGetArchivePath(ARCID));
	}

	public FSFile NARCGet(NARCRef ref, int index) {
		return getDataFile(NARCGetArchivePath(ref) + "/" + index);
	}

	public String NARCGetArchivePath(int ARCID) {
		if (arcMode == ArcMode.PROCEDURAL || narcTable == null) {
			return NARCRef.arcIdToPath(ARCID);
		}
		else {
			return narcTable.getPath(ARCID);
		}
	}

	public String NARCGetArchivePath(NARCRef ref) {
		return NARCGetArchivePath(ref.getARCID(game));
	}

	public GameInfo getGameInfo() {
		return game;
	}

	public FSFile getDataFile(String path) {
		return fs.getFsFile("data/" + path);
	}

	public FSFile getArm9Bin() {
		return fs.getFsFile("arm9.bin");
	}
	
	public FSFile getDecompressedArm9BinMaybeRO() {
		FSFile file = getArm9Bin();
		BLZ.BLZHeader hdr = BLZ.getBLZHeader(file);
		if (hdr != null && hdr.valid()) {
			return new MemoryFile("arm9_decmp", BLZ.BLZ_Decompress(file.getBytes()));
		}
		return file;
	}

	public final FSFile getHdr() {
		return fs.getFsFile("header.bin");
	}

	public FSFile getFsRoot() {
		return fs.getFsFile("data");
	}

	public int getARM9LoadAddr() {
		return romHeader.arm9RamAddress;
	}

	public int getOvlLoadAddr(int ovlId) {
		return overlayTable9.getOverlayInfo(ovlId).mountAddress;
	}

	public FSFile getNewOverlay(int overlayId) {
		return fs.getFsFile(String.format("overlay/overlay_%04d.bin", overlayId));
	}

	public FSFile getDecompressedOverlayMaybeRO(int overlayId) {
		FSFile ovl = getOverlay(overlayId);

		if (overlayTable9.getOverlayInfo(overlayId).compressed) {
			return new MemoryFile(String.valueOf(overlayId), BLZ.BLZ_Decompress(ovl.getBytes()));
		}

		return ovl;
	}

	public FSFile getOverlay(int overlayId) {
		FSFile directMatch = fs.getFsFile(String.format("overlay/overlay_%04d.bin", overlayId));
		if (directMatch.exists()) {
			return directMatch;
		}

		List<? extends FSFile> ovlDir = fs.getFsFile("overlay").listFiles();
		Collections.sort(ovlDir, new Comparator<FSFile>() {
			@Override
			public int compare(FSFile o1, FSFile o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		if (overlayId < ovlDir.size()) {
			return ovlDir.get(overlayId);
		}
		return null;
	}

	public OverlayTable getARM9OverlayTable() {
		return overlayTable9;
	}

	public static enum ArcMode {
		PROCEDURAL,
		ACCURATE
	}
}
