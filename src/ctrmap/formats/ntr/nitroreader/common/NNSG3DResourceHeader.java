package ctrmap.formats.ntr.nitroreader.common;

import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

public class NNSG3DResourceHeader {
	public final int signature;
    public final int byteOrder;
	public final int version;
    public final int fileSize;
    public final int headerSize;
	
    public final int[] offsets;

    public NNSG3DResourceHeader(DataIOStream data) throws IOException {
        this.signature = data.readInt();
        this.byteOrder = data.readUnsignedShort();
        this.version = data.readUnsignedShort();
        this.fileSize = data.readInt();
        this.headerSize = data.readUnsignedShort();
        int blockCount = data.readUnsignedShort();
		offsets = new int[blockCount];
        for (int i = 0; i < blockCount; i++) {
            this.offsets[i] = data.readInt();
        }
    }
}
