package ctrmap.formats.pokemon.gen5.terrain;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.io.base.impl.ext.data.DataInStream;

public class VMapTerrain {
	
	public Tile[][] tiles;
	public List<CornerHeight> corners;
	
	public VMapTerrain(byte[] data) {
		try {
			DataInStream in = new DataInStream(data);
			
			int w = in.readUnsignedShort();
			int h = in.readUnsignedShort();
			tiles = new Tile[w][h];
			
			int maxCornerIndex = -1;
			
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					Tile t = new Tile(in);
					tiles[x][y] = t;
					if (t.heightType == HeightType.CORNER) {
						maxCornerIndex = Math.max(maxCornerIndex, t.height);
					}
				}
			}
			
			corners = new ArrayList<>(maxCornerIndex + 1);
			for (int i = 0; i <= maxCornerIndex; i++) {
				corners.add(new CornerHeight(in));
			}
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(VMapTerrain.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static class Tile {
		public HeightType heightType;
		public int slope;
		public int height;

		public TileType type;
		
		public Tile(DataInput in) throws IOException {
			int first = in.readUnsignedShort();
			heightType = HeightType.values()[first & 3];
			slope = (first >>> 2);
			height = in.readUnsignedShort();
			type = new TileType(in.readInt());
		}
	}
	
	public static class TileType {
		public final int tileClass;
		public final int flags;
		
		public TileType(int value) {
			tileClass = (value & 0xFFFF);
			flags = value >>> 16;
		}
		
		public int getBits() {
			return tileClass | (flags << 16);
		}
	}
	
	public static class CornerHeight {
		public int slope1;
		public int slope1Low;
		public int slope2;
		public int height1;
		public int height2;
		
		public CornerHeight(DataInput in) throws IOException {
			slope1 = in.readUnsignedShort();
			slope1Low = slope1 & 0x3;
			slope1 >>>= 2;
			slope2 = in.readUnsignedShort();
			height1 = in.readUnsignedShort();
			height2 = in.readUnsignedShort();
		}
	}
	
	public static enum HeightType {
		DEFAULT,
		PLAIN,
		CORNER
	}
}
