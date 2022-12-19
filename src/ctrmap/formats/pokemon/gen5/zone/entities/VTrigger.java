package ctrmap.formats.pokemon.gen5.zone.entities;

import ctrmap.formats.pokemon.IScriptObject;
import xstandard.math.vec.Vec3f;
import java.awt.Dimension;
import java.awt.Point;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;

public class VTrigger implements VGridObject, IScriptObject {

	public static final int BYTES = 0x16;

	public int script = 0;
	public int wkId = 0;
	public int wkRefVal = 0;
	public Type type = Type.STANDARD;
	
	public boolean isRail = false;
	
	public int gridX = 0;
	public int worldY;
	public int gridZ = 0;
	
	public int railLineNo;
	public int railPosFront;
	public int railPosSide;
	
	public int w = 2;
	public int h = 1;
	public int unk0x12 = 0;
	
	public static enum Type {
		STANDARD,
		DIR_UP,
		DIR_DOWN,
		DIR_LEFT,
		DIR_RIGHT,
		DISABLED,
		COLLIDING,
		TYPE_7
	}

	public VTrigger() {
		script = -1;
	}

	public VTrigger(DataInput dis) throws IOException {
		script = dis.readUnsignedShort();
		wkRefVal = dis.readUnsignedShort();
		wkId = dis.readUnsignedShort();
		type = Type.values()[dis.readUnsignedShort()];
		isRail = dis.readShort() != 0;
		if (isRail) {
			railLineNo = dis.readUnsignedShort();
			railPosFront = dis.readUnsignedShort();
			railPosSide = dis.readShort();
			w = dis.readUnsignedShort();
			h = dis.readUnsignedShort();
		}
		else {
			gridX = dis.readShort();
			gridZ = dis.readShort();
			w = dis.readUnsignedShort();
			h = dis.readUnsignedShort();
			worldY = dis.readShort();
		}
		unk0x12 = dis.readShort();
	}

	public void write(DataOutput dos) throws IOException {
		dos.writeShort(script);
		dos.writeShort(wkRefVal);
		dos.writeShort(wkId);
		dos.writeShort(type.ordinal());
		dos.writeShort(isRail ? 1 : 0);
		if (isRail) {
			dos.writeShort(railLineNo);
			dos.writeShort(railPosFront);
			dos.writeShort(railPosSide);
			dos.writeShort(w);
			dos.writeShort(h);
		}
		else {
			dos.writeShort(gridX);
			dos.writeShort(gridZ);
			dos.writeShort(w);
			dos.writeShort(h);
			dos.writeShort(worldY);
		}
		dos.writeShort(unk0x12);
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof VTrigger) {
			VTrigger t = (VTrigger) o;
			return 
				t.script == script && t.wkId == wkId && t.wkRefVal == wkRefVal && t.isRail == isRail
				&& t.gridX == gridX && t.gridZ == gridZ && t.w == w && t.h == h && t.type == type && t.unk0x12 == unk0x12
				&& t.railLineNo == railLineNo && t.railPosFront == railPosFront && t.railPosSide == railPosSide;
		}
		return false;
	}

	@Override
	public Point getGPos() {
		return new Point(gridX, gridZ);
	}

	@Override
	public void setGPos(Point p) {
		gridX = p.x;
		gridZ = p.y;
	}

	@Override
	public Dimension getGDimensions() {
		return new Dimension(w, h);
	}

	@Override
	public boolean getIsDimensionsCentered() {
		return false;
	}

	@Override
	public float getRotationY() {
		return 0;
	}

	@Override
	public float getAltitude() {
		return worldY;
	}
	
	@Override
	public void setAltitude(float alt) {
		this.worldY = Math.round(alt / 8f) * 8;
	}

	@Override
	public float getDimHeight() {
		return VFieldConstants.TILE_REAL_SIZE * 2;
	}

	@Override
	public void setGDimensions(Dimension dim) {
		w = dim.width;
		h = dim.height;
	}

	@Override
	public int getSCRID() {
		return script;
	}

	@Override
	public void setSCRID(int SCRID) {
		script = SCRID;
	}

	@Override
	public int getObjectTypeID() {
		return VZoneEntities.VZE_SCROBJ_TYPEID_TRIGGER;
	}

}
