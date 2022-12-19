package ctrmap.util.tools.ovl;

import rpm.util.AutoRelGenerator;
import rpm.elfconv.ESDBSegmentInfo;
import rpm.elfconv.ExternalSymbolDB;
import rpm.format.rpm.RPM;
import rpm.format.rpm.RPMExternalRelocator;
import rpm.format.rpm.RPMRelocation;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.util.ParsingUtils;
import java.io.IOException;
import ctrmap.formats.ntr.common.compression.BLZ;
import ctrmap.formats.ntr.rom.OverlayTable;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import rpm.format.rpm.RPMMetaData;
import xstandard.fs.FSUtil;
import xstandard.math.MathEx;
import xstandard.text.FormattingUtils;
import java.util.HashSet;
import java.util.UUID;

public class CodeInjectionSystem implements RPMExternalRelocator, RPMSplitManager {

	public static final String CISYS_ROMFS_DIR = "codeinjection";
	public static final String CISYS_RPM_SYMFILE_METAVALUE_KEY = "SymbolFile";

	private static final CodeInjectionSetup DEFAULT_CODEINJECTION_SETUP = new CodeInjectionSetup(344, 0x021FD0C0, 0x8000);
	
	private NTRGameFS fs;
	private ExternalSymbolDB esdb;
	private OverlayTable ovlTable;

	private OvlPatchSystem psys;

	public CodeInjectionSystem(NTRGameFS gameFS, ExternalSymbolDB esdb) {
		this(gameFS, esdb, DEFAULT_CODEINJECTION_SETUP);
	}
	
	public CodeInjectionSystem(NTRGameFS gameFS, ExternalSymbolDB esdb, CodeInjectionSetup setup) {
		fs = gameFS;
		this.esdb = esdb;
		ovlTable = fs.getARM9OverlayTable();

		FSFile ovlFile = fs.getOverlay(setup.overlayId);
		if (OvlPatchSystem.isPatchSystemInitialized(ovlFile)) {
			psys = new OvlPatchSystem(ovlFile);
			psys.bindRPMSplitManager(this);
		} else {
			if (ovlFile == null) {
				ovlFile = fs.getNewOverlay(setup.overlayId);
			}
			if (ovlFile.exists()) {
				ovlFile.delete();
			}
			ovlFile.setBytes(new byte[setup.overlaySize]);
			OverlayTable.OverlayInfo i = ovlTable.getOrCreateOverlayInfo(setup.overlayId);
			i.mountSize = setup.overlaySize;
			i.mountAddress = setup.overlayBaseAddress;
			i.staticInitializersStart = setup.overlayBaseAddress;
			i.staticInitializersEnd = setup.overlayBaseAddress;
			ovlTable.write();

			psys = OvlPatchSystem.initPatchSystem(ovlFile, setup.overlayBaseAddress);
			psys.bindRPMSplitManager(this);

			savePSysData();
		}
	}
	
	public static boolean isCISInitialized(NTRGameFS fs) {
		FSFile ovl344 = fs.getOverlay(344);
		return OvlPatchSystem.isPatchSystemInitialized(ovl344);
	}

	public int getMaxOvlAddr() {
		int max = 0;
		for (OverlayTable.OverlayInfo i : ovlTable.overlays) {
			if ((i.mountAddress & 0xFFF00000) <= 0x02300000) {
				//Do not take VRAM overlays into consideration
				int now = i.mountAddress + i.mountSize;
				if (now > max) {
					max = now;
					System.out.println("New biggest overlay " + i.fileId + " start " + Integer.toHexString(i.mountAddress) + " size " + Integer.toHexString(i.mountSize));
				}
			}
		}
		max = MathEx.padInteger(max, 16);
		return max;
	}

	public OvlPatchSystem getPSys() {
		return psys;
	}

	public ExternalSymbolDB getESDB() {
		return esdb;
	}

	public void queueFilePatch(FSFile df) {
		OvlCodeEntry ci;
		if (RPM.isRPM(df)) {
			ci = psys.queueRPMFilePatch(df);
		} else {
			ci = psys.queueElfFilePatch(df, esdb);
		}
	}
	
	public void queueRPMPatch(RPM rpm, String name) {
		psys.queueRPMFilePatch(rpm, name);
	}

	public final void savePSysData() {
		psys.write();

		for (OvlCodeEntry e : psys.codeInfo) {
			try {
				e.getFullRPM().doExternalRelocations(this);
			} catch (Exception ex) {
				throw new RuntimeException("Error while processing relocations of module " + e.name, ex);
			}
		}
	}

	@Override
	public void processExternalRelocation(RPM rpm, RPMRelocation rel) {
		ESDBSegmentInfo seg = esdb.getSegByName(rel.target.module);
		if (seg != null) {
			AutoRelGenerator.NTRSegmentType segType = AutoRelGenerator.NTRSegmentType.fromName(seg.segmentType);
			if (segType != null) {
				FSFile relocFile = null;
				int addrBase = 0;
				int ovlId = -1;
				switch (segType) {
					case EXECUTABLE:
						relocFile = fs.getArm9Bin();
						addrBase = fs.getARM9LoadAddr();
						break;
					case OVERLAY:
						ovlId = ParsingUtils.parseBasedIntOrDefault(seg.segmentName, -1);
						if (ovlId == -1) {
							throw new IllegalArgumentException("Invalid overlay: " + ovlId);
						}
						relocFile = fs.getOverlay(ovlId);
						addrBase = fs.getOvlLoadAddr(ovlId);
						break;
				}

				if (relocFile == null || !relocFile.exists()) {
					throw new NullPointerException("Relocation file " + seg.segmentName + " of type " + seg.segmentType + " does not exist!");
				}
				BLZ.BLZHeader blzHdr = BLZ.getBLZHeader(relocFile);
				if (blzHdr != null && blzHdr.valid()) {
					relocFile.setBytes(BLZ.BLZ_Decompress(relocFile.getBytes()));
				}

				try (DataIOStream io = relocFile.getDataIOStream()) {
					io.setBase(addrBase);
					System.out.println("Hooking to " + Integer.toHexString(rel.target.getAddrHWordAligned()) + " in " + relocFile + " (base " + Integer.toHexString(addrBase) + ") from " + Integer.toHexString(rel.source.getWritableAddress()));

					io.seek(rel.target.getAddrHWordAligned());
					RPM.writeRelocationDataByType(rpm, rel, io);
				} catch (IOException ex) {
					throw new RuntimeException("Failed to apply external relocation!", ex);
				}

				if (ovlId != -1) {
					//Flag the overlay as uncompressed
					ovlTable.updateByFile(ovlId, relocFile);
					ovlTable.write();
				}
			}
		} else {
			System.out.println("Could not apply extreloc - invalid segment name " + rel.target.module);
		}
	}

	private static RPMMetaData.RPMMetaValue getSymbolFileNameMetaValue(RPM codeRpm) {
		return codeRpm.metaData.findValue(CISYS_RPM_SYMFILE_METAVALUE_KEY);
	}

	private FSFile getRomfsWorkDir() {
		FSFile romfsDir = fs.getDataFile(CISYS_ROMFS_DIR);
		if (!romfsDir.exists()) {
			romfsDir.mkdirs();
		}
		return romfsDir;
	}

	private FSFile getSymbolRPMFile(RPM codeRpm) {
		FSFile romfsDir = getRomfsWorkDir();
		RPMMetaData.RPMMetaValue val = getSymbolFileNameMetaValue(codeRpm);
		if (val == null) {
			return null;
		}
		FSFile symFile = romfsDir.getChild(val.stringValue());
		return symFile;
	}

	@Override
	public RPM getSymbolRPMForCodeRPM(RPM codeRpm) {
		FSFile symFile = getSymbolRPMFile(codeRpm);
		if (symFile != null && (symFile.exists() && RPM.isRPM(symFile))) {
			try {
				return new RPM(symFile);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to read symbol file " + symFile, ex);
			}
		}
		return null;
	}

	private String getFriendliestRPMSymFileName(RPM rpm) {
		if (psys != null) {
			FSFile romfsWorkDir = getRomfsWorkDir();

			HashSet<String> existingNames = new HashSet<>(romfsWorkDir.list());

			String baseName = null;
			for (OvlCodeEntry e : psys.codeInfo) {
				if (e.getSymbolRPM(this) == rpm) {
					baseName = "RPMSYM-" + FSUtil.getFileNameWithoutExtension(e.name);
				}
			}
			if (baseName == null) {
				baseName = "RPMSYM-"
					+ FormattingUtils.getStrWithLeadingZeros(
						8,
						Long.toHexString(UUID.randomUUID().getLeastSignificantBits()).toUpperCase()
					);
			}
			String actualName;
			String rpmExtension = RPM.EXTENSION_FILTER.getPrimaryExtension();
			int counter = 1;
			while (true) {
				actualName = (counter == 1) ? baseName + rpmExtension : baseName + "_" + counter + rpmExtension;
				if (!existingNames.contains(actualName)) {
					break;
				}
				counter++;
			}
			return actualName;
		}
		return null;
	}

	private FSFile createSymbolRPMFile(RPM symRpm) {
		String symFileName = getFriendliestRPMSymFileName(symRpm);
		FSFile symFile = getRomfsWorkDir().getChild(symFileName);
		symRpm.metaData.putValue(new RPMMetaData.RPMMetaValue(CISYS_RPM_SYMFILE_METAVALUE_KEY, symFileName));
		return symFile;
	}

	@Override
	public void writeSymbolRPM(RPM symRpm) {
		FSFile symFile = getSymbolRPMFile(symRpm);
		if (symFile == null) {
			symFile = createSymbolRPMFile(symRpm);
		}
		symFile.setBytes(symRpm.getBytes(false));
	}

	@Override
	public void removeSymbolRPMForCodeRPM(RPM codeRpm) {
		FSFile symFile = getSymbolRPMFile(codeRpm);
		if (symFile != null && symFile.exists()) {
			symFile.delete();
		}
	}
}
