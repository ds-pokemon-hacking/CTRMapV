package ctrmap.formats.ntr.nitroreader.nsbtx.info;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

public class NSBTXCompressedTextureHeader {
    public final int _rtFlags;
    public final int entriesOffset;
    public final int imageOffset;
    public final int idxDataOffset;
    public final int imageSize;
    public final long _rtAddress;

    public NSBTXCompressedTextureHeader(NTRDataIOStream data) throws IOException {
        _rtAddress = data.readInt();
        imageSize = data.readUnsignedShort() << 3;
        entriesOffset = data.readUnsignedShort();
        _rtFlags = data.readUnsignedShort();
        data.skipBytes(2);
        imageOffset = data.readInt();
        idxDataOffset = data.readInt();
    }
}
