package ctrmap.formats.pokemon.gen5.zone.entities;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import xstandard.util.ReflectionHash;
import xstandard.util.ReflectionHashIgnore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VZoneInitScriptDispatcher {

	public static final int ENTRY_SIZEOF = 6;

	public final ReflectionHash hash;
	
	@ReflectionHashIgnore
	private transient FSFile source;

	public DynamicInitScript dynScr;

	public List<StaticInitScript> staticScripts = new ArrayList<>();

	public VZoneInitScriptDispatcher(FSFile fsf) {
		this.source = fsf;
		try {
			DataIOStream in = fsf.getDataIOStream();
			read(in);
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(VZoneInitScriptDispatcher.class.getName()).log(Level.SEVERE, null, ex);
		}
		hash = new ReflectionHash(this);
	}

	public VZoneInitScriptDispatcher(DataIOStream in) throws IOException {
		read(in);
		hash = new ReflectionHash(this);
	}

	public void setFrom(VZoneInitScriptDispatcher other) {
		dynScr = new DynamicInitScript(other.dynScr);
		staticScripts.clear();
		for (StaticInitScript s : other.staticScripts) {
			staticScripts.add(new StaticInitScript(s));
		}
	}

	public void write(DataIOStream out) throws IOException {
		for (StaticInitScript s : staticScripts) {
			if (s.valid()) {
				out.writeShorts(s.tag, s.SCRID);
				out.writeShort(0);
			}
		}
		TemporaryOffset ofsDynBranches = null;
		if (dynScr != null && dynScr.valid()) {
			out.writeShort(dynScr.tag);
			ofsDynBranches = new TemporaryOffset(out, -Integer.BYTES);
		}
		//terminate primary entry stream -- only the tag is read, so we don't need to pad the whole entry
		out.writePadding(Short.BYTES, 0);
		if (ofsDynBranches != null) {
			ofsDynBranches.setHereSelfRelative();
			for (WkCmpEntry e : dynScr.branches) {
				if (e.wkId != 0) {
					out.writeShorts(e.wkId, e.refVal, e.SCRID);
				}
			}
			//terminate wklist entry stream
			out.writePadding(Short.BYTES, 0);
		}
		out.writePadding(Short.BYTES, 0); //unnecessary, but akin to original scripts
	}

	public void writeToSourceFile() {
		if (source != null) {
			try (DataIOStream out = source.getDataIOStream()) {
				out.setLength(0);
				write(out);
			} catch (IOException ex) {
				Logger.getLogger(VZoneInitScriptDispatcher.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private void read(DataIOStream in) throws IOException {
		int max = in.getLength();
		Reader:
		while (in.getPosition() + ENTRY_SIZEOF <= max) {
			int tag = in.readUnsignedShort();
			switch (tag) {
				case 0:
					break Reader;
				case 1:
					dynScr = new DynamicInitScript();
					dynScr.tag = tag;
					int offsBranches = in.readInt();
					if (offsBranches != 0) {
						in.checkpoint();
						in.skipBytes(offsBranches);

						while (in.getPosition() + ENTRY_SIZEOF <= max) {
							int wkId = in.readUnsignedShort();
							if (wkId != 0) {
								WkCmpEntry e = new WkCmpEntry();
								e.wkId = wkId;
								e.refVal = in.readUnsignedShort();
								e.SCRID = in.readUnsignedShort();
								dynScr.branches.add(e);
							} else {
								break;
							}
						}
						in.resetCheckpoint();
					}
					break;
				default:
					StaticInitScript staticScr = new StaticInitScript();
					staticScr.tag = tag;
					staticScr.SCRID = in.readUnsignedShort();
					in.readUnsignedShort(); //padding
					staticScripts.add(staticScr);
					break;
			}
		}
	}

	public static class StaticInitScript {

		public int tag;
		public int SCRID;

		public StaticInitScript() {

		}

		public StaticInitScript(StaticInitScript s) {
			tag = s.tag;
			SCRID = s.SCRID;
		}

		public boolean valid() {
			return tag != 0;
		}
	}

	public static class DynamicInitScript {

		public int tag = 1;
		public List<WkCmpEntry> branches = new ArrayList<>();

		public DynamicInitScript() {

		}

		public DynamicInitScript(DynamicInitScript s) {
			tag = s.tag;
			for (WkCmpEntry e : s.branches) {
				branches.add(new WkCmpEntry(e));
			}
		}

		public boolean valid() {
			return tag != 0;
		}
	}

	public static class WkCmpEntry {

		public int wkId;
		public int refVal;
		public int SCRID;

		public WkCmpEntry() {

		}

		public WkCmpEntry(WkCmpEntry e) {
			wkId = e.wkId;
			refVal = e.refVal;
			SCRID = e.SCRID;
		}
	}
}
