
package ctrmap.formats.pokemon.gen5.camera;

import xstandard.math.MathEx;
import xstandard.math.vec.Vec3f;

public class VCameraCoordinates {
	public float pitch = 45f;
	public float yaw;
	public float tz = 237;
	
	public Vec3f targetOffset = new Vec3f();
	
	public float FOV = 50f;
	
	public void setNull(){
		pitch = 0f;
		yaw = 0f;
		tz = 0f;
		targetOffset.zero();
		FOV = 0f;
	}
	
	public void restrictFxAnglesUnsigned() {
		pitch = MathEx.clamp(0f, 360f, pitch);
		yaw = MathEx.clamp(0f, 360f, yaw);
	}
	
	public void addWeighted(VCameraCoordinates addition, float weight) {
		pitch += addition.pitch * weight;
		yaw += addition.yaw * weight;
		FOV += addition.FOV * weight;
		tz += addition.tz * weight;
		targetOffset.add(addition.targetOffset.clone().mul(weight));
	}

	public void normalizeAngles() {
		pitch = MathEx.clamp(0f, 89f, pitch);
		FOV = MathEx.clamp(10f, 179f, FOV);
	}
	
	public VCameraCoordinates lerp(VCameraCoordinates other, VCameraCoordinates dest, float weight){
		dest.pitch = interpolateRotationAccurate(pitch, other.pitch, weight);
		dest.yaw = interpolateRotationAccurate(yaw, other.yaw, weight);
		dest.tz = tz + weight * (other.tz - tz);
		dest.targetOffset.x = targetOffset.x + weight * (other.targetOffset.x - targetOffset.x);
		dest.targetOffset.y = targetOffset.y + weight * (other.targetOffset.y - targetOffset.y);
		dest.targetOffset.z = targetOffset.z + weight * (other.targetOffset.z - targetOffset.z);
		dest.FOV = FOV + weight * (other.FOV - FOV);
		
		return dest;
	}
	
	private static float interpolateRotationAccurate(float left, float right, float weight) {
		//inferior method, but accurate to that used in-game
		return left + weight * (right - left);
	}
	
	private static float interpolateRotation(float left, float right, float weight) {
		right %= 360f;
		left %= 360f;
		if (right - left > 180f){
			right = -360f + right;
		}
		return left + (right - left) * weight;
	}
}
