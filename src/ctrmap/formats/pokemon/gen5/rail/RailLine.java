package ctrmap.formats.pokemon.gen5.rail;

import ctrmap.missioncontrol_ntr.field.rail.VRailMath;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.iface.DataOutputEx;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;

public class RailLine extends RailEntry {

	public static final int BYTES = 0x48;

	public String name;

	private int point1;
	private int point2;

	public int angle;
	private int curveId;
	private int cameraId;

	public int lineTileLength;

	public RailLine(DataInputEx in, RailData rails) throws IOException {
		super(rails);
		point1 = in.readInt();
		point2 = in.readInt();
		angle = in.readInt();
		curveId = in.readInt();
		cameraId = in.readInt();
		lineTileLength = in.readInt();

		name = in.readPaddedString(0x30).trim();
	}

	public Vec3f getAbsPositionOnRail(float x, float y, boolean centered) {
		RailCurve curve = getCurve();

		RailTilemaps.Block tm = getTilemapBlock();
		if (!centered) {
			x -= tm.tiles.getWidth() / 2f;
		}
		RailPoint p1 = getP1();
		RailPoint p2 = getP2();

		float lineWidthP1 = p1.getAttachmentByLine(this).width;
		float lineWidthP2 = p2.getAttachmentByLine(this).width;

		float yWeight = y / lineTileLength;

		float lineWidthLerp = lineWidthP1 + (lineWidthP2 - lineWidthP1) * yWeight + VFieldConstants.TILE_REAL_SIZE_HALF;

		float xPos = x * rails.info.tileDim * VFieldConstants.TILE_REAL_SIZE;

		Vec3f pos;
		switch (curve.curveType) {
			case SLERP_XZ:
				pos = VRailMath.getCurvePositionSlerpXZ(p1.position, p2.position, curve.position, yWeight, xPos);
				break;
			case SLERP_XYZ:
				pos = VRailMath.getCurvePositionSlerpXYZ(p1.position, p2.position, curve.position, yWeight, xPos);
				break;
			default:
				pos = VRailMath.getNoCurvePosition(p1.position, p2.position, yWeight, xPos);
				break;
		}

		return pos;
	}

	public RailPoint getP1() {
		return rails.points.get(point1);
	}

	public RailPoint getP2() {
		return rails.points.get(point2);
	}

	public RailCurve getCurve() {
		return rails.curves.get(curveId);
	}

	public RailCamera getCamera() {
		return rails.cameras.get(cameraId);
	}

	public RailTilemaps.Block getTilemapBlock() {
		return rails.tilemaps.blocks.get(rails.lines.indexOf(this));
	}

	@Override
	public void write(DataOutputEx out) throws IOException {
		out.writeInts(point1, point2, angle, curveId, cameraId, lineTileLength);
		out.writePaddedString(name, 0x30);
	}
}
