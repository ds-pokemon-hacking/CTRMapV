package ctrmap.formats.pokemon.gen5.rail;

import ctrmap.formats.pokemon.WorldObject;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.iface.DataOutputEx;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import ctrmap.formats.ntr.common.FXIO;
import ctrmap.missioncontrol_ntr.field.VFieldConstants;

public class RailPoint extends RailEntry implements WorldObject {

	public static final int BYTES = 0x70;

	public final LineAttachment[] attachments = new LineAttachment[4];

	public Vec3f position;

	public int cameraId;

	public String name;

	public RailPoint(DataInputEx in, RailData rails) throws IOException {
		super(rails);
		for (int i = 0; i < 4; i++) {
			int line = in.readInt();
			attachments[i] = new LineAttachment(rails);
			attachments[i].line = line;
		}
		for (int i = 0; i < 4; i++) {
			int dir = in.readInt();
			attachments[i].direction = dir;
		}
		for (int i = 0; i < 4; i++) {
			float width = in.readInt();
			attachments[i].width = width;
		}
		position = FXIO.readVecFX32(in);
		cameraId = in.readInt();
		name = in.readPaddedString(0x30);
	}

	public RailCamera getCamera() {
		return rails.cameras.get(cameraId);
	}

	public LineAttachment getAttachmentByLine(RailLine line) {
		for (LineAttachment a : attachments) {
			if (a != null && a.getLine() == line) {
				return a;
			}
		}
		return null;
	}

	@Override
	public Vec3f getWPos() {
		return position;
	}

	@Override
	public void setWPos(Vec3f vec) {
		position.x = vec.x;
		position.z = vec.z;
	}

	@Override
	public Vec3f getWDim() {
		return new Vec3f(VFieldConstants.TILE_REAL_SIZE);
	}

	@Override
	public Vec3f getMinVector() {
		return getWDim().mul(-0.5f);
	}

	@Override
	public float getRotationY() {
		return 0f;
	}

	@Override
	public void write(DataOutputEx out) throws IOException {
		for (LineAttachment a : attachments){
			out.writeInt(a.line);
		}
		for (LineAttachment a : attachments){
			out.writeInt(a.direction);
		}
		for (LineAttachment a : attachments){
			out.writeInt((int)a.width);
		}
		FXIO.writeVecFX32(out, position);
		out.writeInt(cameraId);
		out.writePaddedString(name, 0x30);
	}

	public static class LineAttachment extends RailEntry {

		public int direction;
		public float width;
		private int line;

		public LineAttachment(RailData rails) {
			super(rails);
		}

		public RailLine getLine() {
			if (line == 0xFFFF){
				return null;
			}
			return rails.lines.get(line);
		}

		@Override
		public void write(DataOutputEx out) throws IOException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}
}
