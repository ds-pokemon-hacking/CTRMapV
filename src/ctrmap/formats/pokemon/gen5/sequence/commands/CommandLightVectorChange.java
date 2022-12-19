
package ctrmap.formats.pokemon.gen5.sequence.commands;

import xstandard.math.vec.Vec3f;

/**
 *
 */
public class CommandLightVectorChange extends SeqCommandObject {
	public int lightId;
	
	public float x;
	public float y;
	public float z;
	
	public Vec3f getVector(){
		return new Vec3f(x, y, z);
	}
}
