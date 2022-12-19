package ctrmap.formats.pokemon.gen5.zone.entities;

import xstandard.io.base.iface.DataOutputEx;
import xstandard.math.vec.Vec3f;
import java.awt.Dimension;
import java.awt.Point;
import java.io.DataInput;
import java.io.IOException;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;

public class VWarp implements VGridObject {

	public static final int BYTES = 0x14;

	public int targetZone;
	public int targetWarpId;
	public int unk12;
	public ContactDirection faceDirection = ContactDirection.SOUTH;

	public int transitionType;

	public boolean isRail;

	public int x;
	public int y;
	public int z;

	public int railLineNo;
	public int railPosFront;
	public int railPosSide;

	public int w = 1;
	public int h = 1;

	public enum ContactDirection {
		ANY,
		NORTH,
		SOUTH,
		WEST,
		EAST
	}

	public VWarp() {
		targetZone = 0;
		targetWarpId = 0;
		unk12 = 0;
		isRail = false;
		faceDirection = ContactDirection.SOUTH;
		transitionType = 3;
		x = 0;
		z = 0;
		w = 1;
		h = 1;
	}

	public VWarp(DataInput dis) throws IOException {
		try {
			targetZone = dis.readUnsignedShort();
			targetWarpId = dis.readUnsignedShort();
			faceDirection = ContactDirection.values()[dis.readUnsignedByte()];
			transitionType = dis.readUnsignedByte();
			isRail = dis.readUnsignedShort() == 1;
			if (isRail) {
				railLineNo = dis.readUnsignedShort();
				railPosFront = dis.readUnsignedShort();
				railPosSide = dis.readShort();
			} else {
				x = dis.readShort();
				y = dis.readShort();
				z = dis.readShort();
			}
			w = dis.readShort();
			h = dis.readShort();
			unk12 = dis.readUnsignedShort();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void write(DataOutputEx dos) throws IOException {
		dos.writeShort(targetZone);
		dos.writeShort(targetWarpId);
		dos.writeEnum(faceDirection);
		dos.write(transitionType);
		dos.writeShort(isRail ? 1 : 0);
		if (isRail) {
			dos.writeShort(railLineNo);
			dos.writeShort(railPosFront);
			dos.writeShort(railPosSide);
		} else {
			dos.writeShort(x);
			dos.writeShort(y);
			dos.writeShort(z);
		}
		dos.writeShort(w);
		dos.writeShort(h);
		dos.writeShort(unk12);
	}

	@Override
	public Point getGPos() {
		return new Point(VGridObject.worldToTile(x), VGridObject.worldToTile(z));
	}

	@Override
	public void setGPos(Point p) {
		x = (int) VGridObject.tileToWorldCentered(p.x);
		z = (int) VGridObject.tileToWorldCentered(p.y);
	}

	@Override
	public Vec3f getWPos() {
		return new Vec3f(x - VFieldConstants.TILE_REAL_SIZE_HALF, y, z - VFieldConstants.TILE_REAL_SIZE_HALF);
	}

	@Override
	public Dimension getGDimensions() {
		return new Dimension(w, h);
	}

	@Override
	public void setGDimensions(Dimension dim) {
		w = dim.width;
		h = dim.height;
	}

	@Override
	public boolean getIsDimensionsCentered() {
		return false;
	}

	@Override
	public float getRotationY() {
		//There is the contact direction property, but that does not affect the dimensions of the model
		return 0;
	}

	@Override
	public float getAltitude() {
		return y;
	}

	@Override
	public void setAltitude(float alt) {
		this.y = (int) alt;
	}

	@Override
	public float getDimHeight() {
		return VFieldConstants.TILE_REAL_SIZE * 2;
	}

}
