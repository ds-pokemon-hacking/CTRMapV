
package ctrmap.formats.pokemon.gen5.sequence;

/**
 *
 */
public enum SeqOpCode {
	CMD_NULL,
	CMD_SE_PLAY, //params SEId, volume, pan, speedIncrement, channel
	CMD_SE_STOP, //params SEid, channel
	CMD_SE_ANIM_VOLUME, //params SEid (for deciding channel no. if not specified), startValue, endValue, duration, channel
	CMD_SE_ANIM_PAN, //same as ^
	CMD_SE_ANIM_SPEED, //same as ^
	CMD_BGM_PLAY, //param BGMId
	CMD_BGM_STOP, //stops any playing BGM, no parameters
	CMD_BGM_FADE, //params isFadeOut, duration
	CMD_BGM_CHANGE, //params destBgmId, fadeOutDuration, fadeInDuration, fadeType
	CMD_SCREEN_FADE, //params screen, duration, srcColor, fadeColor (-16 to 16, black to transparent to white)
	CMD_SCREEN_FADE_EX, //params screen, srcColor, fadeColor, dstColor, fadeOutDuration, fadeInDuration
	CMD_LIGHT_COLOR_CHANGE, //params lightId, r, g, b
	CMD_LIGHT_VECTOR_CHANGE, //params lightId, x, y, z
	CMD_G2D_MESSAGE, //params TextFile (in game texts), MsgID, MsgIdIncrementPerUserParam, Duration, X, Y, Width, Height
	CMD_G2D_CELLGRA, //params gameFlag, palID, graID, scrID, fadeInDuration, stayDuration, fadeOutDuration
	CMD_MOTION_BLUR_BEGIN, //intensityTop, intensityBottom
	CMD_MOTION_BLUR_END, //ends motion blur, no parameters
	CMD_CAMERA_SET_PROJECTION, //params FOVX, FOVY, TopScreenCamOffs, BottomScreenCamOffs
	CMD_END //terminates the command stream, must be present in all cases (unless the sequence has 9999 commands)
}
