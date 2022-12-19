
package ctrmap.missioncontrol_ntr;

import ctrmap.formats.common.GameInfo;
import java.util.HashMap;
import java.util.Map;

public class NTRGameConstant {
	private Map<GameInfo.SubGame, Integer> map = new HashMap<>();
	
	public NTRGameConstant(GameValuePair... values){
		for (GameValuePair p : values){
			map.put(p.game, p.value);
		}
	}
	
	public int get(GameInfo game){
		return map.getOrDefault(game.getSubGame(), -1);
	}
	
	public static class GameValuePair {
		public final GameInfo.SubGame game;
		public final int value;
		
		public GameValuePair(GameInfo.SubGame game, int value){
			this.game = game;
			this.value = value;
		}
	}
}
