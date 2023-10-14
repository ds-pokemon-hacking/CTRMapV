package ctrmap.formats.ntr.nitrowriter.nsbca;

import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;
import ctrmap.formats.ntr.nitrowriter.common.settings.AnimationImportSettingsBase;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalAnimationFlags;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.BakedTransform;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.TransformFrame;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.RotationTrack;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.elements.Rot3x3CompactElem;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.rot.elements.RotationElement;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.sca.ScaleTrack;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.SkeletalTransformComponent;
import ctrmap.formats.ntr.nitrowriter.nsbca.transforms.tra.TranslationTrack;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationFrame;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.util.AnimeProcessor;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.structs.TemporaryOffsetShort;
import xstandard.io.structs.TemporaryValue;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Matrix3f;

public class JointAnimationResource extends NNSG3DResource {

	public static final String JAC_MAGIC = "J\u0000AC";

	private short frameCount;
	private List<BakedTransform> transforms = new ArrayList<>();

	public JointAnimationResource(SkeletalAnimation anm, Skeleton skl, AnimationImportSettingsBase importSettings) {
		name = anm.name;
		frameCount = (short) (anm.getFrameCountMaxTime() * (importSettings.sampleRate / 30f));
		for (Joint j : skl.getJoints()) {
			SkeletalBoneTransform bt = (SkeletalBoneTransform) anm.getBoneTransform(j.name);
			if (bt != null) {
				transforms.add(createBakedTransform(bt, j, frameCount, importSettings));
			}
		}
	}

	private static BakedTransform createBakedTransform(SkeletalBoneTransform bt, Joint j, short fcount, AnimationImportSettingsBase settings) {
		boolean isRotConst = AnimeProcessor.checkConstant(bt.rx, bt.ry, bt.rz);
		boolean isSxConst = AnimeProcessor.checkConstant(bt.sx);
		boolean isSyConst = AnimeProcessor.checkConstant(bt.sy);
		boolean isSzConst = AnimeProcessor.checkConstant(bt.sz);
		boolean isTxConst = AnimeProcessor.checkConstant(bt.tx);
		boolean isTyConst = AnimeProcessor.checkConstant(bt.ty);
		boolean isTzConst = AnimeProcessor.checkConstant(bt.tz);

		List<TransformFrame> l = new ArrayList<>();

		boolean isConstInit = false;
		Matrix3f constR = null;
		float ctx = 0f;
		float cty = 0f;
		float ctz = 0f;
		float csx = 0f;
		float csy = 0f;
		float csz = 0f;

		for (int i = 0; i <= fcount; i++) {
			float frame = i / (settings.sampleRate / 30f);
			SkeletalAnimationFrame f = bt.getFrame(frame, j, true);

			if (!isConstInit) {
				if (isSxConst) {
					csx = f.sx.value;
				}
				if (isSyConst) {
					csy = f.sy.value;
				}
				if (isSzConst) {
					csz = f.sz.value;
				}
				if (isRotConst) {
					constR = f.getRotationMatrix();
				}
				if (isTxConst) {
					ctx = f.tx.value;
				}
				if (isTyConst) {
					cty = f.ty.value;
				}
				if (isTzConst) {
					ctz = f.tz.value;
				}
				isConstInit = true;
			}

			TransformFrame tf = new TransformFrame();
			tf.tx = isTxConst ? ctx : f.tx.value;
			tf.ty = isTyConst ? cty : f.ty.value;
			tf.tz = isTzConst ? ctz : f.tz.value;
			tf.sx = isSxConst ? csx : f.sx.value;
			tf.sy = isSyConst ? csy : f.sy.value;
			tf.sz = isSzConst ? csz : f.sz.value;
			tf.rotation = isRotConst ? constR : f.getRotationMatrix();

			f.free();

			l.add(tf);
		}

		BakedTransform t = new BakedTransform(j.getIndex());
		t.rotation = new RotationTrack(l, isRotConst);
		t.tx = new TranslationTrack(l, SkeletalTransformComponent.TX, isTxConst);
		t.ty = new TranslationTrack(l, SkeletalTransformComponent.TY, isTyConst);
		t.tz = new TranslationTrack(l, SkeletalTransformComponent.TZ, isTzConst);
		t.sx = new ScaleTrack(l, SkeletalTransformComponent.SX, isSxConst);
		t.sy = new ScaleTrack(l, SkeletalTransformComponent.SY, isSyConst);
		t.sz = new ScaleTrack(l, SkeletalTransformComponent.SZ, isSzConst);

		if (bt.rx.isEmpty() && bt.ry.isEmpty() && bt.rz.isEmpty()) {
			t.rotation.setIsBindPose();
		}
		if (bt.tx.isEmpty()) {
			t.tx.setIsBindPose();
		}
		if (bt.ty.isEmpty()) {
			t.ty.setIsBindPose();
		}
		if (bt.tz.isEmpty()) {
			t.tz.setIsBindPose();
		}
		if (bt.sx.isEmpty()) {
			t.sx.setIsBindPose();
		}
		if (bt.sy.isEmpty()) {
			t.sy.setIsBindPose();
		}
		if (bt.sz.isEmpty()) {
			t.sz.setIsBindPose();
		}

		return t;
	}

	public byte[] getJ0CABlock() throws IOException {
		DataIOStream out = new DataIOStream();
		out.writeStringUnterminated(JAC_MAGIC);
		out.writeShort(frameCount);
		out.writeShort(transforms.size());
		out.writeInt(0);
		TemporaryOffset axisRotOffset = new TemporaryOffset(out);
		TemporaryOffset m3x3RotOffset = new TemporaryOffset(out);

		List<TemporaryOffsetShort> nodeOffsets = new ArrayList<>();
		for (int i = 0; i < transforms.size(); i++) {
			nodeOffsets.add(new TemporaryOffsetShort(out));
		}

		out.pad(4);

		Map<TemporaryOffset, ScaleTrack> pendingScaleData = new HashMap<>();
		Map<TemporaryOffset, TranslationTrack> pendingTranslationData = new HashMap<>();
		Map<TemporaryValue, RotationTrack> pendingConstantRotationData = new HashMap<>();
		Map<TemporaryOffset, RotationTrack> pendingVariableRotationData = new HashMap<>();

		List<RotationElement> rot3Elements = new ArrayList<>();
		List<RotationElement> rot5Elements = new ArrayList<>();

		for (int i = 0; i < transforms.size(); i++) {
			BakedTransform t = transforms.get(i);

			int flags = 0;
			flags = setFlagIf(flags, SkeletalAnimationFlags.TRANSFORM_SCALE_IS_BIND_POSE, t.isBindS());
			flags = setFlagIf(flags, SkeletalAnimationFlags.TRANSFORM_ROTATION_IS_BIND_POSE, t.isBindR());
			flags = setFlagIf(flags, SkeletalAnimationFlags.TRANSFORM_TRANSLATION_IS_BIND_POSE, t.isBindT());

			if (!t.isBindS()) {
				flags = setFlagIf(flags, SkeletalAnimationFlags.TRANSFORM_SCALE_IS_CONSTANT_X, t.sx.isConstant);
				flags = setFlagIf(flags, SkeletalAnimationFlags.TRANSFORM_SCALE_IS_CONSTANT_Y, t.sy.isConstant);
				flags = setFlagIf(flags, SkeletalAnimationFlags.TRANSFORM_SCALE_IS_CONSTANT_Z, t.sz.isConstant);
			}
			if (!t.isBindR()) {
				flags = setFlagIf(flags, SkeletalAnimationFlags.TRANSFORM_ROTATION_IS_CONSTANT, t.rotation.isConstant);
			}
			if (!t.isBindT()) {
				flags = setFlagIf(flags, SkeletalAnimationFlags.TRANSFORM_TRANSLATION_IS_CONSTANT_X, t.tx.isConstant);
				flags = setFlagIf(flags, SkeletalAnimationFlags.TRANSFORM_TRANSLATION_IS_CONSTANT_Y, t.ty.isConstant);
				flags = setFlagIf(flags, SkeletalAnimationFlags.TRANSFORM_TRANSLATION_IS_CONSTANT_Z, t.tz.isConstant);
			}
			flags |= (t.jointId << 24);

			//Write header
			nodeOffsets.get(i).setHere();
			out.writeInt(flags);

			//Now write the actual transform in accordance with the header
			if (!t.isBindT()) {
				pendingTranslationData.put(writeTranslationTrack(out, t.tx), t.tx);
				pendingTranslationData.put(writeTranslationTrack(out, t.ty), t.ty);
				pendingTranslationData.put(writeTranslationTrack(out, t.tz), t.tz);
			}
			if (!t.isBindR()) {
				if (t.rotation.isConstant) {
					pendingConstantRotationData.put(new TemporaryValue(out), t.rotation);
				} else {
					out.writeInt(t.rotation.getInfo());
					pendingVariableRotationData.put(new TemporaryOffset(out), t.rotation);
				}
			}
			if (!t.isBindS()) {
				pendingScaleData.put(writeScaleTrack(out, t.sx), t.sx);
				pendingScaleData.put(writeScaleTrack(out, t.sy), t.sy);
				pendingScaleData.put(writeScaleTrack(out, t.sz), t.sz);
			}
		}

		pendingConstantRotationData.remove(null);
		pendingVariableRotationData.remove(null);
		pendingScaleData.remove(null);
		pendingTranslationData.remove(null);

		out.pad(16);

		out.writeStringUnterminated("SCALE");
		out.pad(16);

		for (Map.Entry<TemporaryOffset, ScaleTrack> st : pendingScaleData.entrySet()) {
			st.getKey().setHere();
			st.getValue().writeElements(out);
			out.pad(4);
		}

		out.pad(16);
		out.writeStringUnterminated("TRANSLATION");
		out.pad(16);

		for (Map.Entry<TemporaryOffset, TranslationTrack> tt : pendingTranslationData.entrySet()) {
			tt.getKey().setHere();
			tt.getValue().writeElements(out);
			out.pad(4);
		}

		for (Map.Entry<TemporaryValue, RotationTrack> rt : pendingConstantRotationData.entrySet()) {
			RotationElement e = rt.getValue().getElements().get(0);
			List<RotationElement> elemList = null;
			int offset;
			if (e instanceof Rot3x3CompactElem) {
				offset = 0;
				elemList = rot5Elements;
			} else {
				offset = 0x8000;
				elemList = rot3Elements;
			}
			if (elemList == null) {
				throw new NullPointerException("Bad rotation element class !?!?!?!?");
			}
			offset |= elemList.size();
			elemList.add(e);
			rt.getKey().set(offset);
		}

		out.pad(16);
		out.writeStringUnterminated("ROTATION-VAR-IDX");
		out.pad(16);
		for (Map.Entry<TemporaryOffset, RotationTrack> rt : pendingVariableRotationData.entrySet()) {
			rt.getKey().setHere();
			for (RotationElement e : rt.getValue().getElements()) {
				List<RotationElement> elemList = null;
				int offset;
				if (e instanceof Rot3x3CompactElem) {
					offset = 0;
					elemList = rot5Elements;
				} else {
					offset = 0x8000;
					elemList = rot3Elements;
				}
				if (elemList == null) {
					throw new NullPointerException("Bad rotation element class !?!?!?!?");
				}
				int index = elemList.indexOf(e);
				if (index == -1) {
					index = elemList.size();
					elemList.add(e);
				}
				if (index > 0x7FFF) {
					System.err.println("Rotation matrix index out of bounds! - " + index);
				}
				offset |= index;
				out.writeShort(offset);
			}
			out.pad(4);
		}

		out.pad(16);
		out.writeStringUnterminated("ROTATION-MTX3");
		out.pad(16);
		axisRotOffset.setHere();
		writeRotElements(out, rot3Elements);

		out.pad(16);
		out.writeStringUnterminated("ROTATION-MTX5");
		out.pad(16);
		m3x3RotOffset.setHere();
		writeRotElements(out, rot5Elements);
		//System.out.println("wrote " + rot3Elements.size() + " compact rotations, + " + rot5Elements.size() + " matrices");
		//System.out.println("wrote " + transforms.size() + " transforms.");

		out.pad(16);
		return out.toByteArray();
	}

	private static void writeRotElements(DataOutput io, List<RotationElement> elems) throws IOException {
		for (RotationElement e : elems) {
			e.write(io);
		}
	}

	private static TemporaryOffset writeScaleTrack(DataIOStream io, ScaleTrack st) throws IOException {
		if (st.isConstant) {
			st.writeElements(io);
			return null;
		} else {
			int info = st.getInfo();
			io.writeInt(info);
			TemporaryOffset ofs = new TemporaryOffset(io);
			return ofs;
		}
	}

	private static TemporaryOffset writeTranslationTrack(DataIOStream io, TranslationTrack tt) throws IOException {
		if (tt.isConstant) {
			tt.writeElements(io);
			return null;
		} else {
			int info = tt.getInfo();
			io.writeInt(info);
			TemporaryOffset ofs = new TemporaryOffset(io);
			return ofs;
		}
	}

	public static int setFlagIf(int flags, int flag, boolean value) {
		if (value) {
			flags |= flag;
		}
		return flags;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return getJ0CABlock();
	}
}
