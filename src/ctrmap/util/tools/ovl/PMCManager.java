package ctrmap.util.tools.ovl;

import ctrmap.formats.ntr.rom.OverlayTable;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import rpm.elfconv.ESDBSegmentInfo;
import rpm.elfconv.ExternalSymbolDB;
import rpm.format.rpm.RPM;
import rpm.format.rpm.RPMMetaData;
import rpm.format.rpm.RPMSymbol;
import rpm.util.AutoRelGenerator;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;

public class PMCManager {

	private static final int PMC_OVERLAY_SIZE = 0x3000;
	private static final String PMC_RPM_UID = "PMC.rpm";
	private static final String PMC_VERSION_META = "PMCVersion";
	private static final String PMC_GAME_ID_META = "PMCGameID";
	private static final String PMC_UNIVERSAL_VERSION = "13.2.0";

	private final NTRGameFS fs;
	private CodeInjectionSystem cis;
	private final ExternalSymbolDB esdb;

	public PMCManager(NTRGameFS fs) {
		this.fs = fs;
		esdb = genESDB(fs);
	}

	private static ExternalSymbolDB genESDB(NTRGameFS fs) {
		ExternalSymbolDB esdb = new ExternalSymbolDB();
		int id = 0;
		esdb.putSegment(new ESDBSegmentInfo(id, "ARM9", AutoRelGenerator.NTRSegmentType.EXECUTABLE.name()));
		id++;
		int ovlCount = fs.getARM9OverlayTable().overlays.size();
		for (int ovlId = 0; ovlId < ovlCount; ovlId++) {
			esdb.putSegment(new ESDBSegmentInfo(id, String.valueOf(ovlId), AutoRelGenerator.NTRSegmentType.OVERLAY.name()));
			id++;
		}
		return esdb;
	}

	private void ensureCIS() {
		if (cis == null) {
			boolean isNew = false;
			int overlayId = getPMCOverlayId();
			int baseAddress;
			if (overlayId == -1) {
				OverlayTable ovlTable = fs.getARM9OverlayTable();
				overlayId = ovlTable.overlays.size();

				baseAddress = CodeInjectionSystem.getMaxOvlAddrFromTable(ovlTable);

				writePMCOverlayId(overlayId);
				isNew = true;
			} else {
				baseAddress = fs.getOvlLoadAddr(overlayId);
			}
			System.out.println("PMCManager init @ overlay " + overlayId + " | Base address 0x" + Integer.toHexString(baseAddress) + ", buffer size 0x" + Integer.toHexString(PMC_OVERLAY_SIZE));
			cis = new CodeInjectionSystem(fs, esdb, new CodeInjectionSetup(overlayId, baseAddress, PMC_OVERLAY_SIZE));
			if (isNew) {
				//No longer need ARM9 decompression patch since our ROM packer handles that automatically
				//cis.queueFilePatch(new MemoryFile("arm9_decmp_off_HK_REL.rpm", CTRMapVResources.ACCESSOR.getByteArray("codeinjection/arm9_decmp_off_HK_REL.rpm")));
				cis.savePSysData();
			}
		}
	}

	public void updatePMC(RPM pmcRPM) {
		ensureCIS();
		setOverlaySizeToPMC(pmcRPM);
		cis.queueRPMPatch(pmcRPM, PMC_RPM_UID);
		cis.savePSysData();
	}

	private void setOverlaySizeToPMC(RPM pmcRPM) {
		RPMSymbol symb = pmcRPM.getSymbol("FULL_COPY_ARM9_0x0207B41C_ResizeMemoryForOvl344");
		if (symb == null) {
			for (RPMSymbol s : pmcRPM.symbols) {
				if (s.name != null && s.name.endsWith("AdjustHeapStart")) {
					symb = s;
					break;
				}
			}
		}

		if (symb != null) {
			try {
				DataIOStream code = pmcRPM.getCodeStream();
				code.seekUnbased(symb.address);
				int heapStart = cis.getMaxOvlAddr();
				code.writeInt(heapStart);
				System.out.println("Set system heap start to 0x" + Integer.toHexString(heapStart));
			} catch (IOException ex) {
				Logger.getLogger(PMCManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			System.err.println("Could not find overlay size symbol in PMC!");
		}
	}

	private FSFile getOverlayIdTxt() {
		return fs.getDataFile(CodeInjectionSystem.CISYS_ROMFS_DIR + "/pmc_overlay.txt");
	}

	private void writePMCOverlayId(int overlayId) {
		FSFile file = getOverlayIdTxt();
		if (file.isDirectory()) {
			file.delete();
		}
		file.setBytes(String.valueOf(overlayId).getBytes(StandardCharsets.US_ASCII));
	}

	private int getPMCOverlayId() {
		try {
			FSFile file = getOverlayIdTxt();
			if (file != null && file.isFile()) {
				String data = new String(file.getBytes(), StandardCharsets.US_ASCII);
				return Integer.parseInt(data);
			}
		} catch (Exception ex) {

		}
		return -1;
	}
	
	public String getPMCVersionString() {
		return getPMCVersionString(getPMCRPM());
	}
	
	public String getPMCVersionString(RPM pmcRpm) {
		if (pmcRpm != null && pmcRpm.metaData != null) {
			RPMMetaData.RPMMetaValue mv = pmcRpm.metaData.findValue(PMC_VERSION_META);
			if (mv != null) {
				return mv.stringValue();
			}
		}
		return null;
	}
	
	public boolean checkVersionOver(String version, String check) {
		if (version == null) {
			return check == null;
		}
		return version.compareTo(check) >= 0;
	}
	
	public boolean isUniversalInjectionCompatible() {
		ensureCIS();
		if (getPMCRPM() == null) {
			return true;
		}
		return checkVersionOver(getPMCVersionString(), PMC_UNIVERSAL_VERSION);
	}
	
	public boolean isPMCRPMUniversal(RPM rpm) {
		return checkVersionOver(getPMCVersionString(rpm), PMC_UNIVERSAL_VERSION);
	}
	
	public boolean checkMinPMCVersion(RPM rpm) {
		return checkVersionOver(getPMCVersionString(rpm), getMinPMCVersion());
	}
	
	public String getMinPMCVersion() {
		return PMC_UNIVERSAL_VERSION;
	}
	
	public boolean isGameMatched(RPM rpm) {
		if (rpm.metaData != null) {
			RPMMetaData.RPMMetaValue mv = rpm.metaData.findValue(PMC_GAME_ID_META);
			if (mv != null) {
				return mv.stringValue().equals(fs.getGameInfo().getSubGame().name());
			}
		}
		return false;
	}
	
	private RPM getPMCRPM() {
		OvlCodeEntry entry = cis.getPSys().getPatchByName(PMC_RPM_UID);
		if (entry != null) {
			return entry.getFullRPM();
		}
		return null;
	}
}
