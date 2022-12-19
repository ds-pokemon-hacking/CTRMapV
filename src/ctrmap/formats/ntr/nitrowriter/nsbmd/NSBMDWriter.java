package ctrmap.formats.ntr.nitrowriter.nsbmd;

import ctrmap.formats.ntr.nitrowriter.common.NNSG3DWriter;
import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;
import ctrmap.formats.ntr.nitrowriter.nsbmd.mat.NitroMaterialResource;
import ctrmap.formats.ntr.nitrowriter.nsbtx.TEX0;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceType;
import xstandard.gui.file.ExtensionFilter;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NSBMDWriter extends NNSG3DWriter {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Nitro System Binary Model", "*.nsbmd");

	public NSBMDWriter(G3DResource rsc) {
		this(rsc, new NSBMDExportSettings(), new NNSWriterLogger.DummyLogger());
	}

	public NSBMDWriter(G3DResource rsc, NSBMDExportSettings settings, NNSWriterLogger log) {
		super("BMD0");
		rsc.renameAllDuplicates();
		MDL0 mdl0 = null;
		TEX0 tex0 = null;
		if (settings.includeModels) {
			addBlock(mdl0 = new MDL0(rsc, settings, log));
		}
		if (settings.includeTextures) {
			addBlock(tex0 = new TEX0(getNSBMDConvertTexList(rsc, mdl0, settings, log), log));
		}

		if (mdl0 != null) {
			//Null tex0 preserves original names
			mdl0.syncTEX0(tex0);
		}
	}

	public static List<Texture> getNSBMDConvertTexList(G3DResource res, MDL0 mdl0, NSBMDExportSettings settings, NNSWriterLogger log) {
		List<Texture> textures = new ArrayList<>();
		if (settings.includeTextures) {
			if (settings.omitUnusedTextures) {
				if (mdl0 != null) {
					for (NNSG3DResource nnres : mdl0.tree) {
						if (nnres instanceof NitroModelResource) {
							NitroModelResource mdl = (NitroModelResource) nnres;
							for (NitroMaterialResource mat : mdl.materials.materials) {
								ArraysEx.addIfNotNullOrContains(textures, mat.texture);
							}
						}
					}
				} else {
					HashSet<String> presentTexNames = new HashSet<>();
					for (Model mdl : res.models) {
						for (Material mat : mdl.materials) {
							if (!mat.textures.isEmpty()) {
								String texName = mat.textures.get(0).textureName;
								if (!presentTexNames.contains(texName)) {
									Texture tex = (Texture) res.getNamedResource(texName, G3DResourceType.TEXTURE);
									if (tex != null) {
										textures.add(tex);
									}
									presentTexNames.add(texName);
								}
							}
						}
					}
				}
			} else {
				textures.addAll(res.textures);
			}
		}
		while (textures.size() > 255) {
			Texture tex = textures.remove(255);
			log.err("Texture count over 255! Omitting texture " + tex.name);
		}
		return textures;
	}
}
