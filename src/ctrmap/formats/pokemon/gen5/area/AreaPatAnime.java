package ctrmap.formats.pokemon.gen5.area;

import ctrmap.formats.pokemon.gen5.gfbtp.GFBTP;
import ctrmap.formats.ntr.nitroreader.nsbtx.NSBTX;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.texture.TextureConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class AreaPatAnime {

	private GFBTP btp;
	private NSBTX btx;

	public AreaPatAnime(GFBTP btp, NSBTX btx) {
		this.btp = btp;
		this.btx = btx;
	}

	public G3DResource toGeneric(List<Material> worldMaterials) {
		G3DResource res = new G3DResource();

		MaterialAnimation anm = new MaterialAnimation();
		anm.name = "AreaPatAnime";
		anm.frameCount = btp.frameCount;

		for (GFBTP.GFBTPKeyFrameGroup grp : btp.groups) {

			Map<String, MatAnimBoneTransform> transforms = new HashMap<>();

			List<AreaTexKeyFrame> kfs = new ArrayList<>();
			Map<String, List<String>> palettesPerTexture = new HashMap<>();

			for (GFBTP.KeyFrame kf : grp.keyFrames) {
				AreaTexKeyFrame akf = new AreaTexKeyFrame(kf, btx);
				List<String> ppt = palettesPerTexture.getOrDefault(akf.texPlttRef.texture, new ArrayList<>());
				if (!ppt.contains(akf.texPlttRef.palette)) {
					ppt.add(akf.texPlttRef.palette);
				}
				if (!palettesPerTexture.containsKey(akf.texPlttRef.texture)) {
					palettesPerTexture.put(akf.texPlttRef.texture, ppt);
				}

				kfs.add(akf);

				for (Material mat : worldMaterials) {
					if (mat.hasTextureName(akf.texPlttRef.texture) && !transforms.containsKey(mat.name)) {
						MatAnimBoneTransform bt = new MatAnimBoneTransform();
						bt.name = mat.name;
						transforms.put(mat.name, bt);
					}
				}
			}

			Map<AreaTexKeyFrame.TextureReference, Texture> textures = new HashMap<>();
			List<String> btTextureNames = new ArrayList<>();

			for (AreaTexKeyFrame kf : kfs) {
				String targetTextureName
					= palettesPerTexture.get(kf.texPlttRef.texture).size() > 1
					? kf.texPlttRef.texture + "_" + kf.texPlttRef.palette
					: kf.texPlttRef.texture;

				int texIdx = btTextureNames.indexOf(targetTextureName);
				if (texIdx == -1) {
					texIdx = btTextureNames.size();
					btTextureNames.add(targetTextureName);
				}

				for (MatAnimBoneTransform bt : transforms.values()) {
					bt.textureIndices[0].add(new KeyFrame(kf.frame, texIdx));
				}

				if (!textures.containsKey(kf.texPlttRef)) {
					Texture tex = btx.TEX0.getConvTexture(
						kf.texPlttRef.texture,
						kf.texPlttRef.palette
					);
					if (tex != null) {
						textures.put(kf.texPlttRef, tex);

						res.addTexture(tex);
					}
				}
			}

			for (MatAnimBoneTransform bt : transforms.values()) {
				bt.textureNames.addAll(btTextureNames);
				anm.bones.add(bt);
			}
		}

		res.addMatAnime(anm);

		return res;
	}

	public static class AreaTexKeyFrame {

		public int frame;
		public TextureReference texPlttRef;

		public AreaTexKeyFrame(GFBTP.KeyFrame btpKey, NSBTX btx) {
			frame = btpKey.frame;
			texPlttRef = new TextureReference(btpKey.texIdx, btpKey.palIdx, btx);
		}

		public static class TextureReference {

			public String texture;
			public String palette;

			public TextureReference(int tex, int pal, NSBTX btx) {
				texture = btx.TEX0.textures.get(tex).name;
				palette = btx.TEX0.palettes.get(pal).name;
			}

			@Override
			public int hashCode() {
				int hash = 3;
				hash = 31 * hash + Objects.hashCode(this.texture);
				hash = 31 * hash + Objects.hashCode(this.palette);
				return hash;
			}
		}
	}
}
