package ctrmap.missioncontrol_ntr;

import ctrmap.formats.common.GameInfo;

public class VGameConstant {
	
	private final int valueBW;
	private final int valueBW2;
	
	public VGameConstant(int valueBW, int valueBW2){
		this.valueBW = valueBW;
		this.valueBW2 = valueBW2;
	}	
	
	public int get(GameInfo game){
		return game.isBW2() ? valueBW2 : valueBW;
	}
}
