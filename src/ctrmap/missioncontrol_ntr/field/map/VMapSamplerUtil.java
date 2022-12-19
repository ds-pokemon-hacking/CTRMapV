package ctrmap.missioncontrol_ntr.field.map;

import ctrmap.formats.pokemon.gen5.terrain.VMapTerrain;

public class VMapSamplerUtil {
	public static void sampleHeight(MapTerrainBuf dest, VMapTerrain terrain, float relPosX, float relPosZ, float chunkSpan, float chunkY) {
		float posX = relPosX + chunkSpan * 0.5f;
		float posZ = relPosZ + chunkSpan * 0.5f;
		int indexX = (int)(posX / 16f);
		float subX = posX % 16f;
		int indexZ = (int)(posZ / 16f);
		float subZ = posZ % 16f;
		
		float heightOffset = 0f;
		
		if (indexX >= terrain.tiles.length) {
			System.err.println("Tile X index " + indexX + " out of bounds for length " + terrain.tiles.length);
			return;
		}
		if (indexZ >= terrain.tiles[0].length) {
			System.err.println("Tile Z index " + indexX + " out of bounds for length " + terrain.tiles[0].length);
			return;
		}
		
		VMapTerrain.Tile t = terrain.tiles[indexX][indexZ];
		switch (t.heightType) {
			case PLAIN:
				heightOffset = 0f;
				dest.heightDiv = 1f;
				dest.slopeX = 0f;
				dest.slopeZ = 0f;
				break;
			case DEFAULT:
				heightOffset = VMapHeightTable.getHeightOffset(t.height);
				dest.slopeX = VMapHeightTable.getSlopeX(t.slope);
				dest.slopeZ = -VMapHeightTable.getSlopeZ(t.slope);
				dest.heightDiv = VMapHeightTable.getSlopeHeightDiv(t.slope);
				break;
			case CORNER:
				VMapTerrain.CornerHeight corner = terrain.corners.get(t.height);
				boolean corner1;
				if ((t.type.flags & 0x8000) != 0) {
					corner1 = subX > subZ; //bottom left corner = 2, top right = 1
				}
				else {
					corner1 = subX + subZ < 16f; //bottom right corner = 2, top left = 1
				}
				int slopeIndex = corner1 ? corner.slope1 : corner.slope2;
				int heightOffsIndex = corner1 ? corner.height1 : corner.height2;
				dest.slopeX = VMapHeightTable.getSlopeX(slopeIndex);
				dest.slopeZ = -VMapHeightTable.getSlopeZ(slopeIndex);
				dest.heightDiv = VMapHeightTable.getSlopeHeightDiv(slopeIndex);
				heightOffset = VMapHeightTable.getHeightOffset(heightOffsIndex);
				break;
		}
		
		dest.tileType = new VMapTerrain.TileType(t.type.getBits() & 0xFFFFFFFE);
		dest.heightY = chunkY + -(dest.slopeX * relPosX + dest.slopeZ * relPosZ + heightOffset) / dest.heightDiv;
	}
	
	public static class MapTerrainBuf {
		public float slopeX;
		public float slopeZ;
		public float heightDiv;
		
		public VMapTerrain.TileType tileType;
		
		public float heightY;
	}
}
