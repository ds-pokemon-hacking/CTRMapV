package ctrmap.formats.pokemon.gen5.sequence.rel;

import xstandard.fs.FSFile;
import xstandard.io.InvalidMagicException;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.util.StringIO;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.formats.pokemon.gen5.sequence.SeqCmd;
import ctrmap.formats.pokemon.gen5.sequence.SeqHeader;
import ctrmap.formats.pokemon.gen5.sequence.SeqOpCode;
import ctrmap.formats.pokemon.gen5.sequence.Sequence;
import ctrmap.formats.pokemon.gen5.sequence.commands.SeqCommandObject;
import ctrmap.formats.pokemon.gen5.sequence.commands.SeqCommandObjectFactory;

/**
 *
 */
public class RelocatableSequence extends Sequence {

	public static final String MAGIC = "RlSeqBin";
	public static final String RELOCATION_TABLE_MAGIC = "RelocTbl";

	public static final int RS_PADDING = 0x10;

	private FSFile source;

	public RelocatableSequence(FSFile f) {
		source = f;

		if (f != null) {
			try {
				DataIOStream io = f.getDataIOStream();

				if (!StringIO.checkMagic(io, MAGIC)) {
					throw new InvalidMagicException("Not a RlSeqBin file.");
				}

				int relocationTableOffset = io.readInt();

				int pos = io.getPosition();
				io.seek(relocationTableOffset);
				if (!StringIO.checkMagic(io, RELOCATION_TABLE_MAGIC)) {
					throw new InvalidMagicException("Invalid relocation table magic.");
				}
				io.skipBytes(4);
				StreamRelocator.relocate(io, 0);
				io.seek(pos);

				int sequenceDataOffset = io.readInt();

				io.seek(sequenceDataOffset);
				SeqHeader hdr = new SeqHeader(-1, io);
				read(io, hdr);

				io.close();
			} catch (IOException ex) {
				Logger.getLogger(RelocatableSequence.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public RelocatableSequence(Sequence s) {
		seqId = s.seqId;
		params = s.params;
		playbackCommands = s.playbackCommands;
		endCommands = s.endCommands;
		resources = s.resources;
	}

	public void write() {
		if (source != null) {
			try {
				DataIOStream memoryStream = new DataIOStream();
				RelocatableWriter out = new RelocatableWriter(memoryStream);

				out.writeStringUnterminated(MAGIC);
				TemporaryOffset rtOffset = new TemporaryOffset(out);
				RelocatableWriter.RelocatableOffset seqDataOffset = new RelocatableWriter.RelocatableOffset(out);
			
				out.writeStringUnterminated(SeqHeader.RS_MAGIC);
				seqDataOffset.setHere();
				RelocatableWriter.RelocatableOffset paramsOffset = new RelocatableWriter.RelocatableOffset(out);
				RelocatableWriter.RelocatableOffset cmds1Offset = new RelocatableWriter.RelocatableOffset(out);
				RelocatableWriter.RelocatableOffset cmds2Offset = new RelocatableWriter.RelocatableOffset(out);
				RelocatableWriter.RelocatableOffset rscOffset = new RelocatableWriter.RelocatableOffset(out);
				out.writeInt(resources.resources.size());
				out.pad(RS_PADDING);

				//Params
				out.writeStringUnterminated("PRM0");
				paramsOffset.setHere();
				params.write(out);
				out.pad(RS_PADDING);

				//Commands 1
				out.writeStringUnterminated("CMD1");
				cmds1Offset.setHere();
				writeCmdArray(playbackCommands, out);
				out.pad(RS_PADDING);

				//Commands 2
				out.writeStringUnterminated("CMD2");
				cmds2Offset.setHere();
				writeCmdArray(endCommands, out);
				out.pad(RS_PADDING);
				
				//Resources
				out.writeStringUnterminated("RSC0");
				rscOffset.setHere();
				resources.write(out);
				out.pad(RS_PADDING);

				out.seek(out.getLength());
				out.pad(RS_PADDING);
				rtOffset.setHere();
				out.writeStringUnterminated(RELOCATION_TABLE_MAGIC);
				out.writeInt(0);
				out.writeRelocationTable();
				
				out.pad(0x40);

				out.close();
				source.setBytes(memoryStream.toByteArray());
			} catch (IOException ex) {
				Logger.getLogger(RelocatableSequence.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private static void writeCmdArray(List<SeqCommandObject> cmds, DataOutput out) throws IOException {
		cmds = new ArrayList<>(cmds);
		cmds.sort((SeqCommandObject o1, SeqCommandObject o2) -> {
			if (o1.getOpCode() == SeqOpCode.CMD_END){
				return 1;
			}
			else if (o2.getOpCode() == SeqOpCode.CMD_END){
				return -1;
			}
			return o1.startFrame - o2.startFrame;
		});
		
		SeqCommandObject endCmd = null;
		
		for (SeqCommandObject cmd : cmds) {
			SeqCommandObjectFactory.createWritableCommand(cmd).write(out);
			if (cmd.getOpCode() == SeqOpCode.CMD_END) {
				endCmd = cmd;
				break;
			}
		}
		if (endCmd == null) {
			SeqCmd end = new SeqCmd(SeqOpCode.CMD_END, -1);
			end.write(out);
		}
	}

	public void writeToFile(FSFile fsf) {
		source = fsf;
		write();
	}
}
