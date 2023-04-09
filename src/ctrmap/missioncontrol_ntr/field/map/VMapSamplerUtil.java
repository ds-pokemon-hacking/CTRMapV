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
				dest.slopeCos = 1f;
				dest.slopeSinX = 0f;
				dest.slopeSinZ = 0f;
				break;
			case DEFAULT:
				heightOffset = VMapHeightTable.getHeightOffset(t.height);
				dest.slopeSinX = VMapHeightTable.getSlopeSinX(t.slope);
				dest.slopeSinZ = -VMapHeightTable.getSlopeSinZ(t.slope);
				dest.slopeCos = VMapHeightTable.getSlopeCos(t.slope);
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
				heightOffset = VMapHeightTable.getHeightOffset(heightOffsIndex);
				dest.slopeSinX = VMapHeightTable.getSlopeSinX(slopeIndex);
				dest.slopeSinZ = -VMapHeightTable.getSlopeSinZ(slopeIndex);
				dest.slopeCos = VMapHeightTable.getSlopeCos(slopeIndex);
				break;
		}
		
		dest.tileType = new VMapTerrain.TileType(t.type.getBits() & 0xFFFFFFFE);
		dest.heightY = chunkY + -(dest.slopeSinX * relPosX + dest.slopeSinZ * relPosZ + heightOffset) / dest.slopeCos;
	}
	
	public static class MapTerrainBuf {
		public float slopeSinX;
		public float slopeSinZ;
		public float slopeCos;
		
		public VMapTerrain.TileType tileType;
		
		public float heightY;
	}
}
