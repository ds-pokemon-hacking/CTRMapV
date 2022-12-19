package ctrmap.formats.pokemon.gen5.npcreg;

import xstandard.fs.FSFile;
import xstandard.gui.DialogUtils;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import xstandard.math.BitMath;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class VNPCRegistry {
	
	public static int[] SPRITE_ATLAS_SIZES_X = new int[64];
	public static int[] SPRITE_ATLAS_SIZES_Y = new int[64];
	
	//method to generate atlas size LUTs. The game uses a table like these where the lowest 3 bits are Y and highest 3 are X.
	//no reason not to do it procedurally here however
	static {
		for (int i = 0; i < 8; i++) {
			int pow = (1 << 3);
			for (int j = 0; j < 8; j++) {
				SPRITE_ATLAS_SIZES_Y[i * 8 + j] = pow;
				SPRITE_ATLAS_SIZES_X[j * 8 + i] = pow;
				pow <<= 1;
			}
		}
	}

	public List<Entry> entries = new ArrayList<>();
	public boolean modified = false;
	private FSFile f;

	public VNPCRegistry(FSFile fsf) {
		f = fsf;
		try {
			initFromStream(fsf.getInputStream());
		} catch (IOException ex) {
			Logger.getLogger(VNPCRegistry.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void initFromStream(ReadableStream in) throws IOException {
		DataInStream dis = new DataInStream(in);
		int count = dis.readInt();
		for (int i = 0; i < count; i++) {
			entries.add(new Entry(dis));
		}
		dis.close();
	}

	public boolean store(boolean dialog) {
		if (!modified) {
			return true;
		}
		if (dialog) {
			int result = DialogUtils.showSaveConfirmationDialog("NPC registry");
			switch (result) {
				case JOptionPane.YES_OPTION:
					break;
				case JOptionPane.NO_OPTION:
					modified = false;
					return true;
				case JOptionPane.CANCEL_OPTION:
					return false;
			}
		}
		try {
			DataOutStream dos = f.getDataOutputStream();
			dos.writeInt(entries.size());
			for (Entry e : entries) {
				e.write(dos);
			}
			dos.close();
		} catch (IOException ex) {
			Logger.getLogger(VNPCRegistry.class.getName()).log(Level.SEVERE, null, ex);
		}
		modified = false;
		return true;
	}

	public static class Entry {

		public int uid;
		public EntityType entityType;
		public int sceneNodeType;
		public boolean shadow;
		public int footprints;
		public boolean reflections;
		public int billboardSize;
		public int spriteAtlasSize;
		public int spriteControllerType;
		public NPCGender gender;
		public int width;
		public int height;
		public int wPosOffX;
		public int wPosOffY;
		public int wPosOffZ;
		public int[] resourceIndices = new int[5];
		public int padding;

		public Entry(DataInput dis) throws IOException {
			uid = dis.readUnsignedShort(); //0x0
			entityType = EntityType.VALUES[dis.readUnsignedByte()];
			sceneNodeType = dis.readUnsignedByte();
			shadow = dis.readBoolean();
			footprints = dis.readUnsignedByte();
			reflections = dis.readBoolean();
			billboardSize = dis.readUnsignedByte();
			spriteAtlasSize = dis.readUnsignedByte();
			spriteControllerType = dis.readUnsignedByte();
			gender = NPCGender.VALUES[dis.readUnsignedByte()];
			width = dis.readUnsignedByte();
			height = dis.readUnsignedByte();
			wPosOffX = dis.readByte();
			wPosOffY = dis.readByte();
			wPosOffZ = dis.readByte();
			for (int i = 0; i < resourceIndices.length; i++) {
				resourceIndices[i] = dis.readUnsignedShort();
			}
			padding = dis.readUnsignedShort();
		}
		
		public void setSpriteAtlasSize(int sizeX, int sizeY) {
			spriteAtlasSize = ((BitMath.bitLog2(sizeX) - 3) << 3) + (BitMath.bitLog2(sizeY) - 3);
		}
		
		public int getSpriteAtlasSizeX() {
			return (1 << (3 + (spriteAtlasSize >> 3)));
		}
		
		public int getSpriteAtlasSizeY() {
			return 1 << (3 + (spriteAtlasSize & 7));
		}

		public void write(DataOutput dos) throws IOException {
			dos.writeShort(uid);
			dos.write(entityType.ordinal());
			dos.write(sceneNodeType);
			dos.writeBoolean(shadow);
			dos.write(footprints);
			dos.writeBoolean(reflections);
			dos.write(billboardSize);
			dos.write(spriteAtlasSize);
			dos.write(spriteControllerType);
			dos.write(gender.ordinal());
			dos.write(width);
			dos.write(height);
			dos.write(wPosOffX);
			dos.write(wPosOffY);
			dos.write(wPosOffZ);
			for (int i : resourceIndices) {
				dos.writeShort(i);
			}
			dos.writeShort(padding);
		}
	}
	
	public enum EntityType {
		PLACEHOLDER,
		ACTOR,
		OBJECT;
		
		public static final EntityType[] VALUES = values();
	}

	public enum NPCGender {
		MALE,
		FEMALE,
		NONE;
		
		public static final NPCGender[] VALUES = values();
	}
}
