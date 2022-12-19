
package ctrmap.formats.pokemon.gen5.font;

import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class GFSystemFont {
	
	public int glyphWidth;
	public int glyphHeight;
	public int letterSpacing;
	public int lineSpacing;
	
	public Glyph[] glyphs;
	
	public GFSystemFont(FSFile fsf) {
		try {
			DataIOStream in = fsf.getDataIOStream();
			
			int glyphsOffset = in.readInt();
			int glyphWidthsOffset = in.readInt();
			int glyphCount = in.readInt();
			glyphWidth = in.readUnsignedByte();
			glyphHeight = in.readUnsignedByte();
			letterSpacing = in.readUnsignedByte();
			lineSpacing = in.readUnsignedByte();
			glyphs = new Glyph[glyphCount];
			
			in.seek(glyphsOffset);
			for (int i = 0; i < glyphCount; i++) {
				Glyph g = new Glyph();
				g.bitmap = in.readBytes((glyphWidth * glyphHeight) >> 3);
				glyphs[i] = g;
			}
			
			in.seek(glyphWidthsOffset);
			for (int i = 0; i < glyphCount; i++) {
				glyphs[i].width = in.readUnsignedByte();
			}
			
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(GFSystemFont.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public BufferedImage createGlyphBuffer() {
		return new BufferedImage(glyphWidth, glyphHeight, BufferedImage.TYPE_BYTE_BINARY);
	}
	
	public BufferedImage renderGlyph(int glyphIndex, BufferedImage dest) {
		return glyphs[glyphIndex].toImage(dest);
	}
	
	public BufferedImage renderToSheet(int charsPerRow) {
		int cellWidth = (this.glyphWidth + letterSpacing);
		int cellHeight = (this.glyphHeight + lineSpacing);
		BufferedImage sheet = new BufferedImage(cellWidth * charsPerRow, cellHeight * ((glyphs.length + charsPerRow - 1) / charsPerRow), BufferedImage.TYPE_INT_RGB);
		Graphics grp = sheet.getGraphics();
		BufferedImage tempGlyphBuf = createGlyphBuffer();
		for (int glyphIndex = 0; glyphIndex < glyphs.length; glyphIndex++) {
			int outX = (glyphIndex % charsPerRow) * cellHeight;
			int outY = (glyphIndex / charsPerRow) * cellWidth;
			if (outY >= sheet.getHeight()) {
				break;
			}
			if (outX >= sheet.getWidth()) {
				continue;
			}
			renderGlyph(glyphIndex, tempGlyphBuf);
			grp.drawImage(tempGlyphBuf, outX, outY, outX + this.glyphWidth, outY + this.glyphHeight, 0, 0, this.glyphWidth, this.glyphHeight, null);
		}
		grp.setColor(Color.DARK_GRAY);
		for (int x = cellWidth - 1; x < sheet.getWidth(); x += cellWidth) {
			grp.drawLine(x, 0, x, sheet.getHeight());
		}
		for (int y = cellHeight - 1; y < sheet.getHeight(); y += cellHeight) {
			grp.drawLine(0, y, sheet.getWidth(), y);
		}
		return sheet;
	}
	
	public static void main(String[] args) {
		GFSystemFont font = new GFSystemFont(new DiskFile("D:\\Emugames\\DS\\hacking_stuff\\IDB\\NewIDB\\BuiltInFont.gfsysfont"));
		BufferedImage sheet = font.renderToSheet(16);
		try {
			ImageIO.write(sheet, "png", new File("D:\\Emugames\\DS\\hacking_stuff\\IDB\\NewIDB\\SystemFont.png"));
		} catch (IOException ex) {
			Logger.getLogger(GFSystemFont.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static class Glyph {
		public int width;
		public byte[] bitmap;
		
		BufferedImage toImage(BufferedImage dest) {
			byte[] imgData = ((DataBufferByte) dest.getRaster().getDataBuffer()).getData();
			int w = dest.getWidth();
			int h = dest.getHeight();
			int tileCountX = (w + 7) >> 3;
			int tileCountY = (h + 7) >> 3;
			int outIndex = 0;
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x += 8) {
					int tileIndex = (x >> 3) + ((y >> 3) * tileCountX);
					int rowIndexInTile = y & 7;
					int inputIndex = (tileIndex << 3) + rowIndexInTile;
					byte inData = bitmap[inputIndex];
					imgData[outIndex] = inData;
					outIndex++;
				}
			}
			return dest;
		}
	}
}
