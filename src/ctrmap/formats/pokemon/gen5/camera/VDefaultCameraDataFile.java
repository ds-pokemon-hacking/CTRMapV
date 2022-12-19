
package ctrmap.formats.pokemon.gen5.camera;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VDefaultCameraDataFile {
	
	public List<VDefaultCameraData> entries = new ArrayList<>();
	
	public VDefaultCameraDataFile(FSFile fsf){
		try {
			DataIOStream io = fsf.getDataIOStream();

			int count = io.getLength() / VDefaultCameraData.BYTES;
			
			for (int i = 0; i < count; i++){
				entries.add(new VDefaultCameraData(io));
			}
			
			io.close();
		} catch (IOException ex) {
			Logger.getLogger(VDefaultCameraDataFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
