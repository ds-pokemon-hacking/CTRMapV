package ctrmap.formats.shrek3.g3d;

import xstandard.io.serialization.annotations.Size;

public class S3DAnimKey {
	@Size(Short.BYTES)
	public int frame;
	@Size(Short.BYTES)
	public int flags;
}
