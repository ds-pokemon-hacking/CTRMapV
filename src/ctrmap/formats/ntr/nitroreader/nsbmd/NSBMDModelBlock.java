package ctrmap.formats.ntr.nitroreader.nsbmd;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.util.List;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import java.io.IOException;

public class NSBMDModelBlock {
    public List<NSBMDModel> models;

    public NSBMDModelBlock(NTRDataIOStream data) throws IOException {
        data.setBaseHere();
        String magic = data.readPaddedString(4);
        int size = data.readInt();
        models = NNSPatriciaTreeReader.processOffsetTree(data, (io, entry) -> {
			return new NSBMDModel(data, entry.getName());
		});
        data.resetBase();
    }

    public List<NSBMDModel> getModels() {
        return models;
    }
}
