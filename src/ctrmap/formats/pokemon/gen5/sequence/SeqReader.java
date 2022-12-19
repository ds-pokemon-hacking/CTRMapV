
package ctrmap.formats.pokemon.gen5.sequence;

import xstandard.formats.yaml.Yaml;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SeqReader {
	
	public List<Sequence> sequences = new ArrayList<>();
	
	public SeqReader(FSFile binary, int offsetBase, int seqTablePtr, int sequencesCount){
		try {
			DataIOStream io = binary.getDataIOStream();
			
			io.setBase(offsetBase);
			
			io.seek(seqTablePtr);
			
			List<SeqHeader> headers = new ArrayList<>();
			
			for (int i = 0; i < sequencesCount; i++){
				headers.add(new SeqHeader(i, io));
			}
			
			for (SeqHeader hdr : headers){
				if (hdr.paramsPtr == 0){
					sequences.add(null);
				}
				else {
					sequences.add(new Sequence(io, hdr));
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(SeqReader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void dumpRelocatableBinaries(FSFile dir){
		for (Sequence seq : sequences){
			if (seq == null){
				continue;
			}
			FSFile target = dir.getChild(String.format("Sequence%02d.bseq", seq.seqId));
			seq.toRelocatable().writeToFile(target);
		}
	}
	
	public void dumpYML(FSFile dir){
		for (Sequence seq : sequences){
			if (seq == null){
				continue;
			}
			FSFile target = dir.getChild(String.format("Sequence%02d.yml", seq.seqId));
			
			seq.getYML().writeToFile(target);
		}
	}
	
	
	public static void main(String[] args){
		//SeqReader reader = new SeqReader(new DiskFile("D:\\_REWorkspace\\pokescript_genv\\_CUTSCENE_IDB\\overlay9_293"), 0x21A1BA0, 0x21A6ECC, 27);
		//reader.dumpRelocatableBinaries(new DiskFile("D:\\_REWorkspace\\pokescript_genv\\_CUTSCENE_IDB\\export_binary"));
		//reader.dumpYML(new DiskFile("D:\\_REWorkspace\\pokescript_genv\\_CUTSCENE_IDB\\export_yml"));
		DiskFile source = new DiskFile("D:\\_REWorkspace\\pokescript_genv\\_CUTSCENE_IDB\\export_yml");
		DiskFile target = new DiskFile("D:\\Emugames\\DS\\DStools\\NARChive_v1.1\\demo3d_seq_data");
		DiskFile lfsTarget = new DiskFile("D:\\_REWorkspace\\CTRMapProjects\\White2\\vfs\\data\\demo3d_seq_data.narc");
		for (FSFile seq : source.listFiles()){
			System.out.println("Converting sequence " + seq.getName());
			Sequence s = new Sequence(new Yaml(seq));
			//s.toRelocatable().writeToFile(target.getChild(String.format("Sequence%02d.bseq", s.seqId)));
			s.toRelocatable().writeToFile(lfsTarget.getChild(String.valueOf(s.seqId)));
		}
	}
}
