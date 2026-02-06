
package ctrmap.util.tools.swan;

import xstandard.formats.yaml.YamlNodeName;

public class SwanInclude {
	
	public static final int PRIORITY_BEFORE_TYPEDEFS = 1;
	public static final int PRIORITY_NORMAL = 0;

	@YamlNodeName("FileName")
	public String fileName;
	@YamlNodeName("Priority")
	public int priority = PRIORITY_NORMAL;

	public SwanInclude() {

	}

	public SwanInclude(String fileName) {
		this.fileName = fileName.trim();
	}

	@Override
	public String toString() {
		return fileName;
	}
}
