package ctrmap.missioncontrol_ntr.fs;

import ctrmap.formats.common.GameInfo;
import ctrmap.missioncontrol_ntr.NTRGameConstant;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NARCListingTable {

	public static final NTRGameConstant PTR_OFFSET = new NTRGameConstant(
		new NTRGameConstant.GameValuePair(GameInfo.SubGame.W2, 0x200563C)
	);

	public List<String> paths = new ArrayList<>();

	public NARCListingTable(NTRGameFS fs, GameInfo game) {
		int ptrOffset = PTR_OFFSET.get(game);
		if (ptrOffset != -1) {
			DataIOStream io = fs.getDecompressedArm9BinMaybeRO().getDataIOStream();
			io.setBase(fs.getARM9LoadAddr());
			try {
				io.seekAndSeek(ptrOffset);

				int max = io.getLength() - Integer.BYTES;
				int pos = io.getPosition();

				List<Integer> pointers = new ArrayList<>();

				while (pos <= max) {
					int ptr = io.readInt();
					if ((ptr & 0xFF000000) == 0x02000000) {
						pointers.add(ptr);
					} else {
						break;
					}
					pos += Integer.BYTES;
				}

				for (int p : pointers) {
					io.seek(p);
					paths.add(io.readString());
				}

				io.close();
			} catch (IOException ex) {
				Logger.getLogger(NARCListingTable.class.getName()).log(Level.SEVERE, null, ex);
			}
			io.resetBase();
		}
	}

	public String getPath(int ARCID) {
		if (ARCID < 0 || ARCID >= paths.size()) {
			throw new ArrayIndexOutOfBoundsException("ARCID out of range! " + ARCID);
		}
		return paths.get(ARCID);
	}
}
