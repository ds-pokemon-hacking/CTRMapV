
package ctrmap.formats.ntr.nitroreader.nsbmd;

import ctrmap.formats.common.FormatIOExConfig;

/**
 *
 */
public class NSBMDImportSettings implements FormatIOExConfig {
	public final boolean eliminateStrips;
	public final boolean triangulate;
	public final boolean makeSmoothSkin;
	public final boolean mergeMeshesByMaterials;
	
	public NSBMDImportSettings(boolean eliminateStrips, boolean triangulate, boolean makeSmoothSkin, boolean mergeMeshesByMaterials){
		this.eliminateStrips = eliminateStrips | triangulate;
		this.triangulate = triangulate;
		this.makeSmoothSkin = makeSmoothSkin;
		this.mergeMeshesByMaterials = mergeMeshesByMaterials && makeSmoothSkin;
	}
}
