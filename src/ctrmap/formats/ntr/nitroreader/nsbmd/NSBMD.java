package ctrmap.formats.ntr.nitroreader.nsbmd;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.common.NNSG3DResource;
import ctrmap.formats.ntr.nitroreader.nsbtx.NSBTXDataBlock;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.MaterialProcessor;
import ctrmap.renderer.scene.texturing.Texture;
import xstandard.fs.FSFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.access.MemoryStream;

public class NSBMD extends NNSG3DResource {

	public static final String MAGIC = "BMD0";

	public NSBMDModelBlock MDL0;
	public NSBTXDataBlock TEX0;

	public NSBMD(NSBMDModelBlock MDL0, NSBTXDataBlock TEX0) {
		this.MDL0 = MDL0;
		this.TEX0 = TEX0;
	}

	public NSBMD(byte[] data) {
		this(new MemoryStream(data));
	}
	
	public NSBMD(FSFile file) {
		this(file.getIO());
	}

	public NSBMD(IOStream io) {
		this(new NTRDataIOStream(io));
	}
	
	public NSBMD(NTRDataIOStream io) {
		try {
			readBase(io);
			seekBlock(io, 0);
			MDL0 = new NSBMDModelBlock(io);
			if (seekBlock(io, 1)) {
				TEX0 = new NSBTXDataBlock(io);
			}
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(NSBMD.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public G3DResource toGeneric() {
		return toGeneric(new NSBMDImportSettings(true, false, true, true));
	}

	public G3DResource toGeneric(NSBMDImportSettings settings) {
		G3DResource res = new G3DResource();
		List<String> convertedTextures = new ArrayList<>();
		for (NSBMDModel mdl : MDL0.getModels()) {
			res.addModel(mdl.toGeneric(settings));

			if (TEX0 != null) {
				for (NSBMDMaterial mat : mdl.getMaterials()) {
					if (mat.textureName != null) {
						if (!convertedTextures.contains(mat.textureName)) {
							Texture tex = TEX0.getConvTexture(mat.textureName, mat.paletteName);
							if (tex != null) {
								res.addTexture(tex);
							}
							convertedTextures.add(mat.textureName);
						}
					}
				}
			}
		}

		MaterialProcessor.setAutoAlphaBlendByTexture(res);

		return res;
	}

	public NSBMDModelBlock getMDL0() {
		return this.MDL0;
	}

	public NSBTXDataBlock getTEX0() {
		return this.TEX0;
	}
}
