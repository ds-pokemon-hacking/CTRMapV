package ctrmap.missioncontrol_ntr.field;

import xstandard.fs.FSFile;
import ctrmap.formats.common.GameInfo;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.formats.pokemon.gen5.camera.VCameraDataFile;
import ctrmap.formats.pokemon.gen5.zone.VZoneTable;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;

public class FieldCameraLoader {

	private NTRGameFS fs;

	private List<CameraInfo> cameraRegistry = new ArrayList<>();
	
	public final boolean EX_IS_LOADED_FROM_INJECTED;

	public FieldCameraLoader(VZoneTable zones, NTRGameFS fs, GameInfo game) {
		this.fs = fs;
		FSFile camIndex = fs.getDataFile("field_camera_data_index.tbl");
		EX_IS_LOADED_FROM_INJECTED = camIndex.exists();
		try {
			if (!EX_IS_LOADED_FROM_INJECTED) {
				DataIOStream io = fs.getDecompressedArm9BinMaybeRO().getDataIOStream();

				io.setBase(fs.getARM9LoadAddr());

				if (game.getSubGame() == GameInfo.SubGame.W2) {
					io.seek(0x2018E56);
					int count = io.read();

					io.seekAndSeek(0x2018E60);

					for (int i = 0; i < count; i++) {
						cameraRegistry.add(new CameraInfo(io));
					}
				}

				io.close();

				DataIOStream camIndexOut = camIndex.getDataIOStream();

				for (int i = 0; i < zones.getZoneCount(); i++) {
					camIndexOut.writeShort(getCameraIndexForZone(i));
				}

				camIndexOut.close();
			} else {
				DataIOStream io = camIndex.getDataIOStream();

				int count = io.getLength() / Short.BYTES;

				for (int i = 0; i < count; i++) {
					short value = io.readShort();
					if (value != -1) {
						CameraInfo ri = new CameraInfo(i, value);
						cameraRegistry.add(ri);
					}
				}

				io.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(FieldCameraLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public final int getCameraIndexForZone(int zoneId) {
		for (CameraInfo ri : cameraRegistry) {
			if (ri.zone == zoneId) {
				return ri.camera;
			}
		}
		return -1;
	}
	
	public VCameraDataFile loadNoGridCameraData(int index) {
		return new VCameraDataFile(fs.NARCGet(NARCRef.FIELD_CAMERA_AREA_NOGRID, index));
	}

	public VCameraDataFile loadCameraData(int zoneId) {
		int idx = getCameraIndexForZone(zoneId);

		if (idx != -1) {
			System.out.println("Found camera data for zone " + zoneId + "! Loading from " + idx);
			return new VCameraDataFile(fs.NARCGet(NARCRef.FIELD_CAMERA_AREA_GRID, idx));
		}
		return null;
	}

	public static class CameraInfo {

		public int zone;
		public int camera;

		public CameraInfo(DataInput in) throws IOException {
			int value = in.readUnsignedShort();
			zone = value & 0x7FF;
			camera = value >>> 11;
		}

		public CameraInfo(int zone, int camera) {
			this.zone = zone;
			this.camera = camera;
		}
	}
}
