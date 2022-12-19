package ctrmap.formats.qos.g3d;

import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.common.gfx.commands.AbstractGECommandProcessor;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatTexImageParamSet;
import ctrmap.formats.ntr.common.gfx.commands.poly.PolyAttrSet;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeminiModelConverter extends AbstractGECommandProcessor {

	private PrimitiveType primitiveMode;
	private GeminiTextureInstance nowMaterial;
	private List<Vertex> nowVertices = new ArrayList<>();

	private Listener listener;

	private Map<Integer, GeminiTextureInstance> materialOffsets = new HashMap<>();
	private int nowOffset;

	public GeminiModelConverter(Listener listener) {
		this.listener = listener;
	}

	public void setNowCmdOffset(int offs) {
		this.nowOffset = offs;
	}

	public void notifyTextureInstance(GeminiTextureInstance inst) {
		for (int fixup : inst.texImageParamFixups) {
			materialOffsets.put(fixup, inst);
		}
	}

	public GeminiTextureInstance getNowMaterial() {
		return nowMaterial;
	}
	
	private float nowPolygonAlpha = 1f;
	
	public float getNowPolygonAlpha() {
		return nowPolygonAlpha;
	}

	private boolean meshHasColor;
	private boolean meshHasNormal;
	private boolean meshHasUV;

	public void setMeshAttribs(Mesh mesh) {
		mesh.hasColor = meshHasColor;
		mesh.hasNormal = meshHasNormal;
		mesh.hasUV[0] = meshHasUV;
	}

	@Override
	public void vertexEx(Vec3f vertex) {
		Vertex v = new Vertex();
		mulVertex(vertex);
		v.position = vertex;
		if (meshHasColor) {
			v.color = currentColor.clone();
		}
		if (meshHasNormal) {
			v.normal = currentNormal.clone();
			mulNormal(v.normal);
		}
		if (meshHasUV) {
			v.uv[0] = currentTexcoord.clone();
			absTexture(v.uv[0]);
		}
		nowVertices.add(v);
	}

	@Override
	public void color(RGBA col) {
		super.color(col);
		meshHasColor = true;
		meshHasNormal = false;
	}

	@Override
	public void normal(Vec3f normal) {
		super.normal(normal);
		meshHasNormal = true;
		meshHasColor = false;
	}

	@Override
	public void texCoord(Vec2f texcoord) {
		super.texCoord(texcoord);
		meshHasUV = true;
	}

	@Override
	public void matSpecularEmissive(RGBA spec, RGBA emi) {

	}

	@Override
	public void texPaletteBase(int base) {

	}

	@Override
	public void texMap(boolean repeatU, boolean repeatV, boolean mirrorU, boolean mirrorV) {

	}

	@Override
	public void texGenMode(MatTexImageParamSet.GETexcoordGenMode mode) {

	}

	@Override
	public void texColor0Transparent(boolean transparent) {

	}

	@Override
	public void begin(PrimitiveType primitiveMode) {
		nowVertices.clear();
		this.primitiveMode = primitiveMode;
	}

	@Override
	public void end() {
		//on vertex read done
		listener.onPolygonFinished(this, primitiveMode, nowVertices);
	}

	@Override
	public void texImage2D(int w, int h, GETextureFormat fmt, int offset) {
		GeminiTextureInstance texInst = materialOffsets.get(nowOffset);
		if (texInst != null) {
			nowMaterial = texInst;
			w = 8 << texInst.texture.sizeS.ordinal();
			h = 8 << texInst.texture.sizeT.ordinal();
			fmt = GETextureFormat.values()[texInst.texture.format.ordinal()];
		} else {
			System.err.println("Could not find teximage param fixup! @ 0x" + Integer.toHexString(nowOffset));
		}
		super.texImage2D(w, h, fmt, offset);
		if (fmt == GETextureFormat.NULL) {
			meshHasUV = false;
		}
	}

	@Override
	public void polygonId(int polyId) {

	}

	@Override
	public void polygonAlpha(float alpha) {
		nowPolygonAlpha = alpha;
	}

	@Override
	public void setFogEnable(boolean enable) {

	}

	@Override
	public void setLightEnable(int index, boolean enable) {

	}

	@Override
	public void polygonMode(PolyAttrSet.GEPolygonMode mode) {

	}

	@Override
	public void cullFace(boolean drawFront, boolean drawBack) {

	}

	@Override
	public void depthFunc(PolyAttrSet.GEDepthFunction func) {

	}

	@Override
	public void dot1OverMode(PolyAttrSet.GE1DotOverMode mode) {

	}

	@Override
	public void farClipMode(PolyAttrSet.GEFarClipMode mode) {

	}

	@Override
	public void xluDepthMode(PolyAttrSet.GEXLUDepthMode mode) {

	}

	public static interface Listener {

		public void onPolygonFinished(GeminiModelConverter conv, PrimitiveType primitiveType, List<Vertex> polygon);
	}
}
