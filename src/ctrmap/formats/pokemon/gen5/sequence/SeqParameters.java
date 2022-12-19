package ctrmap.formats.pokemon.gen5.sequence;

import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.common.gfx.GXColorAlpha;
import xstandard.formats.yaml.YamlNode;
import xstandard.formats.yaml.YamlNodeName;
import xstandard.formats.yaml.YamlReflectUtil;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SeqParameters {

	@YamlNodeName("Camera")
	public CameraConfig camera = new CameraConfig();

	@YamlNodeName("RenderState")
	public SeqRenderState renderState = new SeqRenderState();
	@YamlNodeName("Fog")
	public FogConfig fog = new FogConfig();
	@YamlNodeName("EdgeTable")
	public EdgeTableConfig edgeTable = new EdgeTableConfig();
	@YamlNodeName("ClearColor")
	public ClearColorConfig clearColor = new ClearColorConfig();

	@YamlNodeName("P1")
	public int p1;
	@YamlNodeName("FieldAreaID")
	public int fieldAreaID;
	@YamlNodeName("P8")
	public int p8;
	@YamlNodeName("P9")
	public int p9;

	@YamlNodeName("P10")
	public float p12;

	@YamlNodeName("field_4E")
	public int field_4E;
	@YamlNodeName("field_50")
	public int field_50;
	@YamlNodeName("field_52")
	public int field_52;
	
	private byte[] raw = new byte[42 * 2];

	public SeqParameters(DataInput in) throws IOException {
		//in.readFully(raw);
		if (true){
			//return;
		}
		camera.animeID = in.readUnsignedShort();
		p1 = in.readUnsignedShort();
		fieldAreaID = in.readUnsignedShort();

		int flags = in.readUnsignedShort();
		renderState.isVBlank60FPS = (flags & 1) != 0;
		renderState.isDual3DRender = (flags & 2) != 0;
		renderState.alphaBlend = (flags & 4) != 0;
		renderState.antiAliasing = (flags & 8) != 0;
		fog.enableFog = (flags & 16) != 0;
		edgeTable.enableEdgeColorTable = (flags & 32) != 0;
		renderState.depthSort.manual = (flags & 64) != 0;
		renderState.depthSort.wDepth = (flags & 128) != 0;
		renderState.alphaTest.enabled = (flags & 256) != 0;
		renderState.alphaTest.reference = (flags >> 9) & 31;

		camera.fovX = in.readInt() / 4096f;
		camera.fovY = in.readInt() / 4096f;

		p8 = in.readUnsignedShort();
		p9 = in.readUnsignedShort();
		camera.zNear = in.readInt() / 4096f;
		camera.zFar = in.readInt() / 4096f;
		p12 = in.readInt() / 4096f;

		camera.topScreenOffset = in.readShort();
		camera.bottomScreenOffset = in.readShort();

		renderState.disp1DotDepth = in.readInt() / 4096f;

		clearColor.color = new GXColorAlpha(in.readUnsignedShort(), in.readUnsignedShort());
		clearColor.polygonID = in.readUnsignedShort();
		clearColor.depth = in.readUnsignedShort();

		fog.color = new GXColorAlpha(in.readUnsignedShort(), in.readUnsignedByte());
		fog.slope = in.readUnsignedByte();
		int blendAndOffset = in.readUnsignedShort();
		fog.blend = blendAndOffset >> 15;
		fog.offset = blendAndOffset & 0x7FFF;

		for (int i = 0; i < 8; i++) {
			fog.fogTable[i] = in.readUnsignedByte();
		}

		for (int i = 0; i < 8; i++) {
			edgeTable.edgeColorTable[i] = new GXColor(in.readUnsignedShort());
		}

		field_4E = in.readUnsignedShort();
		field_50 = in.readUnsignedShort();
		field_52 = in.readUnsignedShort();
	}
	
	public SeqParameters(){
		
	}
	
	public void write(DataOutput out) throws IOException {
		//out.write(raw);
		if (true){
			//return;
		}
		out.writeShort(camera.animeID);
		out.writeShort(p1);
		out.writeShort(fieldAreaID);
		
		int flags = 0;
		flags |= renderState.isVBlank60FPS ? 1 : 0;
		flags |= renderState.isDual3DRender ? 2 : 0;
		flags |= renderState.alphaBlend ? 4 : 0;
		flags |= renderState.antiAliasing ? 8 : 0;
		flags |= fog.enableFog ? 16 : 0;
		flags |= edgeTable.enableEdgeColorTable ? 32 : 0;
		flags |= renderState.depthSort.manual ? 64 : 0;
		flags |= renderState.depthSort.wDepth ? 128 : 0;
		flags |= renderState.alphaTest.enabled ? 256 : 0;
		flags |= renderState.alphaTest.reference << 9;
		out.writeShort(flags);
		
		out.writeInt((int)(camera.fovX * 4096f));
		out.writeInt((int)(camera.fovY * 4096f));
		
		out.writeShort(p8);
		out.writeShort(p9);
		
		out.writeInt((int)(camera.zNear * 4096f));
		out.writeInt((int)(camera.zFar * 4096f));
		out.writeInt((int)(p12 * 4096f));

		out.writeShort(camera.topScreenOffset);
		out.writeShort(camera.bottomScreenOffset);
		
		out.writeInt((int)(renderState.disp1DotDepth * 4096f));
		
		clearColor.color.write(out);
		clearColor.color.writeAlpha(out, true);
		out.writeShort(clearColor.polygonID);
		out.writeShort(clearColor.depth);
		
		fog.color.write(out);
		fog.color.writeAlpha(out, false);
		out.write(fog.slope);
		out.writeShort((fog.blend & 0x7FFF) | ((fog.offset & 1) << 15));
		
		for (int i = 0; i < 8; i++){
			out.write(fog.fogTable[i]);
		}
		
		for (int i = 0; i < 8; i++){
			edgeTable.edgeColorTable[i].write(out);
		}
		
		out.writeShort(field_4E);
		out.writeShort(field_50);
		out.writeShort(field_52);
	}

	public YamlNode getYMLNode() {
		YamlNode n = YamlReflectUtil.serialize("Parameters", this);
		return n;
	}

	public static class CameraConfig {

		@YamlNodeName("AnimeID")
		public int animeID;
		@YamlNodeName("FOVX")
		public float fovX;
		@YamlNodeName("FOVY")
		public float fovY;
		
		@YamlNodeName("ZFar")
		public float zFar;
		@YamlNodeName("ZNear")
		public float zNear;

		@YamlNodeName("TopScreenOffset")
		public int topScreenOffset;
		@YamlNodeName("BottomScreenOffset")
		public int bottomScreenOffset;
	}

	public static class SeqRenderState {

		@YamlNodeName("IsVBlank60FPS")
		public boolean isVBlank60FPS;
		@YamlNodeName("IsDual3DRender")
		public boolean isDual3DRender;
		@YamlNodeName("AntiAliasing")
		public boolean antiAliasing;
		@YamlNodeName("AlphaBlend")
		public boolean alphaBlend;

		@YamlNodeName("AlphaTest")
		public AlphaTest alphaTest = new AlphaTest();
		@YamlNodeName("DepthSort")
		public DepthSort depthSort = new DepthSort();

		@YamlNodeName("Disp1DotDepth")
		public float disp1DotDepth;

		public static class AlphaTest {

			@YamlNodeName("Enabled")
			public boolean enabled;
			@YamlNodeName("Reference")
			public int reference;
		}

		public static class DepthSort {

			@YamlNodeName("Manual")
			public boolean manual;
			@YamlNodeName("WDepth")
			public boolean wDepth;
		}
	}

	public static class FogConfig {

		@YamlNodeName("EnableFog")
		public boolean enableFog;
		@YamlNodeName("Color")
		public GXColorAlpha color;
		@YamlNodeName("Slope")
		public int slope;
		@YamlNodeName("Blend")
		public int blend;
		@YamlNodeName("Offset")
		public int offset;
		@YamlNodeName("FogTable")
		public int[] fogTable = new int[8];
	}

	public static class EdgeTableConfig {

		@YamlNodeName("EnableEdgeColorTable")
		public boolean enableEdgeColorTable;
		@YamlNodeName("EdgeColorTable")
		public GXColor[] edgeColorTable = new GXColor[8];
	}

	public static class ClearColorConfig {

		@YamlNodeName("Color")
		public GXColorAlpha color;
		@YamlNodeName("PolygonID")
		public int polygonID;
		@YamlNodeName("Depth")
		public int depth;
	}
}
