
package ctrmap.formats.pokemon.gen5.zone;

import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class VZoneHeader {
	public static int BYTES = 0x30;
	
	public int mapType;
	public int npcInfoCacheIdx;
	
	public int areaID;
	public int matrixID;
	public int scriptsID;
	public int initScriptsID;
	public int textFileID;
	
	public int BGMSpring;
	public int BGMSummer;
	public int BGMAutumn;
	public int BGMWinter;
	
	public int encID;
	public int unknown;
	
	public int entitiesID;
	public int parentZoneID;
	
	public int locNameNo;
	public int locNameDispType;
	
	public int weather;
	public int actorProjMatrixType;
	public int cameraIndex;
	
	public boolean enableCycling = true;
	public boolean enableRunning = true;
	public boolean enableEscapeRope = true;
	public boolean enableFlyFrom = true;
	public boolean enableCyclingBGM = true;
	public boolean enableEntralinkWarp = false;
	
	public int mapTransitionEffect;
	public int battleBG;
	
	public int matrixCamBoundaryIndex = -1;
	
	public int nameIcon;
	public int diffLevelAdjustment;
	
	public int flyX;
	public int flyY;
	public int flyZ;
	
	public VZoneHeader() {
		
	}
	
	public VZoneHeader(VZoneHeader otherHdr) {
		mapType = otherHdr.mapType;
		npcInfoCacheIdx = otherHdr.npcInfoCacheIdx;
		areaID = otherHdr.areaID;
		matrixID = otherHdr.matrixID;
		textFileID = otherHdr.textFileID;
		scriptsID = otherHdr.scriptsID;
		initScriptsID = otherHdr.initScriptsID;
		BGMAutumn = otherHdr.BGMAutumn;
		BGMSpring = otherHdr.BGMSpring;
		BGMWinter = otherHdr.BGMWinter;
		BGMSummer = otherHdr.BGMSummer;
		encID = otherHdr.encID;
		unknown = otherHdr.unknown;
		actorProjMatrixType = otherHdr.actorProjMatrixType;
		enableEntralinkWarp = otherHdr.enableEntralinkWarp;
		entitiesID = otherHdr.entitiesID;
		parentZoneID = otherHdr.parentZoneID;
		locNameNo = otherHdr.locNameNo;
		locNameDispType = otherHdr.locNameDispType;
		weather = otherHdr.weather;
		cameraIndex = otherHdr.cameraIndex;
		mapTransitionEffect = otherHdr.mapTransitionEffect;
		battleBG = otherHdr.battleBG;
		matrixCamBoundaryIndex = otherHdr.matrixCamBoundaryIndex;
		nameIcon = otherHdr.nameIcon;
		diffLevelAdjustment = otherHdr.diffLevelAdjustment;
		enableCycling = otherHdr.enableCycling;
		enableEscapeRope = otherHdr.enableEscapeRope;
		enableCyclingBGM = otherHdr.enableCyclingBGM;
		enableFlyFrom = otherHdr.enableFlyFrom;
		enableRunning = otherHdr.enableRunning;
		flyX = otherHdr.flyX;
		flyY = otherHdr.flyY;
		flyZ = otherHdr.flyZ;
	}
	
	public VZoneHeader(DataInput in) throws IOException {
		mapType = in.readUnsignedByte();
		npcInfoCacheIdx = in.readUnsignedByte();
		
		areaID = in.readUnsignedShort();
		matrixID = in.readUnsignedShort();
		scriptsID = in.readUnsignedShort();
		initScriptsID = in.readUnsignedShort();
		textFileID = in.readUnsignedShort();
		
		BGMSpring = in.readUnsignedShort();
		BGMSummer = in.readUnsignedShort();
		BGMAutumn = in.readUnsignedShort();
		BGMWinter = in.readUnsignedShort();
		
		encID = in.readUnsignedShort(); //the rest of the bits is unused
		unknown = (encID >> 13) & 7;
		encID &= 0x1FFF;
		encID = (encID << 19) >> 19;
		
		entitiesID = in.readUnsignedShort();
		parentZoneID = in.readUnsignedShort();
		
		locNameNo = in.readUnsignedShort();
		locNameDispType = locNameNo >> 10 & 0b111111;
		locNameNo &= 0x3FF;
		
		int envFlags = in.readUnsignedShort();
		weather = envFlags & 0x3F;
		actorProjMatrixType = (envFlags >> 6) & 7;
		cameraIndex = (envFlags >> 9) & 0x7F;
		
		int flagsAndBattleBG = in.readUnsignedShort();
		mapTransitionEffect = flagsAndBattleBG & 0x1F;
		battleBG = (flagsAndBattleBG >> 5) & 0x1F;
		enableCycling = ((flagsAndBattleBG >> 10) & 1) == 1;
		enableRunning = ((flagsAndBattleBG >> 11) & 1) == 1;
		enableEscapeRope = ((flagsAndBattleBG >> 12) & 1) == 1;
		enableFlyFrom = ((flagsAndBattleBG >> 13) & 1) == 1;
		enableCyclingBGM = ((flagsAndBattleBG >> 14) & 1) == 1;
		enableEntralinkWarp = ((flagsAndBattleBG >> 15) & 1) == 1;
		
		matrixCamBoundaryIndex = in.readShort();
		nameIcon = in.readUnsignedShort();
		diffLevelAdjustment = nameIcon >> 13 & 7;
		nameIcon &= 0x1FFF;
		
		flyX = in.readInt();
		flyY = in.readInt();
		flyZ = in.readInt();
	}
	
	public byte[] getBytes(){
		try {
			DataIOStream dos = new DataIOStream();
			dos.write(mapType);
			dos.write(npcInfoCacheIdx);
			dos.writeShort(areaID);
			dos.writeShort(matrixID);
			dos.writeShort(scriptsID);
			dos.writeShort(initScriptsID);
			dos.writeShort(textFileID);
			dos.writeShort(BGMSpring);
			dos.writeShort(BGMSummer);
			dos.writeShort(BGMAutumn);
			dos.writeShort(BGMWinter);
			dos.writeShort(encID | (unknown << 13));
			dos.writeShort(entitiesID);
			dos.writeShort(parentZoneID);
			dos.writeShort(locNameNo | (locNameDispType << 10));
			dos.writeShort(weather | (actorProjMatrixType << 6) | (cameraIndex << 9));
			
			int flagsAndBattleBG = mapTransitionEffect;
			flagsAndBattleBG |= (battleBG << 5);
			flagsAndBattleBG = (flagsAndBattleBG | ((enableCycling) ? 1 : 0) << 10);
			flagsAndBattleBG = (flagsAndBattleBG | ((enableRunning) ? 1 : 0) << 11);
			flagsAndBattleBG = (flagsAndBattleBG | ((enableEscapeRope) ? 1 : 0) << 12);
			flagsAndBattleBG = (flagsAndBattleBG | ((enableFlyFrom) ? 1 : 0) << 13);
			flagsAndBattleBG = (flagsAndBattleBG | ((enableCyclingBGM) ? 1 : 0) << 14);
			flagsAndBattleBG = (flagsAndBattleBG | ((enableEntralinkWarp) ? 1 : 0) << 15);
			dos.writeShort(flagsAndBattleBG);
			dos.writeShort(matrixCamBoundaryIndex);
			dos.writeShort(nameIcon | (diffLevelAdjustment << 13));
			
			dos.writeInt(flyX);
			dos.writeInt(flyY);
			dos.writeInt(flyZ);
			dos.close();
			return dos.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(VZoneHeader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
