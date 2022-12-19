package ctrmap.util.tools.swan;

import java.util.List;
import xstandard.formats.yaml.YamlNodeName;

public class SwanTypedef {

	@YamlNodeName("CPPName")
	public String cppName;
	@YamlNodeName("Content")
	public String content;

	public SwanTypedef() {

	}

	public SwanTypedef(String content) {
		this.content = content.trim();
	}

	public String getTypeName() {
		//ugly
		int startIdx = 0;
		if (content.startsWith("template")) {
			startIdx = content.indexOf('>');
		}
		if (content.contains("operator ")) {
			int s = content.indexOf("operator ");
			int e = content.indexOf('(', s);
			if (s != -1 && e != -1) {
				return content.substring(s, e);
			}
		}
		int firstBracketIdx = content.indexOf('{', startIdx);
		if (firstBracketIdx == -1) {
			int lastOtherBracketIdx = content.lastIndexOf(')');
			if (lastOtherBracketIdx != -1) {
				int openingBracketIdx = -1;
				int l = 0;
				for (int idx = lastOtherBracketIdx - 1; idx >= 0; idx--) {
					char c = content.charAt(idx);
					if (c == ')') {
						l++;
					} else if (c == '(') {
						l--;
						if (l < 0) {
							openingBracketIdx = idx;
							break;
						}
					}
				}
				if (openingBracketIdx == -1) {
					return null;
				}
				firstBracketIdx = openingBracketIdx;
			}
		} else {
			//handle c++ base classes
			for (int idx = firstBracketIdx - 1; idx >= 0; idx--) {
				char c = content.charAt(idx);
				if (c == ':') {
					if (idx > 0 && content.charAt(idx - 1) != ':' && idx < content.length() - 1 && content.charAt(idx + 1) != ':') {
						//This is a c++ base class notation
						firstBracketIdx = idx - 1;
						break;
					}
				}
			}
		}
		if (firstBracketIdx == -1) {
			firstBracketIdx = content.lastIndexOf(';');
			if (firstBracketIdx == -1) {
				return null;
			}
			int firstArrStartIdx = content.indexOf('[');
			if (firstArrStartIdx != -1) {
				firstBracketIdx = firstArrStartIdx;
			}
		}
		int i = firstBracketIdx;
		int nameEndIdx = -1;
		int nameStartIdx = -1;
		while (i >= 0) {
			if (nameEndIdx == -1) {
				if (isNameChar(content.charAt(i))) {
					nameEndIdx = i + 1;
				}
			} else {
				if (nameStartIdx == -1) {
					if (!isNameChar(content.charAt(i))) {
						nameStartIdx = i + 1;
						break;
					}
				}
			}
			i--;
		}
		if (nameStartIdx != -1 && nameEndIdx != -1) {
			return content.substring(nameStartIdx, nameEndIdx);
		}
		return null;
	}

	public String prettyPrint(int firstIdx, boolean sanitize) {
		String typeName = getTypeName();
		String str = sanitize ? sanitizeCodeBlock(content) : content;
		int index = typeName == null ? -1 : str.indexOf(typeName);
		if (index != -1) {
			StringBuilder sb = new StringBuilder();
			sb.append(str, 0, index);
			if (sanitize && content.contains("INLINE")) {
				firstIdx += "extern ".length();
			}
			while (sb.length() < firstIdx) {
				sb.append(" ");
			}
			sb.append(str, index, str.length());
			return sb.toString();
		}
		return str;
	}

	public static int getPrettyPrintFirstNameIdx(List<SwanTypedef> typedefs, boolean sanitize) {
		int best = 0;
		for (SwanTypedef td : typedefs) {
			String cnt = td.content;
			String tn = td.getTypeName();
			int index = tn == null ? -1 : (sanitize ? sanitizeCodeBlock(cnt) : cnt).indexOf(tn);
			if (index != -1) {
				index = (index + 3) & 0xFFFFFFFC; //align up to 4 spaces
				if (index > best) {
					best = index;
				}
			}
		}
		return best;
	}
	
	public static String sanitizeCodeBlock(String block) {
		block = block.trim(); //trims newlines at end
		block = block.replace("__fastcall ", ""); //GCC does not like this
		block = block.replace(" **", "** ");
		block = block.replace(" *", "* "); //char *stuff to char* stuff
		block = block.replace(" ,", ",");
		block = block.replace(" )", ")");
		return block;
	}

	public static boolean isNameChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == ':';
	}

	@Override
	public String toString() {
		String tn = getTypeName();
		if (tn == null) {
			return "<invalid type>";
		}
		return tn;
	}
}
