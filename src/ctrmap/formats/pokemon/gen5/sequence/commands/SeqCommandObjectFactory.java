package ctrmap.formats.pokemon.gen5.sequence.commands;

import xstandard.formats.yaml.YamlListElement;
import xstandard.formats.yaml.YamlNode;
import xstandard.formats.yaml.YamlReflectUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.formats.pokemon.gen5.sequence.SeqCmd;
import ctrmap.formats.pokemon.gen5.sequence.SeqOpCode;

public class SeqCommandObjectFactory {

	private static final Map<SeqOpCode, Class<? extends SeqCommandObject>> SEQCMDOBJ_CLASSES = new HashMap<>();

	static {
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_NULL, CommandNull.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_SE_PLAY, CommandSEPlay.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_SE_STOP, CommandSEStop.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_SE_ANIM_VOLUME, CommandSEAnimate.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_SE_ANIM_PAN, CommandSEAnimate.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_SE_ANIM_SPEED, CommandSEAnimate.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_BGM_PLAY, CommandBGMPlay.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_BGM_STOP, CommandBGMStop.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_BGM_FADE, CommandBGMFade.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_BGM_CHANGE, CommandBGMChange.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_SCREEN_FADE, CommandScrFade.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_SCREEN_FADE_EX, CommandScrFadeEx.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_LIGHT_COLOR_CHANGE, CommandLightColorChange.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_LIGHT_VECTOR_CHANGE, CommandLightVectorChange.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_G2D_MESSAGE, CommandG2DMessage.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_G2D_CELLGRA, CommandG2DCellGra.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_MOTION_BLUR_BEGIN, CommandMotionBlurBegin.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_MOTION_BLUR_END, CommandMotionBlurEnd.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_CAMERA_SET_PROJECTION, CommandCameraSetProjection.class);
		SEQCMDOBJ_CLASSES.put(SeqOpCode.CMD_END, CommandEnd.class);
	}

	public static SeqCommandObject createSeqCommandObject(SeqCmd cmd) {
		try {
			Class<? extends SeqCommandObject> cls = SEQCMDOBJ_CLASSES.get(cmd.opCode);

			SeqCommandObject obj = cls.newInstance();
			obj.startFrame = cmd.startFrame;
			obj.setOpCode(cmd.opCode);

			int userDataIndex = 0;
			for (Field field : cls.getFields()) {
				int mods = field.getModifiers();
				if (!Modifier.isStatic(mods) && !Modifier.isTransient(mods)) {
					int userDataValue = cmd.userData[userDataIndex];
					Class fieldType = field.getType();

					if (fieldType.isEnum()) {
						field.set(obj, fieldType.getEnumConstants()[userDataIndex]);
					} else if (fieldType == Integer.TYPE) {
						field.set(obj, userDataValue);
					} else if (fieldType == Float.TYPE) {
						field.set(obj, userDataValue / 4096f);
					} else {
						throw new UnsupportedOperationException("Non-deserializable field type! - " + field);
					}
					userDataIndex++;
				}
			}
			return obj;
		} catch (InstantiationException | IllegalAccessException ex) {
			Logger.getLogger(SeqCommandObjectFactory.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static SeqCommandObject createSeqCommandObject(YamlNode yml) {
		try {
			SeqOpCode opCode = SeqOpCode.valueOf(yml.getChildValue("OpCode"));
			int startFrame = yml.getChildIntValue("StartFrame");

			Class<? extends SeqCommandObject> cls = SEQCMDOBJ_CLASSES.get(opCode);

			SeqCommandObject obj = cls.newInstance();
			obj.startFrame = startFrame;
			obj.setOpCode(opCode);

			for (Field field : cls.getFields()) {
				int mods = field.getModifiers();
				if (!Modifier.isStatic(mods) && !Modifier.isTransient(mods)) {
					String name = YamlReflectUtil.getYamlFieldName(field);
					YamlNode source = yml.getChildByNameIgnoreCase(name);

					if (source != null) {
						Class fieldType = field.getType();

						if (fieldType.isEnum()) {
							Object[] enumValues = fieldType.getEnumConstants();
							String str = source.getValue();
							for (Object ev : enumValues) {
								if (((Enum) ev).toString().equals(str)) {
									field.set(obj, ev);
									break;
								}
							}
						} else if (fieldType == Integer.TYPE) {
							field.set(obj, source.getValueInt());
						} else if (fieldType == Float.TYPE) {
							field.set(obj, source.getValueFloat());
						} else {
							throw new UnsupportedOperationException("Non-deserializable field type!");
						}
					}
				}
			}
			return obj;
		} catch (InstantiationException | IllegalAccessException ex) {
			Logger.getLogger(SeqCommandObjectFactory.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static SeqCmd createWritableCommand(SeqCommandObject obj) {
		try {
			SeqCmd cmd = new SeqCmd(obj.getOpCode(), obj.startFrame);

			int userDataIndex = 0;
			for (Field field : obj.getClass().getFields()) {
				int mods = field.getModifiers();
				if (!Modifier.isStatic(mods) && !Modifier.isTransient(mods)) {
					Class fieldType = field.getType();
					int value = 0;

					if (fieldType.isEnum()) {
						value = ((Enum) field.get(obj)).ordinal();
					} else if (fieldType == Integer.TYPE) {
						value = field.getInt(obj);
					} else if (fieldType == Float.TYPE) {
						value = (int) (field.getFloat(obj) * 4096f);
					} else {
						throw new UnsupportedOperationException("Non-deserializable field type!");
					}

					cmd.userData[userDataIndex] = value;
					userDataIndex++;
				}
			}
			return cmd;
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			Logger.getLogger(SeqCommandObjectFactory.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static YamlNode createYmlNode(SeqCommandObject obj) {
		YamlNode n = new YamlNode(new YamlListElement());

		n.addChild("StartFrame", obj.startFrame);
		n.addChild("OpCode", obj.getOpCode());

		YamlReflectUtil.addFieldsToNode(n, obj);

		return n;
	}
}
