
package ctrmap.formats.qos;

import xstandard.io.serialization.annotations.typechoice.TypeChoiceInt;
import ctrmap.formats.qos.g3d.*;

@TypeChoiceInt(key = 0x8FF8AE93, value = GeminiObject.class)
@TypeChoiceInt(key = 0xF7ED313C, value = GeminiFile.class)
@TypeChoiceInt(key = 0x9010F703, value = GeminiFile.AssetIdentifier.class)
@TypeChoiceInt(key = 0x981BB988, value = GeminiFile.ArrayIdentifier.class)
@TypeChoiceInt(key = 0xFB833FEA, value = GeminiModelStub.class)
@TypeChoiceInt(key = 0xE5B66C3D, value = GeminiModel.class)
@TypeChoiceInt(key = 0xC3B634C0, value = GeminiAttachment.class)
@TypeChoiceInt(key = 0xCB2D29EA, value = GeminiAnimatedValue.class)
@TypeChoiceInt(key = 0xB361CE36, value = GeminiTextureInstance.class)
@TypeChoiceInt(key = 0xC27B34DA, value = GeminiTexture.class)
@TypeChoiceInt(key = 0xD960513B, value = GeminiFileReference.class)
@TypeChoiceInt(key = 0xE089A1DA, value = GeminiExtraData.class)
@TypeChoiceInt(key = 0xD9394213, value = GeminiLight.class)
@TypeChoiceInt(key = 0xAB3D1B04, value = GeminiStaticLight.class)
@TypeChoiceInt(key = 0xA168175B, value = GeminiAnimationStub.class)
@TypeChoiceInt(key = 0xEB5AF13E, value = GeminiScene.class)
@TypeChoiceInt(key = 0x9C49EB7A, value = GeminiCamera.class)
@TypeChoiceInt(key = 0x99E5D9AD, value = GeminiRegionSet.class)
@TypeChoiceInt(key = 0xED9D27B3, value = GeminiRegionTypeData.class)
@TypeChoiceInt(key = 0x8A0E4335, value = GeminiRegionData.class)
@TypeChoiceInt(key = 0xEB3C8208, value = GeminiCompressedAnimationS8KeyChannel.class)
@TypeChoiceInt(key = 0x8A36614F, value = GeminiCompressedAnimationS16KeyChannel.class)
@TypeChoiceInt(key = 0xA032D83D, value = GeminiCompressedAnimationChannelBase.class)
@TypeChoiceInt(key = 0x8BF9F171, value = GeminiAnimationChannel.class)
@TypeChoiceInt(key = 0x987A053E, value = GeminiCompressedAnimation.class)
@TypeChoiceInt(key = 0xB489FA28, value = GeminiAnimation.class)
@TypeChoiceInt(key = 0x8A737648, value = GeminiCompressedAnimationStub.class)
public class GeminiObject {

}
