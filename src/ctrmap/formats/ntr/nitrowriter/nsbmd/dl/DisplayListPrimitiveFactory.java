package ctrmap.formats.ntr.nitrowriter.nsbmd.dl;

import ctrmap.formats.ntr.common.gfx.commands.GEDisplayList;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxColorSet;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxListBegin;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxListEnd;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxNormalSet;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSet10;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSet16;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSetDiff;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSetXY;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSetXZ;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSetYZ;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxUvSet;
import ctrmap.formats.ntr.common.FX;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;

public class DisplayListPrimitiveFactory {

	private static final float VTX_DIFF_MAX_ABS = 512f / 4096f;

	private PrimitiveType currentPrimitiveType = null;
	private RGBA currentColor = new RGBA(-1, -1, -1, -1);
	private Vec3f currentNormal = new Vec3f(0, 0, 0);
	private Vec3f currentPositionAbs = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	private Vec2f currentUV = new Vec2f(Float.MAX_VALUE, Float.MAX_VALUE);

	private DLSubMesh sm;
	private GEDisplayList dl;

	public DisplayListPrimitiveFactory(DLSubMesh sm, GEDisplayList dl) {
		this.sm = sm;
		this.dl = dl;
	}

	public void setChangePrimitiveMode(PrimitiveType type) {
		if (currentPrimitiveType != type || type == PrimitiveType.QUADSTRIPS || type == PrimitiveType.TRISTRIPS) {
			if (currentPrimitiveType != null) {
				dl.addCommand(new VtxListEnd());
			}
			dl.addCommand(new VtxListBegin(type));
			currentPrimitiveType = type;
		}
	}

	public void addVertex(SeparablePrimitive primitive, int vtxNo) {
		Vertex vtx = primitive.vertices[vtxNo];

		if (sm.meshAttributes.hasUV && !vtx.uv[0].equals(currentUV)) {
			currentUV = vtx.uv[0];
			dl.addCommand(new VtxUvSet(currentUV, sm.texDim));
		}

		/*
			According to GBATek, rendering can be accelerated by writing UV coordinates BEFORE normals.
			I'm not sure if the same applies to vertex colors, but since they use p much the same pipeline,
			I decided to write UVs first of everything just to be sure.
		 */
		if (sm.meshAttributes.hasColor && !vtx.color.equals(currentColor)) {
			currentColor = vtx.color;
			dl.addCommand(new VtxColorSet(currentColor));
		}

		if (sm.meshAttributes.hasNormal && !vtx.normal.equalsImprecise(currentNormal, 0.001f)) {
			Vec3f tNormal = primitive.normalsAbs[vtxNo];
			currentNormal = vtx.normal;
			dl.addCommand(new VtxNormalSet(tNormal));
		}

		Vec3f tPosition = primitive.positionsAbs[vtxNo];

		if (tPosition.x == currentPositionAbs.x) {
			dl.addCommand(new VtxPosSetYZ(tPosition.y, tPosition.z));
		} else if (tPosition.y == currentPositionAbs.y) {
			dl.addCommand(new VtxPosSetXZ(tPosition.x, tPosition.z));
		} else if (tPosition.z == currentPositionAbs.z) {
			dl.addCommand(new VtxPosSetXY(tPosition.x, tPosition.y));
		} else {
			Vec3f fxifyLast = new Vec3f();
			Vec3f fxifyNext = new Vec3f();
			fxify(currentPositionAbs, fxifyLast);
			fxify(tPosition, fxifyNext);
			Vec3f diff = new Vec3f();
			fxifyNext.sub(fxifyLast, diff);

			if (Math.abs(diff.x) < VTX_DIFF_MAX_ABS && Math.abs(diff.y) < VTX_DIFF_MAX_ABS && Math.abs(diff.z) < VTX_DIFF_MAX_ABS) {
				dl.addCommand(new VtxPosSetDiff(diff));
			} else {
				//The VtxPosSet10 command has half the fract bits compared to a standard FX16
				//In turn, it's sqrt times as imprecise as VtxPosSet16 - 64 compared to 4096
				//We can check how much of a deviation using the command would make and decide according to that
				float vecDeviation = checkDivVecMaxDeviation(tPosition, 64f);

				if (vecDeviation <= FX.FX_MIN) {
					//The imprecisions are about the same as on a FX32 since the deviation is lower than 1/4096f
					//This actually has double the tolerance since the values can go from left or right of the original value
					//Might change this later if it causes problems
					dl.addCommand(new VtxPosSet10(tPosition));
				} else {
					dl.addCommand(new VtxPosSet16(tPosition));
				}
			}
		}

		currentPositionAbs = tPosition;
	}
	
	private static void fxify(Vec3f src, Vec3f dest) {
		dest.x = FX.unfx16(FX.fx16(src.x));
		dest.y = FX.unfx16(FX.fx16(src.y));
		dest.z = FX.unfx16(FX.fx16(src.z));
	}

	public void addPrimitive(SeparablePrimitive p) {
		setChangePrimitiveMode(p.type);

		for (int i = 0; i < p.vertices.length; i++) {
			addVertex(p, i);
		}
	}

	private static float checkDivVecMaxDeviation(Vec3f vec, float div) {
		return Math.max(getImprecisionDeviation(vec.x, div), Math.max(getImprecisionDeviation(vec.y, div), getImprecisionDeviation(vec.z, div)));
	}

	private static float getImprecisionDeviation(float value, float div) {
		return Math.abs((Math.round(value * div) / div) - value);
	}
}
