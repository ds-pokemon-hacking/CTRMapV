package ctrmap.formats.pokemon.gen5.zone.entities;

import ctrmap.formats.ntr.common.FXIO;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class VNPC {

	public static final int BYTES = 0x24;

	public int uid = 0; //all unsigned shorts
	public int objCode = 0;
	public int moveCode = 0;
	public int eventType = 0; //4 on items and 10 on double battle paired NPCs
	public int spawnFlag = 0;
	public int script = 0;
	public int faceDirection = 0;
	public final int[] params = new int[3];
	public int areaWidth = 1;
	public int areaHeight = 1;

	public boolean isPositionRail = false;
	
	//ZoneNPCPositionGrid
	public int gposX = 0;
	public int gposZ = 0;
	public float wposY;

	//ZoneNPCPositionRail
	public int railLineNo;
	public int railFrontPos;
	public int railSidePos;

	public VNPC(DataInput dis) throws IOException {
		uid = dis.readUnsignedShort();
		objCode = dis.readUnsignedShort();
		moveCode = dis.readUnsignedShort();
		eventType = dis.readUnsignedShort();
		spawnFlag = dis.readUnsignedShort();
		script = dis.readUnsignedShort();
		faceDirection = dis.readUnsignedShort();
		for (int i = 0; i < params.length; i++) {
			params[i] = dis.readUnsignedShort();
		}
		areaWidth = dis.readShort();
		areaHeight = dis.readShort();
		isPositionRail = dis.readInt() != 0;
		if (isPositionRail) {
			railLineNo = dis.readUnsignedShort();
			railFrontPos = dis.readUnsignedShort();
			railSidePos = dis.readShort();
			dis.readShort();
		} else {
			gposX = dis.readUnsignedShort();
			gposZ = dis.readUnsignedShort();
			wposY = FXIO.readFX32(dis);
		}
	}

	public VNPC(int uid, int mdlUID) {
		this.uid = uid;
		this.objCode = mdlUID;
	}

	public VNPC() {
	}

	public void write(DataOutput dos) throws IOException {
		dos.writeShort(uid);
		dos.writeShort(objCode);
		dos.writeShort(moveCode);
		dos.writeShort(eventType);
		dos.writeShort(spawnFlag);
		dos.writeShort(script);
		dos.writeShort(faceDirection);
		for (int p : params) {
			dos.writeShort(p);
		}
		dos.writeShort(areaWidth);
		dos.writeShort(areaHeight);
		dos.writeInt(isPositionRail ? 1 : 0);
		if (isPositionRail) {
			dos.writeShort(railLineNo);
			dos.writeShort(railFrontPos);
			dos.writeShort(railSidePos);
			dos.writeShort(0);
		} else {
			dos.writeShort(gposX);
			dos.writeShort(gposZ);
			FXIO.writeFX32(dos, wposY);
		}
	}

	@Override
	public boolean equals(Object o2) {
		if (o2 != null && o2 instanceof VNPC) {
			VNPC npc = (VNPC) o2;
			return npc.uid == uid && npc.objCode == objCode && npc.moveCode == moveCode && npc.eventType == eventType && npc.spawnFlag == spawnFlag && npc.script == script && npc.faceDirection == faceDirection && Arrays.equals(npc.params, params) && npc.isPositionRail == isPositionRail && npc.areaWidth == areaWidth && npc.areaHeight == areaHeight && npc.gposX == gposX && npc.gposZ == gposZ && npc.wposY == wposY && npc.railSidePos == railSidePos && npc.railFrontPos == railFrontPos && npc.railLineNo == railLineNo;
		}
		return false;
	}
}
