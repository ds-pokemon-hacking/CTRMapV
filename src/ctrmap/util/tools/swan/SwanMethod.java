package ctrmap.util.tools.swan;

import xstandard.formats.yaml.YamlNodeName;
import xstandard.text.StringEx;

public class SwanMethod {

	@YamlNodeName("CPPName")
	public String cppName;
	@YamlNodeName("CName")
	public String cName;
	@YamlNodeName("IsStatic")
	public boolean isStatic;
	@YamlNodeName("ParamSwizzle")
	public String paramSwizzle;

	public int[] getParamSwizzleIndices() {
		if (paramSwizzle == null) {
			return null;
		}
		String[] swizzleParams = StringEx.splitOnecharFastNoBlank(paramSwizzle, '/');
		int[] out = new int[swizzleParams.length];
		for (int i = 0; i < swizzleParams.length; i++) {
			try {
				out[i] = Integer.parseInt(swizzleParams[i]);
			} catch (NumberFormatException ex) {
				out[i] = -1;
			}
		}
		return out;
	}

	public String[] getSwizzleParamExtra(int[] indices) {
		if (indices == null) {
			return null;
		}
		String[] swizzleParams = StringEx.splitOnecharFastNoBlank(paramSwizzle, '/');
		for (int i = 0; i < swizzleParams.length; i++) {
			if (indices[i] != -1) {
				swizzleParams[i] = null;
			}
		}
		return swizzleParams;
	}
	
	@Override
	public String toString() {
		return cppName;
	}
}
