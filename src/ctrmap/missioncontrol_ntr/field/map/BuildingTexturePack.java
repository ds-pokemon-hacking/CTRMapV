
package ctrmap.missioncontrol_ntr.field.map;

import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;

public class BuildingTexturePack extends G3DResource {
	
	private final FSFile fsf;
	
	public BuildingTexturePack(FSFile fsf, G3DResource base) {
		this.fsf = fsf;
		merge(base);
	}
	
	public void setBaseBytes(byte[] data) {
		fsf.setBytes(data);
	}
}
