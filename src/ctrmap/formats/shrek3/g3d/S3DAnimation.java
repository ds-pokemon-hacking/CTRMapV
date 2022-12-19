package ctrmap.formats.shrek3.g3d;

import ctrmap.formats.ntr.common.NDSDeserializer;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.serialization.annotations.MagicStrLE;
import xstandard.io.serialization.annotations.typechoice.TypeChoiceStr;

@MagicStrLE
@TypeChoiceStr(key = "unco", value = S3DAnimationUncomp.class)
@TypeChoiceStr(key = "comp", value = S3DAnimationComp.class)
public class S3DAnimation {
	
	public static S3DAnimation fromFile(FSFile f) {
		return new NDSDeserializer().deserializeFile(S3DAnimation.class, f);
	}
	
	public static void main(String[] args) {
		DiskFile srcFile = new DiskFile("D:\\Emugames\\DS\\1051 - Shrek the Third (USA)_Extracted\\data_uncomp\\gob\\0ca751c3.animation.bin");
		S3DAnimation a = fromFile(srcFile);
		YamlReflectUtil.serializeObjectAsYml(a).writeToFile(srcFile.getParent().getChild(srcFile.getNameWithoutExtension() + ".yml"));
	}
}
