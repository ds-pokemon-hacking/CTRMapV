package ctrmap.util.tools.ovl;

import java.io.IOException;
import rpm.format.rpm.RPM;
import xstandard.io.base.iface.DataOutputEx;
import xstandard.io.base.impl.ext.data.DataIOStream;

/**
 *
 */
public class OvlCodeEntry {

	public String name;
	public int offset;
	public int length;

	public boolean readOnly = false;

	private RPM rpm;
	private RPM codeOnlyRPM;
	private RPM symOnlyRPM;

	public boolean updated = false;

	public OvlCodeEntry(DataIOStream in) throws IOException {
		name = in.readString();
		offset = in.readInt();
		length = in.readInt();
		int flags = in.readInt();
		readOnly = (flags & 1) != 0;
		int pos = in.getPosition();
		try {
			rpm = new RPM(in, offset, offset + length);
			makeSplitRPMs();
		} catch (Exception ex) {
			throw new RuntimeException("Error while reading " + name, ex);
		}
		if (rpm.getCodeStream() == null) {
			throw new RuntimeException("Error while reading (code stream failed to load) " + name);
		}
		in.seek(pos);
	}

	public OvlCodeEntry(RPM rpm) {
		this.rpm = rpm;
		updated = true;
		makeSplitRPMs();
	}
	
	private void makeSplitRPMs() {
		codeOnlyRPM = RPM.createPartialHandle(rpm, true, false, true);
		symOnlyRPM = RPM.createPartialHandle(rpm, false, true, true);
	}
	
	public void setBaseAddrNoUpdateBytes(int base) {
		rpm.setBaseAddrNoUpdateBytes(base);
		codeOnlyRPM.setBaseAddrNoUpdateBytes(base);
		symOnlyRPM.setBaseAddrNoUpdateBytes(base);
	}

	public RPM getFullRPM() {
		return rpm;
	}
	
	public RPM getSymbolRPM(RPMSplitManager splitMng) {
		if (splitMng != null) {
			return symOnlyRPM;
		}
		return rpm;
	}

	public RPM getCodeRPM(RPMSplitManager splitMng) {
		if (splitMng != null) {
			return codeOnlyRPM;
		}
		return rpm;
	}
	
	public void write(DataOutputEx out) throws IOException {
		out.writeString(name);
		out.writeInt(offset);
		out.writeInt(length);
		out.writeInt(readOnly ? 1 : 0);
	}

	public int getEndOffset() {
		return offset + length;
	}

	public int getStructSize() {
		return name.length() + 1 + 3 * Integer.BYTES;
	}
}
