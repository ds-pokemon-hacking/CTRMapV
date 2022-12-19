package ctrmap.util.tools.swan;

import xstandard.formats.yaml.YamlNodeName;

public class SwanEnum {

	@YamlNodeName("Definition")
	public SwanTypedef definition;
	@YamlNodeName("DefineFlagOps")
	public boolean defineFlagOps;

	@Override
	public String toString() {
		return definition.toString();
	}
}
