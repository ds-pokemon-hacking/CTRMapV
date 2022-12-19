package ctrmap.formats.ntr.nitrowriter.nsbta;

import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSPatriciaTreeWriter;
import ctrmap.formats.ntr.nitrowriter.nsbta.transforms.SRT0Transform;
import ctrmap.formats.ntr.nitrowriter.nsbta.transforms.SRTTransformTrack;
import ctrmap.formats.ntr.nitrowriter.nsbta.transforms.elements.SRTTransformElement;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.util.StringIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SRTAnimationResource extends NNSG3DResource {

	public static final String MAT_MAGIC = "M\u0000AT";

	public int frameCount;
	private List<SRT0Transform> transforms = new ArrayList<>();

	public SRTAnimationResource(NNSWriterLogger log, MaterialAnimation anm) {
		name = anm.name;
		frameCount = (int) anm.getFrameCountMaxTime();

		int transformCount = 0;
		for (MatAnimBoneTransform bt : anm.bones) {
			if (bt.hasCoordinator(0)) {
				if (transformCount < 255) {
					//crappy DS GPU only supports one texture per material
					transforms.add(new SRT0Transform(bt, frameCount));
					transformCount++;
				} else {
					log.err("Too many transforms! Omitting " + bt.name);
				}
			}
		}
	}

	@Override
	public byte[] getBytes() throws IOException {
		DataIOStream out = new DataIOStream();
		out.writeStringUnterminated(MAT_MAGIC);
		out.writeShort(frameCount);
		out.write(0);
		out.write(0); //flags and matrix mode, both 0 on bg_b01.nsbta

		NNSPatriciaTreeWriter.writeNNSPATRICIATree(out, transforms, SRT0Transform.BYTES);

		Map<TemporaryOffset, SRTTransformTrack> pendingTracks = new HashMap<>();

		for (SRT0Transform t : transforms) {
			pendingTracks.put(t.sx.writeTrackInfo(out), t.sx);
			pendingTracks.put(t.sy.writeTrackInfo(out), t.sy);
			pendingTracks.put(t.r.writeTrackInfo(out), t.r);
			pendingTracks.put(t.tx.writeTrackInfo(out), t.tx);
			pendingTracks.put(t.ty.writeTrackInfo(out), t.ty);
		}

		for (SRT0Transform t : transforms) {
			StringIO.writePaddedString(out, t.name, 16);
		}

		pendingTracks.remove(null);

		out.pad(16);
		for (Map.Entry<TemporaryOffset, SRTTransformTrack> e : pendingTracks.entrySet()) {
			e.getKey().setHere();
			for (SRTTransformElement elem : e.getValue().getElements()) {
				elem.write(out);
			}
			out.pad(4);
		}

		return out.toByteArray();
	}

	public static int setFlagIf(int flags, int flag, boolean value) {
		if (value) {
			flags |= flag;
		}
		return flags;
	}
}
