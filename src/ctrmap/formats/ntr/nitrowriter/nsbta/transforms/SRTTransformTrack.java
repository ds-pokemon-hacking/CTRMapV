package ctrmap.formats.ntr.nitrowriter.nsbta.transforms;

import ctrmap.formats.ntr.nitrowriter.nsbta.transforms.elements.SRTTransformElement;
import ctrmap.formats.ntr.common.FX;
import static ctrmap.formats.ntr.nitrowriter.nsbta.SRTAnimationResource.setFlagIf;
import ctrmap.formats.ntr.nitrowriter.nsbta.transforms.elements.SRTFX16Elem;
import ctrmap.formats.ntr.nitrowriter.nsbta.transforms.elements.SRTFX32Elem;
import ctrmap.renderer.scene.animation.material.MaterialAnimationFrame;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SRTTransformTrack {
	
	protected final boolean constant;
	protected float constValue;
	protected boolean isFX16 = true;
	
	private SRTTrackType type;
	protected List<SRTTransformElement> elements = new ArrayList<>();
	
	protected SRTTransformTrack(SRTTrackType type, boolean constant){
		this.type = type;
		this.constant = constant;
	}
	
	public SRTTransformTrack(SRTTrackType type, List<MaterialAnimationFrame> bakedAnimation, boolean constant){
		this(type, constant);
		
		if (constant){
			constValue = getValueByCompType(bakedAnimation.get(0));
			isFX16 = FX.allowFX16(constValue);
		}
		else {
			constValue = 0f;

			for (MaterialAnimationFrame f : bakedAnimation){
				if (!FX.allowFX16(getValueByCompType(f))){
					isFX16 = false;
					break;
				}
			}
			
			for (MaterialAnimationFrame f : bakedAnimation){
				if (isFX16){
					elements.add(new SRTFX16Elem(getValueByCompType(f)));
				}
				else {
					elements.add(new SRTFX32Elem(getValueByCompType(f)));
				}
			}
		}
	}
	
	public List<SRTTransformElement> getElements(){
		return elements;
	}
	
	public TemporaryOffset writeTrackInfo(DataIOStream out) throws IOException {
		int flags = setFlagIf(0, MaterialCoordAnimationFlags.TRACK_IS_CONSTANT, constant);
		flags = setFlagIf(flags, MaterialCoordAnimationFlags.TRACK_ENC_IS_FX16, isFX16);
		out.writeInt(flags);
		if (constant){
			out.writeInt(FX.fx32(constValue)); //can be FX16, but is always 4 bytes
		}
		else {
			return new TemporaryOffset(out);
		}
		return null;
	}
	
	private float getValueByCompType(MaterialAnimationFrame frm){
		switch (type){
			case R:
				return frm.r.value;
			case SX:
				return frm.sx.value;
			case SY:
				return frm.sy.value;
			case TX:
				return frm.tx.value;
			case TY:
				return frm.ty.value;
		}
		return 0f;
	}
	
	public static enum SRTTrackType {
		SX,
		SY,
		TX,
		TY,
		R
	}
}
