package ctrmap.formats.ntr.nitroreader.nsbtx;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.Utils;
import java.io.IOException;

public class NSBTXPalette {

	public final int originalOffset;

	public final short[] colors;
	public final String name;

	public NSBTXPalette(NTRDataIOStream data, String name, int size) throws IOException {
		//System.out.println("read palette " + name + " size " + size + " off " + Integer.toHexString(data.getPosition()));
		this.name = name;
		this.originalOffset = data.getPosition();
		if (size >= 0) {
			int count = size >> 1;
			colors = new short[count];
			for (int i = 0; i < count; i++) {
				colors[i] = data.readShort();
			}
		} else {
			colors = new short[0];
		}
	}
	
	public NSBTXPalette(String name, short[] colors) {
		this.name = name;
		this.colors = colors;
		originalOffset = -1;
	}
}
