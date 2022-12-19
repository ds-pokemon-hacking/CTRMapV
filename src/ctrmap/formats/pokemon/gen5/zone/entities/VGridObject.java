package ctrmap.formats.pokemon.gen5.zone.entities;

import ctrmap.formats.pokemon.WorldObject;
import xstandard.math.vec.Vec3f;
import java.awt.Dimension;
import java.awt.Point;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;

public interface VGridObject extends WorldObject {

	public Point getGPos();

	public void setGPos(Point p);

	public boolean getIsDimensionsCentered();

	public Dimension getGDimensions();

	public void setGDimensions(Dimension dim);

	public float getDimHeight();

	public float getAltitude();

	public default void setAltitude(float val) {

	}

	@Override
	public default Vec3f getWPos() {
		return gPosToWPos(this);
	}

	@Override
	public default Vec3f getWDim() {
		return gDimToWDim(this);
	}

	@Override
	public default void setWPos(Vec3f vec) {
		setGPos(wPosToGPos(vec));
		setAltitude(vec.y);
	}

	@Override
	public default void setWDim(Vec3f vec) {
		setGDimensions(wDimToGDim(vec));
	}

	@Override
	public default Vec3f getMinVector() {
		return getIsCenteredMinVec(this);
	}

	public static Vec3f getIsCenteredMinVec(VGridObject obj) {
		if (!obj.getIsDimensionsCentered()) {
			return new Vec3f();
		} else {
			Vec3f wd = obj.getWDim();
			return new Vec3f(-wd.x / 2f, -wd.y / 2f, -wd.z / 2f);
		}
	}

	public static Vec3f gPosToWPos(VGridObject obj) {
		Point gpos = obj.getGPos();
		if (obj.getIsDimensionsCentered()) {
			return new Vec3f(tileToWorldCentered(gpos.x), obj.getAltitude(), tileToWorldCentered(gpos.y));
		} else {
			return new Vec3f(tileToWorldNonCentered(gpos.x), obj.getAltitude(), tileToWorldNonCentered(gpos.y));
		}
	}

	public static Point wPosToGPos(Vec3f vec) {
		return new Point(worldToTileClosest(vec.x), worldToTileClosest(vec.z));
	}

	public static Point wPosToGPosNonRounded(Vec3f vec) {
		return new Point(worldToTile(vec.x), worldToTile(vec.z));
	}

	public static Vec3f gDimToWDim(VGridObject obj) {
		Dimension gDim = obj.getGDimensions();
		return new Vec3f(tileToWorldNonCentered(gDim.width), obj.getDimHeight(), tileToWorldNonCentered(gDim.height));
	}

	public static Dimension wDimToGDim(Vec3f vec) {
		Dimension dim = new Dimension(worldToTileClosest(vec.x), worldToTileClosest(vec.z));
		return dim;
	}

	public static int worldToTile(float world) {
		return (int) Math.floor(world / VFieldConstants.TILE_REAL_SIZE);
	}

	public static int worldToTileClosest(float world) {
		return (int) Math.round(world / VFieldConstants.TILE_REAL_SIZE);
	}

	public static float tileToWorldCentered(int tile) {
		return (tile + 0.5f) * VFieldConstants.TILE_REAL_SIZE;
	}

	public static float tileToWorldNonCentered(int tile) {
		return tile * VFieldConstants.TILE_REAL_SIZE;
	}
}
