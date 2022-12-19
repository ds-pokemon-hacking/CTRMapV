package ctrmap.formats.ntr.nitroreader.nsbtp;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.util.List;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import java.io.IOException;
import java.util.ArrayList;

public class NSBTPTrack {
	private String materialName;
    private List<NSBTPKeyframe> keyframes = new ArrayList<>();

    public NSBTPTrack(NTRDataIOStream data, int keyframeCount, String matName, List<String> textures, List<String> palettes) throws IOException {
        this.materialName = matName;
        for (int i = 0; i < keyframeCount; i++) {
            this.keyframes.add(new NSBTPKeyframe(data.readUnsignedShort(), textures.get(data.read()), palettes.get(data.read())));
        }
    }

	public MatAnimBoneTransform toGeneric(){
		MatAnimBoneTransform bt = new MatAnimBoneTransform();
		bt.name = materialName;
		
		for (NSBTPKeyframe kf : keyframes){
			int idx = bt.textureNames.indexOf(kf.textureName);
			if (idx == -1){
				idx = bt.textureNames.size();
				bt.textureNames.add(kf.textureName);
			}
			bt.textureIndices[0].add(new KeyFrame(kf.frame, idx));
		}
		
		return bt;
	}
}
