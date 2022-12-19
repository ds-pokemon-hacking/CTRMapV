
package ctrmap.util.tools.ovl;

import rpm.format.rpm.RPM;

public interface RPMSplitManager {
	public void removeSymbolRPMForCodeRPM(RPM codeRpm);
	public RPM getSymbolRPMForCodeRPM(RPM codeRpm);
	public void writeSymbolRPM(RPM codeRpm);
}