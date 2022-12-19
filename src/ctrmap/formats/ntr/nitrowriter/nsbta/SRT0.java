package ctrmap.formats.ntr.nitrowriter.nsbta;

import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DDataBlockBase;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;

/**
 *
 */
public class SRT0 extends NNSG3DDataBlockBase {

	public SRT0(NNSWriterLogger log, MaterialAnimation... animations) {
		super("SRT0");
		if (log == null) {
			log = new NNSWriterLogger.DummyLogger();
		}
		int animeCount = 0;
		for (MaterialAnimation a : animations) {
			if (animeCount < 255) {
				tree.addResource(new SRTAnimationResource(log, a));
				animeCount++;
			} else {
				log.err("Too many animations! Omitting " + a.name);
			}
		}
	}
}
