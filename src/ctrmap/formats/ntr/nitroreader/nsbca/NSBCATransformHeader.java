package ctrmap.formats.ntr.nitroreader.nsbca;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCARotationTrack;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCARotationConst;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCARotationCurve;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCAScaleTrack;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCAScaleConst;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCAScaleCurve;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCATranslationTrack;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCATranslationConst;
import ctrmap.formats.ntr.nitroreader.nsbca.tracks.NSBCATranslationCurve;
import java.io.IOException;
import xstandard.util.EnumBitflagsInt;

public class NSBCATransformHeader {
	
	public int jointId;

	public boolean isBindPoseS;
	public boolean isBindPoseR;
	public boolean isBindPoseT;

	public NSBCARotationTrack r = new NSBCARotationConst();
	public NSBCAScaleTrack sx = new NSBCAScaleConst();
	public NSBCAScaleTrack sy = new NSBCAScaleConst();
	public NSBCAScaleTrack sz = new NSBCAScaleConst();
	public NSBCATranslationTrack tx = new NSBCATranslationConst();
	public NSBCATranslationTrack ty = new NSBCATranslationConst();
	public NSBCATranslationTrack tz = new NSBCATranslationConst();

	private NSBCAScaleTrack getJointAnmScale(NTRDataIOStream data, boolean isConst) throws IOException {
		if (isConst) {
			return new NSBCAScaleConst(data);
		}
		return new NSBCAScaleCurve(data);
	}

	private NSBCARotationTrack getJointAnmRot(NTRDataIOStream data, boolean isConst) throws IOException {
		if (isConst) {
			return new NSBCARotationConst(data);
		}
		return new NSBCARotationCurve(data);
	}

	private NSBCATranslationTrack getJointAnmTrans(NTRDataIOStream data, boolean isConst) throws IOException {
		if (isConst) {
			return new NSBCATranslationConst(data);
		}
		return new NSBCATranslationCurve(data);
	}

	public NSBCATransformHeader(NTRDataIOStream in) throws IOException {
		int header = in.readInt();
		jointId = header >>> 24;
		EnumBitflagsInt<NSBCATransformFlag> flags = new EnumBitflagsInt(NSBCATransformFlag.class, header & 0xFFFFFF);
		if (!flags.isSet(NSBCATransformFlag.IDENTITY)) {
			isBindPoseS = flags.isSet(NSBCATransformFlag.BIND_POSE_S);
			isBindPoseR = flags.isSet(NSBCATransformFlag.BIND_POSE_R);
			isBindPoseT = flags.isSet(NSBCATransformFlag.BIND_POSE_T);
			if (!flags.isSetAny(NSBCATransformFlag.BIND_POSE_T, NSBCATransformFlag.IDENTITY_T)) {
				tx = getJointAnmTrans(in, flags.isSet(NSBCATransformFlag.CONST_TX));
				ty = getJointAnmTrans(in, flags.isSet(NSBCATransformFlag.CONST_TY));
				tz = getJointAnmTrans(in, flags.isSet(NSBCATransformFlag.CONST_TZ));
			}
			if (!flags.isSetAny(NSBCATransformFlag.BIND_POSE_R, NSBCATransformFlag.IDENTITY_R)) {
				r = getJointAnmRot(in, flags.isSet(NSBCATransformFlag.CONST_R));
			}
			if (!flags.isSetAny(NSBCATransformFlag.BIND_POSE_T, NSBCATransformFlag.IDENTITY_S)) {
				sx = getJointAnmScale(in, flags.isSet(NSBCATransformFlag.CONST_SX));
				sy = getJointAnmScale(in, flags.isSet(NSBCATransformFlag.CONST_SY));
				sz = getJointAnmScale(in, flags.isSet(NSBCATransformFlag.CONST_SZ));
			}
		}
	}

	public enum NSBCATransformFlag {
		IDENTITY,
		IDENTITY_T,
		BIND_POSE_T,
		CONST_TX,
		CONST_TY,
		CONST_TZ,
		IDENTITY_R,
		BIND_POSE_R,
		CONST_R,
		IDENTITY_S,
		BIND_POSE_S,
		CONST_SX,
		CONST_SY,
		CONST_SZ;
	}
}
