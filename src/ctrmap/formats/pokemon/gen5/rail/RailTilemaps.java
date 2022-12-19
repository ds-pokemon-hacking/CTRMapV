package ctrmap.formats.pokemon.gen5.rail;

import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import xstandard.util.ReflectionHashIgnore;
import xstandard.util.ResizeableMatrix;
import java.awt.Color;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RailTilemaps {

	@ReflectionHashIgnore
	private FSFile source;

	public List<Block> blocks = new ArrayList<>();

	public RailTilemaps(FSFile fsf) {
		this.source = fsf;
		try {
			DataInStream in = fsf.getDataInputStream();

			int blockCount = in.readInt();
			for (int i = 0; i < blockCount; i++) {
				blocks.add(new Block(in));
			}

			in.close();
		} catch (IOException ex) {
			Logger.getLogger(RailTilemaps.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void store() {
		try (DataOutStream out = source.getDataOutputStream()) {
			out.writeInt(blocks.size());
			for (Block b : blocks) {
				b.write(out);
			}
		} catch (IOException ex) {
			Logger.getLogger(RailTilemaps.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static class Block {

		public ResizeableMatrix<Tile> tiles;

		public Block(DataInput in) throws IOException {
			tiles = new ResizeableMatrix(in.readShort(), in.readShort(), null);
			for (int y = 0; y < tiles.getHeight(); y++) {
				for (int x = 0; x < tiles.getWidth(); x++) {
					tiles.set(x, y, new Tile(in));
				}
			}
		}

		public Texture createTexture() {
			Texture tex = new Texture(tiles.getWidth(), tiles.getHeight(), TextureFormatHandler.RGBA8);

			byte[] b = tex.data;
			for (int x = 0; x < tiles.getWidth(); x++) {
				for (int y = 0; y < tiles.getHeight(); y++) {
					Tile t = tiles.get(x, y);
					int rgb = new Random(t.type).nextInt(0xFFFFFF);
					Color col = new Color(rgb);
					int off = (y * tex.width + x) * tex.format.getNativeBPP();
					b[off] = (byte) col.getRed();
					b[off + 1] = (byte) col.getGreen();
					b[off + 2] = (byte) col.getBlue();
					b[off + 3] = (byte) 0xFF;
				}
			}

			return tex;
		}

		public void write(DataOutput out) throws IOException {
			out.writeShort(tiles.getWidth());
			out.writeShort(tiles.getHeight());
			for (Tile t : tiles) {
				t.write(out);
			}
		}

		public static class Tile {

			public int type;
			public int coll;
			public int shadow;

			public Tile(DataInput in) throws IOException {
				type = in.readShort();
				coll = in.readUnsignedByte();
				shadow = in.readUnsignedByte();
			}

			public void write(DataOutput out) throws IOException {
				out.writeShort(type);
				out.write(coll);
				out.write(shadow);
			}
		}
	}
}
