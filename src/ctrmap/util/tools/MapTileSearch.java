package ctrmap.util.tools;

import ctrmap.formats.pokemon.containers.DefaultGamefreakContainer;
import ctrmap.formats.pokemon.containers.GFContainer;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapTileSearch {

	public static void main(String[] args) {
		FSFile mapFileDir = new DiskFile("D:\\_REWorkspace\\pokescript_genv\\00\\8");

		int seekTileClass = 0xF;
		int seekTileFlags = 0x100;

		boolean seekFlags = false;

		for (FSFile chunk : mapFileDir.listFiles()) {
			GFContainer gfcont = new DefaultGamefreakContainer(chunk, "DM");

			MapTile mapTile = new MapTile();

			switch (gfcont.getSignature()) {
				case "WB":
				case "GC":
					byte[] tiles = gfcont.getFile(1);

					try {
						DataIOStream in = new DataIOStream(tiles);
						int w = in.readUnsignedShort();
						int h = in.readUnsignedShort();
						for (int y = 0; y < h; y++) {
							for (int x = 0; x < w; x++) {
								mapTile.set(in);
								if (seekFlags) {
									if ((mapTile.getTileFlags() & seekTileFlags) != 0) {
										System.out.println("TILEFLAGS " + Integer.toHexString(mapTile.getTileFlags()) + " in file " + chunk + " xy " + x + ":" + y);
									}
								} else {
									if (mapTile.getTileClass() == seekTileClass) {
										System.out.println("TILECLASS " + Integer.toHexString(seekTileClass) + " in file " + chunk + " xy " + x + ":" + y);
									}
									if (mapTile.getField0Type() == 2) {
										//System.out.println("field0 2 at " + Integer.toHexString(seekTileClass) + " in file " + chunk + " xy " + x + ":" + y);
									}
								}
							}
						}
						in.close();
					} catch (IOException ex) {
						Logger.getLogger(MapTileSearch.class.getName()).log(Level.SEVERE, null, ex);
					}

					break;

			}
		}
	}

	private static class MapTile {

		public int param1;
		public int height;
		public int type;

		public void set(DataInput in) throws IOException {
			param1 = in.readUnsignedShort();
			height = in.readUnsignedShort();
			type = in.readInt();
		}

		public int getField0Type() {
			return param1 & 3;
		}

		public int getTileClass() {
			return type & 0xFFFF;
		}

		public int getTileFlags() {
			return (type >> 16) & 0xFFFF;
		}
	}
}
