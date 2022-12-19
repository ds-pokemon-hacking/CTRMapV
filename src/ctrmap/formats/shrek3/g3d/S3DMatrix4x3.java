
package ctrmap.formats.shrek3.g3d;

import org.joml.Matrix4x3f;

public class S3DMatrix4x3 {
    float m00, m01, m02;
    float m10, m11, m12;
    float m20, m21, m22;
    float m30, m31, m32;
	
	public Matrix4x3f toMatrix() {
		return new Matrix4x3f(m00, m01, m02, m10, m11, m12, m20, m21, m22, m30, m31, m32);
	}
}
