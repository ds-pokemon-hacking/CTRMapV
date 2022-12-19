package ctrmap.formats.pokemon.gen5.mapmatrix;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.util.ResizeableMatrix;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class VMapMatrix {

	private FSFile source;

	public boolean hasZones;
	public ResizeableMatrix<Integer> chunkIds;
	public ResizeableMatrix<Integer> zoneIds;

	public VMapMatrix() {
		hasZones = false;
		chunkIds = new ResizeableMatrix<>(1, 1, -1);
		zoneIds = new ResizeableMatrix<>(1, 1, -1);
	}
	
	public VMapMatrix(FSFile f) {
		try {
			source = f;

			DataInStream dis = f.getDataInputStream();

			hasZones = dis.readInt() == 1;
			int width = dis.readUnsignedShort();
			int height = dis.readUnsignedShort();
			chunkIds = new ResizeableMatrix(width, height, -1);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					chunkIds.set(j, i, dis.readInt());
				}
			}

			if (hasZones) {
				zoneIds = new ResizeableMatrix<>(width, height, -1);
				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						zoneIds.set(j, i, dis.readInt());
					}
				}
			}

			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(VMapMatrix.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public Point getFirstMatrixPoint(int zoneID){
		int smallestX = getWidth();
		int smallestY = getHeight();
		for (int x = 0; x < getWidth(); x++){
			for (int y = 0; y < getHeight(); y++){
				if (chunkIds.get(x, y) != -1 && (!hasZones || zoneIds.get(x, y) == zoneID)){
					smallestX = Math.min(x, smallestX);
					smallestY = Math.min(y, smallestY);
				}
			}
		}
		return new Point(smallestX, smallestY);
	}
	
	public Point getLastMatrixPoint(int zoneID){
		int largestX = 0;
		int largestY = 0;
		for (int x = 0; x < getWidth(); x++){
			for (int y = 0; y < getHeight(); y++){
				if (chunkIds.get(x, y) != -1 && (!hasZones || zoneIds.get(x, y) == zoneID)){
					largestX = Math.max(x, largestX);
					largestY = Math.max(y, largestY);
				}
			}
		}
		return new Point(largestX, largestY);
	}
	
	public Dimension getActiveDimensions(int zoneID){
		Point f = getFirstMatrixPoint(zoneID);
		Point l = getLastMatrixPoint(zoneID);
		return new Dimension(l.x - f.x + 1, l.y - f.y + 1);
	}
	
	public int getWidth(){
		return chunkIds.getWidth();
	}
	
	public int getHeight(){
		return chunkIds.getHeight();
	}
}
