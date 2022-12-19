
import ctrmap.creativestudio.ngcs.rtldr.NGCSJulietIface;
import ctrmap.editor.gui.editors.gen5.level.Gen5NGCSPlugin;
import rtldr.JRTLDRCore;
import ctrmap.creativestudio.ngcs.rtldr.INGCSPlugin;

public class NGCSPlugin implements INGCSPlugin {
	
	@Override
	public void attach(NGCSJulietIface j) {
		loadSubPlugin(Gen5NGCSPlugin.class);
	}
	
	//will be invoked by creativestudio when needed, we will use this to hook the sub-plugins

	private static void loadSubPlugin(Class<? extends INGCSPlugin> cls) {
		try {
			INGCSPlugin p = cls.newInstance();
			if (p != null) {
				JRTLDRCore.loadExtension(NGCSJulietIface.getInstance(), p);
			}
		} catch (Throwable t) {

		}
	}
}
