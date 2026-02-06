package ctrmap.util.tools.swan;

import java.util.List;
import xstandard.util.ListenableList;
import java.util.Objects;
import xstandard.formats.yaml.YamlNodeName;

public class SwanSourceFile {

	@YamlNodeName("Path")
	public String path;
	@YamlNodeName("Includes")
	public List<String> includesLegacy;
	@YamlNodeName("IncludesNew")
	public ListenableList<SwanInclude> includes = new ListenableList<>();
	@YamlNodeName("Typedefs")
	public ListenableList<SwanTypedef> typedefs = new ListenableList<>();
	@YamlNodeName("GVars")
	public ListenableList<SwanTypedef> gvars = new ListenableList<>();
	@YamlNodeName("Functions")
	public ListenableList<SwanTypedef> functions = new ListenableList<>();
	@YamlNodeName("Enums")
	public ListenableList<SwanEnum> enums = new ListenableList<>();
	@YamlNodeName("Structures")
	public ListenableList<SwanStructure> structures = new ListenableList<>();
	@YamlNodeName("Macros")
	public String macros;

	public SwanSourceFile() {

	}

	public SwanSourceFile(String path) {
		this.path = path;
	}

	public SwanTypedef getFuncByName(String name) {
		for (SwanTypedef td : functions) {
			if (Objects.equals(td.getTypeName(), name)) {
				return td;
			}
		}
		return null;
	}

	public SwanInclude getIncludeByFileName(String fileName) {
		for (SwanInclude inc : includes) {
			if (Objects.equals(inc.fileName, fileName)) {
				return inc;
			}
		}
		return null;
	}

	void pullLegacyIncludes() {
		if (includesLegacy != null) {
			for (String include : includesLegacy) {
				includes.add(new SwanInclude(include));
			}
			includesLegacy.clear();
		}
	}

	@Override
	public String toString() {
		return path;
	}
}
