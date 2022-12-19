package ctrmap.missioncontrol_ntr.field.rail;

import xstandard.math.vec.Vec3f;

public class VRailMath {

	public static Vec3f getNoCurvePosition(Vec3f p1, Vec3f p2, float weightY, float posX) {
		Vec3f posDiff = p2.clone().sub(p1);

		Vec3f pos = new Vec3f();
		posDiff.mulAdd(weightY, p1, pos);

		Vec3f perp = new Vec3f();
		posDiff.cross(0f, 1f, 0f, perp);
		perp.normalize();

		Vec3f result = perp;
		result.mulAdd(posX, pos);

		return result;
	}

	public static Vec3f makeSlerp(Vec3f dp1, Vec3f dp2, float yWeight) {
		float len1 = dp1.length();
		float len2 = dp2.length();

		dp1 = dp1.clone();
		dp2 = dp2.clone();
		dp1.normalize();
		dp2.normalize();

		float angle = (float) Math.acos(dp1.dot(dp2));

		float sinAngle = (float) Math.sin(angle);

		Vec3f slerp;

		if (sinAngle == 0.0) {
			slerp = dp2;
		} else {
			float sinAngleWeightFront = (float) Math.sin(angle * (1.0 - yWeight));
			float sinAngleWeightBack = (float) Math.sin(angle * yWeight);

			slerp = dp1.mulAdd(sinAngleWeightFront, dp2.mul(sinAngleWeightBack));
			slerp.div(sinAngle);
			slerp.normalize();
		}

		return slerp.mul(lerp(len1, len2, yWeight));
	}

	public static Vec3f getCurvePositionSlerpXZ(Vec3f p1, Vec3f p2, Vec3f cp, float weightY, float posX) {
		Vec3f c = cp.clone();
		c.y = lerp(p1.y, p2.y, weightY);

		Vec3f dp1 = p1.clone().sub(c);
		Vec3f dp2 = p2.clone().sub(c);

		dp1.y = 0f;
		dp2.y = 0f;

		Vec3f slerp = makeSlerp(dp1, dp2, weightY);

		Vec3f result = slerp.clone().add(c);

		slerp.normalize();

		Vec3f crossProduct = new Vec3f();
		slerp.cross(dp1.sub(dp2).normalize(), crossProduct);

		if (crossProduct.y >= 0.0) {
			result = slerp.mulAdd(-posX, result);
		} else {
			result = slerp.mulAdd(posX, result);
		}

		return result;
	}
	
	public static Vec3f getCurvePositionSlerpXYZ(Vec3f p1, Vec3f p2, Vec3f cp, float weightY, float posX) {
		Vec3f dp1 = p1.clone().sub(cp);
		Vec3f dp2 = p2.clone().sub(cp);

		Vec3f slerp = makeSlerp(dp1, dp2, weightY);

		Vec3f result = slerp.clone().add(cp);
		
		if (slerp.x == 0f || slerp.z == 0f){
			p2.clone().sub(p1).cross(0f, 1f, 0f, slerp);
		}
		else {
			slerp.y = 0f;
		}

		slerp.normalize();

		Vec3f crossProduct = new Vec3f();
		slerp.cross(dp1.sub(dp2).normalize(), crossProduct);

		if (crossProduct.y >= 0.0) {
			result = slerp.mulAdd(-posX, result);
		} else {
			result = slerp.mulAdd(posX, result);
		}

		return result;
	}

	private static float lerp(float a, float b, float weight) {
		return a + (b - a) * weight;
	}
}
