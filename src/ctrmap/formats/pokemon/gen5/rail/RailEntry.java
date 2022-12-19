
package ctrmap.formats.pokemon.gen5.rail;

import xstandard.io.base.iface.DataOutputEx;
import java.io.IOException;

/**
 *
 */
public abstract class RailEntry {
	protected RailData rails;
	
	protected RailEntry(RailData rails){
		this.rails = rails;
	}
	
	public RailData getRails(){
		return rails;
	}
	
	public abstract void write(DataOutputEx out) throws IOException;
}
