package ctrmap.editor.gui.editors.gen5.text;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.text.loaders.AbstractTextLoader;
import ctrmap.editor.gui.editors.text.loaders.ITextArcType;
import ctrmap.formats.pokemon.text.GFProfanityCheck;
import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.formats.pokemon.text.ITextFile;
import ctrmap.formats.pokemon.text.MessageHandler;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import xstandard.fs.FSFile;

public class VTextLoader extends AbstractTextLoader {

	public VTextLoader(CTRMap cm) {
		super(cm);
	}

	private NTRGameFS getGameFS() {
		return cm.getMissionControl(VLaunchpad.class).fs;
	}

	private NARCRef getNARC(ArcType arcType) {
		switch (arcType) {
			case PROFANITY:
				return NARCRef.COMMON_PROFANITY_CHECK;
			case SCRIPT_MSG:
				return NARCRef.MSGDATA_SCRIPT;
			case SYSTEM_MSG:
				return NARCRef.MSGDATA_SYSTEM;
		}
		return null;
	}

	@Override
	public ITextFile getTextFileData(ITextArcType type, int file) {
		ArcType typeV = (ArcType) type;
		
		FSFile fsFile = getGameFS().NARCGet(getNARC(typeV), file);
		
		switch (typeV) {
			case PROFANITY:
				return new GFProfanityCheck(fsFile, getMsgHandler());
			case SCRIPT_MSG:
			case SYSTEM_MSG:
				return new TextFile(fsFile, getMsgHandler());
		}
		return null;
	}

	@Override
	public int getTextArcMax(ITextArcType type) {
		return getGameFS().NARCGetDataMax(getNARC((ArcType) type));
	}

	@Override
	public MessageHandler getMsgHandler() {
		return GenVMessageHandler.INSTANCE;
	}

	@Override
	public ITextArcType[] getArcTypes() {
		return ArcType.values();
	}

	@Override
	public boolean checkCanExpandTextArc(ITextArcType type) {
		return type != ArcType.PROFANITY;
	}

	@Override
	public boolean isArcTypeNSFW(ITextArcType type) {
		return type == ArcType.PROFANITY;
	}

	public static enum ArcType implements ITextArcType {
		SYSTEM_MSG("Game text", "system"),
		SCRIPT_MSG("Script text", "script"),
		PROFANITY("Bad words list", "badwords");

		private final String name;
		private final String mgrPackage;

		private ArcType(String name, String mgrPackage) {
			this.name = name;
			this.mgrPackage = mgrPackage;
		}

		@Override
		public String friendlyName() {
			return name;
		}

		@Override
		public String getManagerPackageName() {
			return mgrPackage;
		}
	}
}
