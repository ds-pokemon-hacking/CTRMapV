
import ctrmap.editor.system.workspace.IWSFSPlugin;
import ctrmap.formats.ntr.narc.DirectNARC;
import ctrmap.formats.ntr.narc.blocks.NARC;
import java.util.ArrayList;
import java.util.List;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.arc.ArcFile;
import xstandard.fs.accessors.arc.ArcFileMember;
import xstandard.fs.accessors.arc.ArcInput;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;
import xstandard.util.ProgressMonitor;

public class WSFSPlugin implements IWSFSPlugin {

	@Override
	public String getArcFileMagic() {
		return NARC.MAGIC;
	}

	@Override
	public List<? extends FSFile> getArcFiles(ArcFile arc) {
		List<ArcFileMember> l = new ArrayList<>();
		int len = DirectNARC.getDataMax(arc);

		for (int i = 0; i < len; i++) {
			l.add(new ArcFileMember(arc, String.valueOf(i), this));
		}
		return l;
	}

	@Override
	public IOStream getIOForArcMember(ArcFile arc, String path) {
		return DirectNARC.createIO(arc, path);
	}

	@Override
	public ReadableStream getInputStreamForArcMember(ArcFile arc, String path) {
		return DirectNARC.createInputStream(arc, path);
	}

	@Override
	public WriteableStream getOutputStreamForArcMember(ArcFile arc, String path) {
		return DirectNARC.createOutputStream(arc, path);
	}

	@Override
	public int getDataSizeForArcMember(ArcFile arc, String path) {
		return DirectNARC.getUncompDataSize(arc, path);
	}

	@Override
	public void writeToArcFile(ArcFile arc, ProgressMonitor monitor, ArcInput... inputs) {
		DirectNARC.setData(arc, monitor, inputs);
	}

	@Override
	public boolean isArcFile(FSFile f) {
		return DirectNARC.isNARC(f);
	}

	@Override
	public byte[] getUncompFileData(ArcFile arc, String path) {
		return DirectNARC.getData(arc, path);
	}
}
