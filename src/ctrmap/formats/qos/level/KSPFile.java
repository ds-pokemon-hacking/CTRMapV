package ctrmap.formats.qos.level;

import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.DecimalType;
import xstandard.io.serialization.ReferenceType;
import xstandard.io.serialization.annotations.Inline;
import xstandard.io.serialization.annotations.LengthPos;
import xstandard.io.serialization.annotations.MagicStr;
import xstandard.io.util.IndentedPrintStream;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KSPFile {

	public static final String KSP_MAGIC = "KSP\u0001";

	@MagicStr(KSP_MAGIC)
	public String magic;
	@LengthPos(LengthPos.LengthPosType.BEFORE_PTR)
	public List<KSPBoundingSphere> boundingSpheres;
	public KDTTree tree;

	public KSPFile(FSFile fsf) {
		try {
			BinaryDeserializer deserializer = new BinaryDeserializer(fsf.getIO(), ByteOrder.LITTLE_ENDIAN, ReferenceType.ABSOLUTE_POINTER, DecimalType.FIXED_POINT_NNFX);
			deserializer.deserializeToObject(this);
			deserializer.baseStream.close();
		} catch (IOException ex) {
			Logger.getLogger(KSPFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void dump(FSFile dest) {
		IndentedPrintStream out = new IndentedPrintStream(dest.getNativeOutputStream());
		out.println("KSP:");
		out.incrementIndentLevel();

		out.println("Bounds:");
		out.incrementIndentLevel();
		int index = 0;
		for (KSPBoundingSphere r : boundingSpheres) {
			out.println(index + ": " + r.pos + " [" + r.radius + "]");
			index++;
		}
		out.decrementIndentLevel();

		out.println("Tree:");
		out.incrementIndentLevel();
		out.println("Bounds: " + tree.minVector + " / " + tree.maxVector);
		index = 0;
		for (KDTTree.KDTTreeNode n : tree.nodes) {
			out.println("Leaf " + index + ":");
			out.incrementIndentLevel();
			if (n.isIndex()) {
				out.println("Indices: [");
				out.incrementIndentLevel();
				List<Integer> indices = tree.getIndicesForNode(n);
				for (int i = 0; i < indices.size(); i++) {
					if (i != 0) {
						out.print(", ");
					}
					if (i % 10 == 0 && i != 0) {
						out.println();
					}
					out.print(indices.get(i));
				}
				out.println();
				out.decrementIndentLevel();
				out.println("]");
			} else {
				out.println("Ref. value: " + n.getRefValue());
				out.println("Lower values leaf: " + n.getFailLeafIndex());
				out.println("Higher values leaf: " + (n.getFailLeafIndex() + 1));
			}
			out.decrementIndentLevel();
			index++;
		}
		out.decrementIndentLevel();

		out.decrementIndentLevel();
		out.close();
	}

	public static void main(String[] args) {
		new KSPFile(
			new DiskFile("D:\\Emugames\\DS\\2934 - 007 - Quantum of Solace (USA) (En,Fr)_Extracted\\data_uncomp\\level_airport_middlefloor_rm02a_visual.ksp")
		)
			.dump(
				new DiskFile("D:\\Emugames\\DS\\2934 - 007 - Quantum of Solace (USA) (En,Fr)_Extracted\\research\\level_airport_middlefloor_rm02a_visual.ksp.yml")
			);
	}

	@Inline
	public static class KSPBoundingSphere {

		@Inline
		public Vec3f pos;
		public float radius;
	}
}
