package ctrmap.formats.pokemon.gen5.area;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.pokemon.gen5.gfbtp.GFBTP;
import ctrmap.formats.ntr.nitroreader.nsbtx.NSBTX;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AreaPatAnimeData {
	
	public List<AreaPatAnime> patAnime = new ArrayList<>();

	public AreaPatAnimeData(FSFile fsf) {
		try {
			NTRDataIOStream io = new NTRDataIOStream(fsf.getIO());
			
			int dataCount = io.readInt();
			int[] offsets = new int[dataCount * 2 + 1];
			
			for (int i = 0; i < offsets.length; i++){
				offsets[i] = io.readInt();
			}
			
			for (int i = 0; i < dataCount; i++) {
				int o = i * 2;
				int btpOffs = offsets[o];
				int btxOffs = offsets[o + 1];
				
				io.seek(btpOffs);
				io.setBaseHere();
				GFBTP btp = new GFBTP(io);
				io.resetBase();
				
				io.seek(btxOffs);
				io.setBaseHere();
				NSBTX btx = new NSBTX(io);
				io.resetBase();
				
				patAnime.add(new AreaPatAnime(btp, btx));
			}
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(AreaPatAnimeData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public G3DResource toGeneric(List<Material> worldMaterials){
		G3DResource res = new G3DResource();
		for (AreaPatAnime a : patAnime){
			res.mergeFull(a.toGeneric(worldMaterials));
		}
		return res;
	}
}
