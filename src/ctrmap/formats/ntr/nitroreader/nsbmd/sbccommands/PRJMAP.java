package ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMDModelConverter;
import ctrmap.renderer.scene.texturing.MaterialParams;
import java.io.IOException;

public class PRJMAP extends SBCCommand {

	private int matIdx;
	private int unused;

	public PRJMAP(NTRDataIOStream data) throws IOException {
		matIdx = data.read();
		unused = data.read();
	}

	@Override
	public void toGeneric(NSBMDModelConverter conv) {
		conv.setMaterialTexMap(matIdx, MaterialParams.TextureMapMode.PROJECTION_MAP);
	}
}
