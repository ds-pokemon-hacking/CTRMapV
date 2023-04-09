package ctrmap.missioncontrol_ntr.field.mmodel;

import ctrmap.renderer.scene.Camera;
import xstandard.math.vec.Matrix4;

public class FieldActorCamera extends Camera {
	
	private float frustumZOffset = 0f;
	
	public FieldActorCamera() {
		viewMode = ViewMode.ROTATE;
		translation.zero();
		rotation.zero();
	}
	
	public void setFrustumZOffset(float offset) {
		this.frustumZOffset = offset;
	}

	@Override
	public Matrix4 getProjectionMatrix(Matrix4 dest) {
		Matrix4 mat = super.getProjectionMatrix(dest);
		//projMatrix.M32 += (projMatrix.M22 * (__int64)(int)field->ObjectProjMatrixOffset + 2048) >> 12;
		mat.m32(mat.m32() + mat.m22() * frustumZOffset);
		return mat;
	}
}
