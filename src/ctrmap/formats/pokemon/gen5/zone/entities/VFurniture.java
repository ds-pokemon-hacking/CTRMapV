package ctrmap.formats.pokemon.gen5.zone.entities;

import ctrmap.formats.pokemon.IScriptObject;
import java.awt.Dimension;
import java.awt.Point;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VFurniture implements VGridObject, IScriptObject {

	public static final int BYTES = 0x14;

	public int script;
	public int condition;
	public int interactibility;
	
	public boolean isPositionRail;
	
	public int gridX;
	public int gridZ;
	
	public int railLineNo;
	public int railFrontPos;
	public int railSidePos;
	
	public int y;

	public VFurniture(){
		
	}
	
	public VFurniture(DataInput dis) {
		try {
			script = dis.readUnsignedShort();
			condition = dis.readUnsignedShort();
			interactibility = dis.readUnsignedShort();
			isPositionRail = dis.readUnsignedShort() != 0;
			if (isPositionRail) {
				railLineNo = dis.readUnsignedShort();
				railFrontPos = dis.readUnsignedShort();
				railSidePos = dis.readShort();
				dis.readUnsignedShort();
			}
			else {
				gridX = dis.readInt();
				gridZ = dis.readInt();
			}
			y = dis.readInt();
		} catch (IOException ex) {
			Logger.getLogger(VFurniture.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void write(DataOutput dos) {
		try {
			dos.writeShort(script);
			dos.writeShort(condition);
			dos.writeShort(interactibility);
			dos.writeShort(isPositionRail ? 1 : 0);
			if (isPositionRail) {
				dos.writeShort(railLineNo);
				dos.writeShort(railFrontPos);
				dos.writeShort(railSidePos);
				dos.writeShort(0);
			}
			else {
				dos.writeInt(gridX);
				dos.writeInt(gridZ);
			}
			dos.writeInt(y);
		} catch (IOException ex) {
			Logger.getLogger(VFurniture.class.getName()).log(Level.SEVERE, null, ex);
		}
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
	public boolean getIsDimensionsCentered() {
		return false;
	}

	@Override
	public Dimension getGDimensions() {
		return new Dimension(1, 1);
	}

	@Override
	public float getRotationY() {
		return 0;
	}

	@Override
	public float getAltitude() {
		return y;
	}
	
	@Override
	public void setAltitude(float alt) {
		this.y = Math.round(alt / 8f) * 8;
	}

	@Override
	public float getDimHeight() {
		return 10f;//not yet implemented
	}

	@Override
	public void setGDimensions(Dimension dim) {
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
		return VZoneEntities.VZE_SCROBJ_TYPEID_FURNITURE;
	}
}
