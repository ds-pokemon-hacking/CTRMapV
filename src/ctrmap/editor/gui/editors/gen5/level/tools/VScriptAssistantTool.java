
package ctrmap.editor.gui.editors.gen5.level.tools;

import ctrmap.editor.gui.editors.common.tools.BaseTool;
import ctrmap.editor.gui.editors.gen5.level.entities.VScriptingAssistant;
import ctrmap.renderer.scene.Scene;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;

public class VScriptAssistantTool extends BaseTool{

	private VScriptingAssistant ast;
	
	public VScriptAssistantTool(VScriptingAssistant ast){
		this.ast = ast;
	}
	
	@Override
	public AbstractToolbarEditor getEditor() {
		return ast;
	}

	@Override
	public JComponent getGUI() {
		return ast;
	}

	@Override
	public String getFriendlyName() {
		return "Scripting Assistant";
	}

	@Override
	public String getResGroup() {
		return "ScrA";
	}

	@Override
	public Scene getG3DEx() {
		return null;
	}

	@Override
	public void onViewportSwitch(boolean isOrtho) {
	}
}
