package ctrmap.formats.ntr.nitroreader.nsbtx.info;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

public class NSBTXPaletteHeader {
    public final int _rtFlags;
    public final int entriesOffset;
    public final int imageOffset;
    public final int imageSize;
    public final int _rtAddress;

    public NSBTXPaletteHeader(NTRDataIOStream data) throws IOException {
        this._rtAddress = data.readInt();
        this.imageSize = data.readUnsignedShort() << 3;
        this._rtFlags = data.readUnsignedShort();
        this.entriesOffset = data.readUnsignedShort();
        data.skipBytes(2);
        this.imageOffset = data.readInt();
    }
}
