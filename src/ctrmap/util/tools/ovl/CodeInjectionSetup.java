package ctrmap.util.tools.ovl;

public class CodeInjectionSetup {
	public final int overlayId;
	public final int overlayBaseAddress;
	public final int overlaySize;
	
	public CodeInjectionSetup(int overlayId, int baseAddr, int size) {
		this.overlayId = overlayId;
		this.overlayBaseAddress = baseAddr;
		this.overlaySize = size;
	}
}
