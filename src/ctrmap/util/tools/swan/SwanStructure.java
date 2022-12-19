
package ctrmap.util.tools.swan;

import xstandard.formats.yaml.YamlNodeName;
import xstandard.util.ListenableList;

public class SwanStructure {
	@YamlNodeName("Definition")
	public SwanTypedef definition;
	
	@YamlNodeName("Methods")
	public ListenableList<SwanMethod> methods = new ListenableList<>();
	
	public boolean isPureCpp() {
		return definition.getTypeName().contains("::");
	}
	
	public boolean isNs() {
		return definition.getTypeName().endsWith("::_NS");
	}
	
	@Override
	public String toString() {
		return definition.toString();
	}
}
