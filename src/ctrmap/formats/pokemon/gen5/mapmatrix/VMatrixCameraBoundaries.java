package ctrmap.formats.pokemon.gen5.mapmatrix;

import ctrmap.formats.ntr.common.FXIO;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VMatrixCameraBoundaries {

	public List<VMatrixCameraBoundary> entries = new ArrayList<>();

	public VMatrixCameraBoundaries(FSFile fsf) {
		try {
			DataIOStream io = fsf.getDataIOStream();

			int count = io.readInt();

			for (int i = 0; i < count; i++) {
				VMatrixCameraBoundaryType type = VMatrixCameraBoundaryType.VALUES[io.readInt()];
				
				if (type == VMatrixCameraBoundaryType.CIRCLE_RESTRICT) {
					VMatrixCameraBoundaryCircle circ = new VMatrixCameraBoundaryCircle();
					circ.type = type;
					circ.target = VMatrixCameraBoundaryTarget.VALUES[io.readInt()];
					circ.x = FXIO.readFX32(io);
					circ.z = FXIO.readFX32(io);
					circ.distMax = FXIO.readFX32(io);
					circ.angleMin = FXIO.readAngleDeg16Unsigned(io);
					circ.angleMax = FXIO.readAngleDeg16Unsigned(io);
				}
				else {
					VMatrixCameraBoundaryRect rect = new VMatrixCameraBoundaryRect();
					rect.type = type;
					rect.target = VMatrixCameraBoundaryTarget.VALUES[io.readInt()];
					rect.west = io.readInt();
					rect.east = io.readInt();
					rect.north = io.readInt();
					rect.south = io.readInt();
					entries.add(rect);
				}
			}

			io.close();
		} catch (IOException ex) {
			Logger.getLogger(VMatrixCameraBoundaries.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static class VMatrixCameraBoundary {

		public VMatrixCameraBoundaryType type;
		public VMatrixCameraBoundaryTarget target;
	}

	public static class VMatrixCameraBoundaryRect extends VMatrixCameraBoundary {

		public int west;
		public int east;
		public int north;
		public int south;
	}
	
	public static class VMatrixCameraBoundaryCircle extends VMatrixCameraBoundary {

		public float x;
		public float z;
		
		public float distMax;
		
		public float angleMin;
		public float angleMax;
	}

	public static enum VMatrixCameraBoundaryType {
		RECT_NULL,
		RECT_RESTRICT,
		CIRCLE_RESTRICT,
		RECT_REPEAL;
		
		public static final VMatrixCameraBoundaryType[] VALUES = values();
	}
	
	public static enum VMatrixCameraBoundaryTarget {
		LOOKAT_TGT,
		LOOKAT_CAM_POS;
		
		public static final VMatrixCameraBoundaryTarget[] VALUES = values();
	}
}
