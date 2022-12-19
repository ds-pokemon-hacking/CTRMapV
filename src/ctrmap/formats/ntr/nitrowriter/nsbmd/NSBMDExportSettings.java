
package ctrmap.formats.ntr.nitrowriter.nsbmd;

import ctrmap.formats.common.FormatIOExConfig;

public class NSBMDExportSettings implements FormatIOExConfig {
	public final boolean preferVColOverNormal;
	public final boolean makePolystrips;
	public final int minimumPolystripPrimitiveCount;
	
	public final boolean jointsAsVisGroups;
	
	public final boolean includeModels;
	public final boolean includeTextures;
	
	public final boolean omitUnusedTextures;
	
	public NSBMDExportSettings(){
		preferVColOverNormal = true;
		includeModels = true;
		includeTextures = true;
		makePolystrips = false;
		omitUnusedTextures = true;
		jointsAsVisGroups = true;
		minimumPolystripPrimitiveCount = -1;
	}
	
	public NSBMDExportSettings(boolean incMdl, boolean incTex, boolean preferVCol, boolean makePolystrips, int minimumPolystripPrimitiveCount, boolean jointsAsVisgroups, boolean omitUnusedTex){
		includeModels = incMdl;
		includeTextures = incTex;
		preferVColOverNormal = preferVCol;
		omitUnusedTextures = omitUnusedTex;
		this.makePolystrips = makePolystrips;
		this.jointsAsVisGroups = jointsAsVisgroups;
		this.minimumPolystripPrimitiveCount = minimumPolystripPrimitiveCount;
	}
}
