package ctrmap.formats.qos.g3d;

import ctrmap.formats.qos.GeminiDeserializer;
import ctrmap.formats.qos.GeminiObject;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.serialization.annotations.Inline;
import java.util.List;

public class GeminiScene extends GeminiObject {
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Gemini Scene", "*.gcb");

	@Inline
	public GeminiMatrix4x3 boundingTransform;
	public List<GeminiModelStub> models;
	public List<GeminiCamera> cameras;
	public List<GeminiAnimationStub> animations;
	public List<GeminiRegionSet> regions;
	public List<GeminiLight> lights;

	public static GeminiScene fromFile(FSFile fsf) {
		return GeminiDeserializer.deserializeFileStatic(GeminiScene.class, fsf);
	}
	
	public G3DResource toGeneric(FSFile root) {
		G3DResource res = new G3DResource();
		for (GeminiModelStub mdl : models) {
			if (mdl instanceof GeminiModel) {
				res.merge(((GeminiModel) mdl).toGeneric(root));
			}
		}
		return res;
	}
}
