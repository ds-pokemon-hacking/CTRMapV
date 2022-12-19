package ctrmap.formats.pokemon.gen5.gfbca;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.camera.CameraBoneTransform;
import ctrmap.renderer.scene.animation.camera.CameraViewpointBoneTransform;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationFrame;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationTransformRequest;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.camcvt.SkeletalMatrixBakery;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import xstandard.math.vec.Matrix4;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GFBCA {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Game Freak Binary Curve Animation", "*.gfbca");

	public int frameCount;
	private boolean hasScale;
	private boolean hasRotation;
	private boolean hasTranslation;

	public List<GFBCAFrame> frames = new ArrayList<>();

	public GFBCA(NTRDataIOStream in) {
		try {
			frameCount = in.readInt();

			hasScale = in.readBoolean();
			hasRotation = in.readBoolean();
			hasTranslation = in.readBoolean();
			in.skipBytes(1);

			for (int i = 0; i < frameCount; i++) {
				frames.add(new GFBCAFrame(in, hasScale, hasRotation, hasTranslation));
			}

			in.close();
		} catch (IOException ex) {
			Logger.getLogger(GFBCA.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public GFBCA(FSFile f) {
		this(new NTRDataIOStream(f.getDataIOStream()));
	}

	public GFBCA(byte[] b) {
		this(new NTRDataIOStream(b));
	}

	public GFBCA(Skeleton skl, SkeletalAnimation anm) {
		if (anm != null) {
			SkeletalBoneTransform bt = anm.bones.get(0);
			Joint j = skl.getJoint(bt.name);
			if (j == null) {
				j = new Joint();
			}
			
			int fc = (int)anm.getFrameCountMaxTime();
			frameCount = fc + 1;
			
			boolean hasNoParentTransform = true;
			
			if (j.parentName != null) {
				Joint parent = j.getParent();
				while (parent != null) {
					if (parent.position.equals(0f, 0f, 0f) && parent.rotation.equals(0f, 0f, 0f) && parent.scale.equals(1f, 1f, 1f)) {
						parent = parent.getParent();
					}
					else {
						hasNoParentTransform = false;
						break;
					}
				}
			}

			if (hasNoParentTransform) {
				//Copy, no baking
				hasScale = bt.hasScale();
				hasRotation = bt.hasRotation();
				hasTranslation = bt.hasTranslation();
				
				SkeletalAnimationTransformRequest req = new SkeletalAnimationTransformRequest(0f);
				req.bindJoint = j;
				req.translation = hasTranslation;
				req.rotation = hasRotation;
				req.scale = hasScale;
				
				for (int i = 0; i <= fc; i++) {
					req.frame = i;
					SkeletalAnimationFrame f = bt.getFrame(req);
					frames.add(GFBCAFrame.createFromSkeletalFrame(f));
					f.free();
				}				
			}
			else {
				SkeletalMatrixBakery bakery = new SkeletalMatrixBakery(anm, skl, j);
				
				hasScale = true;
				hasRotation = true;
				hasTranslation = true;
				
				for (int i = 0; i < fc; i++) {
					Matrix4 bake = bakery.manualBake(i);
					
					frames.add(GFBCAFrame.createFromMatrix(bake));
				}
			}
		}
	}

	public GFBCA(Camera cam, CameraAnimation anm) {
		if (!anm.transforms.isEmpty()) {
			CameraBoneTransform cbt = null;
			for (CameraBoneTransform bt : anm.transforms) {
				if (bt instanceof CameraViewpointBoneTransform) {
					cbt = (CameraViewpointBoneTransform) bt;
					break;
				}
			}
			if (cbt == null) {
				throw new UnsupportedOperationException("Can not convert a non-viewpoint camera to GFBCA.");
			}
			CameraViewpointBoneTransform vp = (CameraViewpointBoneTransform) cbt;

			frameCount = (int) anm.frameCount;
			//full-bake the animation

			Camera bakeCam = new Camera(cam);

			for (int frame = 0; frame < frameCount; frame++) {
				vp.getFrame(frame).applyToCamera(bakeCam);
				frames.add(GFBCAFrame.createFromCameraState(bakeCam));
			}

			hasScale = false;
			hasRotation = true;
			hasTranslation = true;
		}
	}

	public void write(FSFile fsf) {
		try {
			DataOutStream out = fsf.getDataOutputStream();

			out.writeInt(frameCount);
			out.writeBoolean(hasScale);
			out.writeBoolean(hasRotation);
			out.writeBoolean(hasTranslation);
			out.write(0);

			for (GFBCAFrame frm : frames) {
				frm.write(out, hasScale, hasRotation, hasTranslation);
			}

			out.close();
		} catch (IOException ex) {
			Logger.getLogger(GFBCA.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static boolean canConvertGFBCA(CameraAnimation anm) {
		return !anm.transforms.isEmpty() && anm.transforms.get(0) instanceof CameraViewpointBoneTransform;
	}

	public static boolean isGFBCA(FSFile f) {
		return isGFBCA(f.getDataIOStream());
	}

	public static boolean isGFBCA(byte[] b) {
		return isGFBCA(new DataIOStream(b));
	}

	public static boolean isGFBCA(DataIOStream io) {
		return isGFBCA(io, false, true);
	}

	public static boolean isGFBCA(DataIOStream io, boolean seek0, boolean close) {
		boolean result = false;
		try {
			if (seek0) {
				io.seek(0);
			}
			if (io.getLength() >= 8) {
				int fc = io.readInt();
				boolean hasScale = io.readBoolean();
				boolean hasRotation = io.readBoolean();
				boolean hasTranslation = io.readBoolean();
				if (hasRotation || hasTranslation || hasScale) {
					int dataSize = 0;
					dataSize += hasScale ? 12 : 0;
					dataSize += hasRotation ? 12 : 0;
					dataSize += hasTranslation ? 12 : 0;

					if (io.getLength() - 8 == fc * dataSize) {
						result = true;
					}
				}
			}
			if (close) {
				io.close();
			}
		} catch (IOException ex) {
		}
		return result;
	}

	public G3DResource toGenericCameraAnimation() {
		CameraAnimation anm = new CameraAnimation();
		anm.frameCount = frameCount;
		anm.name = "CameraAnimation";

		CameraViewpointBoneTransform bt = new CameraViewpointBoneTransform();
		bt.name = "Camera";

		for (int i = 0; i < frameCount; i++) {
			GFBCAFrame f = frames.get(i);
			if (hasRotation) {
				bt.addVector(i, f.rotation, bt.rx, bt.ry, bt.rz);
			}
			if (hasTranslation) {
				bt.addVector(i, f.translation, bt.tx, bt.ty, bt.tz);
			}
		}

		anm.transforms.add(bt);
		return new G3DResource(anm);
	}

	public G3DResource toGenericSkeletalAnimation() {
		SkeletalAnimation anm = new SkeletalAnimation();
		anm.frameCount = frameCount;
		anm.name = "Motion";

		SkeletalBoneTransform bt = new SkeletalBoneTransform();
		bt.name = "Origin";

		for (int i = 0; i < frameCount; i++) {
			GFBCAFrame f = frames.get(i);
			if (hasRotation) {
				bt.addVectorDegreeAngle(i, f.rotation, bt.rx, bt.ry, bt.rz);
			}
			if (hasTranslation) {
				bt.addVector(i, f.translation, bt.tx, bt.ty, bt.tz);
			}
			if (hasScale) {
				bt.addVector(i, f.scale, bt.sx, bt.sy, bt.sz);
			}
		}

		anm.bones.add(bt);
		return new G3DResource(anm);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("GFBCA\n");

		sb.append("FrameCount: ");
		sb.append(frameCount);
		sb.append("\n\n");

		for (int i = 0; i < frameCount; i++) {
			GFBCAFrame f = frames.get(i);
			sb.append("Frame ");
			sb.append(i);
			sb.append("\n");
			if (hasRotation) {
				sb.append("ROT: ");
				sb.append(f.rotation);
				sb.append("\n");
			}
			if (hasTranslation) {
				sb.append("TRA: ");
				sb.append(f.translation);
				sb.append("\n");
			}
			sb.append("\n");
		}

		return sb.toString();
	}
}
