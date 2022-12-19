package ctrmap.missioncontrol_ntr.field.mmodel;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PokemonFieldMModelLUT {

	public List<PokemonFieldMModelEntry> entries = new ArrayList<>();

	public PokemonFieldMModelLUT(FSFile fsf) {
		try (DataInStream in = fsf.getDataInputStream()) {
			int count = in.getLength() / PokemonFieldMModelEntry.BYTES;

			for (int i = 0; i < count; i++) {
				entries.add(new PokemonFieldMModelEntry(in));
			}
		} catch (IOException ex) {
			Logger.getLogger(PokemonFieldMModelLUT.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public PokemonFieldMModelEntry find(int monsno, PokemonFieldMModelSex sex, int forme) {
		for (PokemonFieldMModelEntry e : entries) {
			if (e.species == monsno) {
				if (sex == PokemonFieldMModelSex.DONT_CARE || sex == null || e.sex == PokemonFieldMModelSex.DONT_CARE || sex == e.sex) {
					if (forme == -1 || forme == e.forme) {
						return e;
					}
				}
			}
		}
		return null;
	}
	
	public static class PokemonFieldMModelEntry {

		public static final int BYTES = 8;

		public int species;
		public PokemonFieldMModelSex sex;
		public int forme;
		public int objCode;
		
		public PokemonFieldMModelEntry(DataInput in) throws IOException {
			species = in.readUnsignedShort();
			sex = PokemonFieldMModelSex.VALUES[in.readUnsignedShort()];
			forme = in.readUnsignedShort();
			objCode = in.readUnsignedShort();
		}
	}

	public static enum PokemonFieldMModelSex {
		MALE,
		FEMALE,
		DONT_CARE;
		
		public static final PokemonFieldMModelSex[] VALUES = values();
	}
}
