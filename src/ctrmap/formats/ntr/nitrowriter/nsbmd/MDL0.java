
package ctrmap.formats.ntr.nitrowriter.nsbmd;

import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DDataBlockBase;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;
import ctrmap.formats.ntr.nitrowriter.nsbtx.TEX0;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scenegraph.G3DResource;

public class MDL0 extends NNSG3DDataBlockBase {
	
	public MDL0(G3DResource rsc, NSBMDExportSettings settings, NNSWriterLogger log){
		super("MDL0");
		int modelCount = 0;
		for (Model model : rsc.models){
			if (modelCount >= 255){
				log.err("Model count over 255! Omitting model " + model.name);
				continue;
			}
			tree.addResource(new NitroModelResource(model, rsc.textures, settings, log));
			modelCount++;
		}
	}
	
	public void syncTEX0(TEX0 tex0){
		for (NNSG3DResource res : tree){
			if (res instanceof NitroModelResource){
				((NitroModelResource)res).syncTEX0(tex0);
			}
		}
	}
}
