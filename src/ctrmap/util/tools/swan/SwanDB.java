
package ctrmap.util.tools.swan;

import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.util.ListenableList;
import java.util.Objects;
import xstandard.formats.yaml.YamlNodeName;

public class SwanDB {
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Tchaikovsky Database", "*.swandb");
	
	private transient Yaml yml;
	
	@YamlNodeName("SourceFiles")
	public ListenableList<SwanSourceFile> sourceFiles = new ListenableList<>();
	
	public SwanDB() {
		yml = new Yaml();
	}
	
	public Yaml getYaml() {
		return yml;
	}
	
	public SwanDB(FSFile fsf) {
		yml = new Yaml(fsf);
		YamlReflectUtil.deserializeToObject(yml.root, this);
	}
	
	public void sortSrcFiles() {
		sourceFiles.sort((o1, o2) -> {
			return o1.path.compareTo(o2.path);
		});
		ListenableList<SwanSourceFile> copy = new ListenableList<>(sourceFiles);
		sourceFiles.clear();
		sourceFiles.addAll(copy); //re-add because sorting with Java messed up listeners
	}
	
	public SwanSourceFile findFileByPath(String path) {
		for (SwanSourceFile f : sourceFiles) {
			if (Objects.equals(f.path, path)) {
				return f;
			}
		}
		return null;
	}
	
	public void save() {
		yml.root.removeAllChildren();
		YamlReflectUtil.addFieldsToNode(yml.root, this);
		yml.write();
	}
}
