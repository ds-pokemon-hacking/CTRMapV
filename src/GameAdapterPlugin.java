
import ctrmap.editor.gui.editors.common.IGameAdapter;
import ctrmap.editor.system.juliet.GameAdapterRegistry;
import ctrmap.editor.system.juliet.IGameAdapterPlugin;
import ctrmap.formats.common.GameInfo;
import ctrmap.missioncontrol_base.IMissionControl;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.VMcConfig;

public class GameAdapterPlugin implements IGameAdapterPlugin {

	@Override
	public void attach(GameAdapterRegistry reg) {
		System.out.println("Gen V game support loaded.");
	}

	@Override
	public Class<? extends IMissionControl> getEngineClass() {
		return VLaunchpad.class;
	}

	@Override
	public IGameAdapter createGameAdapter() {
		return new VLaunchpadGameAdapter();
	}

	public static class VLaunchpadGameAdapter implements IGameAdapter<VLaunchpad> {

		public final VMcConfig config = new VMcConfig();
		private final VLaunchpad mc = new VLaunchpad();

		@Override
		public void startup() {
			mc.start(config);
		}

		@Override
		public String getName() {
			return "Pokémon B/W/B2/W2";
		}

		@Override
		public Class<VLaunchpad> getEngineClass() {
			return VLaunchpad.class;
		}

		@Override
		public VLaunchpad getMC() {
			return mc;
		}

		@Override
		public boolean supportsGame(GameInfo game) {
			return game.isGenV();
		}
	}
}
