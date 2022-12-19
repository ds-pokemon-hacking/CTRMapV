package ctrmap.formats.ntr.nitrowriter.nsbva;

import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityBoneTransform;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Skeleton;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryValue;
import xstandard.io.util.BitWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class VisibilityAnimationResource extends NNSG3DResource {

	public static final String VAV_MAGIC = "V\u0000AV";

	public int frameCount;
	private List<NodeVisibilityTrack> tracks = new ArrayList<>();

	public VisibilityAnimationResource(VisibilityAnimation anm, Skeleton skl) {
		frameCount = (int) anm.getFrameCountMaxTime();
		name = anm.name;
		for (Joint j : skl.getJoints()) {
			VisibilityBoneTransform bt = (VisibilityBoneTransform) anm.getBoneTransform(j.name);
			if (bt == null) {
				bt = new VisibilityBoneTransform();
				//blank keyframes always result in enabled visibility
			}
			tracks.add(new NodeVisibilityTrack(bt, frameCount));
		}
	}

	@Override
	public byte[] getBytes() throws IOException {
		DataIOStream out = new DataIOStream();
		out.writeStringUnterminated(VAV_MAGIC);
		out.writeShort(frameCount);
		out.writeShort(tracks.size());
		TemporaryValue size = new TemporaryValue(out);

		BitWriter bw = new BitWriter(out);
		
		for (int i = 0; i < frameCount; i++) {
			for (NodeVisibilityTrack t : tracks) {
				t.write(bw, i);
			}
		}
		
		bw.flush();
		out.pad(4); //The NNS VAs are read in blocks of 32 bits

		size.set(out.getPosition());
		
		out.close();
		return out.toByteArray();
	}
}
