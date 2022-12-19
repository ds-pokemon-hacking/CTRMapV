package ctrmap.formats.ntr.nitroreader.nsbtx.info;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

public class NSBTXHeader {
    public final NSBTXPaletteHeader paletteHeader;
    public final NSBTXCompressedTextureHeader compressedTexHeader;
    public final NSBTXTextureHeader texHeader;

    public NSBTXHeader(NTRDataIOStream data) throws IOException {
        texHeader = new NSBTXTextureHeader(data);
        compressedTexHeader = new NSBTXCompressedTextureHeader(data);
        paletteHeader = new NSBTXPaletteHeader(data);
    }
}
