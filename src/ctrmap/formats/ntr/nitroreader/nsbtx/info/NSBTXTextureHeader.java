package ctrmap.formats.ntr.nitroreader.nsbtx.info;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

public class NSBTXTextureHeader {
    public final int _rtFlags;
    public final int entriesOffset;
    public final int imageOffset;
    public final int imageSize;
    public final long _rtAddress;

    public NSBTXTextureHeader(NTRDataIOStream data) throws IOException {
        this._rtAddress = data.readInt();
        this.imageSize = data.readUnsignedShort() << 3;
        this.entriesOffset = data.readUnsignedShort();
        this._rtFlags = data.readUnsignedShort();
        data.skipBytes(2);
        this.imageOffset = data.readInt();
    }
}
