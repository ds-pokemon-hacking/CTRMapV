
package ctrmap.formats.pokemon.gen5.camera;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import xstandard.util.ListenableList;
import xstandard.util.ReflectionHash;
import xstandard.util.ReflectionHashIgnore;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VCameraDataFile {
	
	public transient final ReflectionHash hash;
	
	@ReflectionHashIgnore
	private transient FSFile file;
	
	public ListenableList<VAbstractCameraData> entries = new ListenableList<>();
	
	public VCameraDataFile(FSFile fsf){
		try {
			file = fsf;
			DataIOStream io = fsf.getDataIOStream();
			
			int count = io.getLength() / VAbstractCameraData.BYTES;
			
			for (int i = 0; i < count; i++){
				io.checkpoint();
				io.seek(i * VCameraDataRect.BYTES + 0x40);
				
				VCameraAreaType type = VCameraAreaType.values()[io.readUnsignedShort()];
				
				io.resetCheckpoint();
				
				entries.add(type == VCameraAreaType.RECTANGLE ? new VCameraDataRect(io) : new VCameraDataCircle(io));
			}
			
			io.close();
			
		} catch (IOException ex) {
			Logger.getLogger(VCameraDataFile.class.getName()).log(Level.SEVERE, null, ex);
		}
		hash = new ReflectionHash(this);
	}
	
	public void write(){
		if (file != null) {
			try {
				DataOutStream out = file.getDataOutputStream();
				
				for (VAbstractCameraData cam : entries){
					cam.write(out);
				}
				
				out.close();
				
				hash.resetChangedFlag();
			} catch (IOException ex) {
				Logger.getLogger(VCameraDataFile.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
