package ctrmap.formats.pokemon.gen5.buildings;

import ctrmap.formats.ntr.nitroreader.nsbca.NSBCA;
import ctrmap.formats.ntr.nitroreader.nsbta.NSBTA;
import ctrmap.formats.ntr.nitroreader.nsbtp.NSBTP;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.PointerTable;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.util.StringIO;
import xstandard.util.ArraysEx;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class AreaBuildingResource extends G3DResource {

	private List<ABResourceListener> listeners = new ArrayList<>();

	public int uid;
	public int type;

	public short doorUID = -1;
	public short doorX;
	public short doorY;
	public short doorZ;

	public short unk1;

	public ABAnimCntType anmCntType = ABAnimCntType.NON_ANIMATED;
	public int unk2;
	public int anmSetEntryCount;
	public int animationCount;

	private byte[][] animations = new byte[4][];
	private AbstractAnimation[] convAnimations = new AbstractAnimation[4];

	public AreaBuildingResource(int uid) {
		this.uid = uid;
	}
	
	public AreaBuildingResource(byte[] bytes) {
		try {
			DataIOStream dis = new DataIOStream(bytes);

			uid = dis.readShort();
			type = dis.readUnsignedShort();
			doorUID = dis.readShort();
			doorX = dis.readShort();
			doorY = dis.readShort();
			doorZ = dis.readShort();

			unk1 = dis.readShort();
			dis.readShort(); //padding

			dis.setBaseHere();

			anmCntType = ABAnimCntType.VALUES[dis.read()];
			unk2 = dis.read();

			anmSetEntryCount = dis.readByte();

			animationCount = dis.readByte();

			for (int i = 0; i < 4; i++) {
				int off = dis.readInt();

				if (off != -1) {
					dis.checkpoint();
					dis.seek(off + 8);

					int length = dis.readInt();
					//Need to retrieve length because NitroReader is not yet updated to NSIO
					dis.seek(off);
					byte[] anime = new byte[length];
					dis.read(anime);

					animations[i] = anime;

					dis.resetCheckpoint();
				} else {
					animations[i] = null;
				}
			}

			dis.resetBase();

			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(AreaBuildingResource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void addListener(ABResourceListener l) {
		ArraysEx.addIfNotNullOrContains(listeners, l);
	}
	
	public void removeListener(ABResourceListener l) {
		listeners.remove(l);
	}
	
	public void condenseAnimations() {
		for (int outIndex = 0; outIndex < 4; outIndex++) {
			if (convAnimations[outIndex] == null) {
				for (int inIndex = outIndex + 1; inIndex < 4; inIndex++) {
					if (convAnimations[inIndex] != null) {
						convAnimations[outIndex] = convAnimations[inIndex];
						animations[outIndex] = animations[inIndex];
						convAnimations[inIndex] = null;
						animations[inIndex] = null;
						break;
					}
				}
			}
		}
	}
	
	public void spreadAnimations2x2() {
		condenseAnimations();
		if (getPresentAnimationCount() == 2) {
			convAnimations[2] = convAnimations[1];
			convAnimations[1] = null;
		}
	}
	
	private List<ABResourceListener> getListenersStatic() {
		return new ArrayList<>(listeners);
	}
	
	public void sendDiscardToListeners(int replacementUID) {
		for (ABResourceListener l : getListenersStatic()) {
			l.onUIDChanged(replacementUID);
		}
	}

	public void setDoorUID(int doorUID) {
		if (this.doorUID != doorUID) {
			this.doorUID = (short) doorUID;
			for (ABResourceListener l : getListenersStatic()) {
				l.onDoorUIDChanged(doorUID);
			}
		}
	}
	
	public void setUID(int uid) {
		if (this.uid != uid) {
			this.uid = uid;
			for (ABResourceListener l : getListenersStatic()) {
				l.onUIDChanged(uid);
			}
		}
	}

	public void setAnmCntType(ABAnimCntType type) {
		if (this.anmCntType != type) {
			this.anmCntType = type;
			for (ABResourceListener l : getListenersStatic()) {
				l.onAnmCntTypeChanged();
			}
		}
	}
	
	public int getAnimationCount() {
		return animations.length;
	}

	public int getPresentAnimationCount() {
		for (int i = 3; i >= 0; i--) {
			if (animations[i] != null) {
				return i + 1;
			}
		}
		return 0;
	}

	public int getPresentAnimationCountInSet(int setNo) {
		int count = 0;
		for (int i = 0, anmIdx = setNo * anmSetEntryCount; i < anmSetEntryCount; i++, anmIdx++) {
			if (animations[i] != null) {
				count = i + 1;
			}
		}
		return count;
	}

	public int getAnimationSetCount() {
		if (anmSetEntryCount == 0 || getPresentAnimationCount() == 0) {
			return 1;
		}
		return (int) Math.ceil(getPresentAnimationCount() / (double) anmSetEntryCount);
	}

	public AbstractAnimation getConvAnm(int index) {
		if (index >= 0 && index < 4) {
			return convAnimations[index];
		}
		return null;
	}
	
	public void setAnm(int index, AbstractAnimation anm, byte[] anmRaw) {
		if (index >= 0 && index < 4) {
			convAnimations[index] = anm;
			animations[index] = anmRaw;
			
			for (ABResourceListener l : listeners) {
				l.onAnmCntTypeChanged();
			}
		}
	}

	public byte[] getBytes() {
		try {
			DataIOStream out = new DataIOStream();

			out.writeShorts(uid, type, doorUID, doorX, doorY, doorZ, unk1, 0);

			out.setBaseHere();

			animationCount = animations.length;

			out.write(anmCntType.ordinal());
			out.write(unk2);
			out.write(anmSetEntryCount);
			out.write(animationCount);

			List<TemporaryOffset> anmPtrTable = PointerTable.allocatePointerTable(4, out, 0, false);

			for (int i = 0; i < 4; i++) {
				if (animations[i] != null) {
					anmPtrTable.get(i).setHere();
					out.write(animations[i]);
				} else {
					anmPtrTable.get(i).set(0xFFFFFFFF);
				}
			}

			out.resetBase();

			out.close();

			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(AreaBuildingResource.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public String getBmName() {
		if (models.isEmpty()) {
			return uid + ": NULL";
		} else {
			return uid + ": " + models.get(0).name;
		}
	}

	public void convertNitroResources(G3DResource model) {
		merge(model);
		int anmIdx = 0;
		for (byte[] anmData : animations) {
			if (anmData != null && anmData.length > 4) {
				G3DResource anmRes = null;

				switch (StringIO.getMagic(anmData, 0, 4)) {
					case NSBTA.MAGIC:
						anmRes = new NSBTA(anmData).toGeneric();
						break;
					case NSBTP.MAGIC:
						anmRes = new NSBTP(anmData).toGeneric();
						break;
					case NSBCA.MAGIC:
						if (!models.isEmpty()) {
							Model mdl = models.get(0);
							NSBCA bca = new NSBCA(anmData);
							if (bca.acceptsModel(mdl)) {
								anmRes = bca.toGeneric(mdl.skeleton);
							}
						}
						break;
				}

				AbstractAnimation resultAnm = null;

				if (anmRes != null) {
					List<AbstractAnimation> sourceAnimations = anmRes.getAnimations();
					if (!sourceAnimations.isEmpty()) {
						resultAnm = sourceAnimations.get(0);
					}
				}

				convAnimations[anmIdx] = resultAnm;
			} else {
				convAnimations[anmIdx] = null;
			}
			anmIdx++;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + this.uid;
		hash = 29 * hash + this.type;
		hash = 29 * hash + this.doorUID;
		hash = 29 * hash + this.doorX;
		hash = 29 * hash + this.doorY;
		hash = 29 * hash + this.doorZ;
		hash = 29 * hash + this.unk1;
		hash = 29 * hash + Objects.hashCode(this.anmCntType);
		hash = 29 * hash + this.unk2;
		hash = 29 * hash + this.anmSetEntryCount;
		hash = 29 * hash + this.animationCount;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof AreaBuildingResource)) {
			return false;
		}
		final AreaBuildingResource other = (AreaBuildingResource) obj;
		
		return uid == other.uid && type == other.type && doorUID == other.doorUID && doorX == other.doorX && doorY == other.doorY && doorZ == other.doorZ 
			&& unk1 == other.unk1 && unk2 == other.unk2 && anmSetEntryCount == other.anmSetEntryCount && animationCount == other.animationCount && anmCntType == other.anmCntType;
	}

	public static enum ABAnimCntType {
		NON_ANIMATED,
		AMBIENT_GENERIC,
		DYNAMIC,
		AMBIENT_RTC;

		public static final ABAnimCntType[] VALUES = values();
	}

	public static interface ABResourceListener {

		public void onUIDChanged(int newUID);
		public void onDoorUIDChanged(int newUID);
		public void onAnmCntTypeChanged();
	}
}
