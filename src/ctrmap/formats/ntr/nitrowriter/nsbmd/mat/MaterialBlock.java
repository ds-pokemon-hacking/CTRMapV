package ctrmap.formats.ntr.nitrowriter.nsbmd.mat;

import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DAssociatedNameTree;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSPatriciaTreeWriter;
import ctrmap.renderer.scene.texturing.Texture;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.PointerTable;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.structs.TemporaryOffsetShort;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MaterialBlock {

    public List<NitroMaterialResource> materials = new ArrayList<>();
    
    public NitroMaterialResource findMaterialByNameAndVAlpha(String name, int vertexAlpha){
        for (NitroMaterialResource mat : materials){
            if (mat.name.equals(name) && mat.vertexAlpha == vertexAlpha){
                return mat;
            }
        }
        return null;
    }
    
    public void addMaterial(NitroMaterialResource mat){
        materials.add(mat);
    }
    
    public int getMaterialCount(){
        return materials.size();
    }

    public byte[] getBytes() throws IOException {
        DataIOStream out = new DataIOStream();

        TemporaryOffsetShort textureDictOff = new TemporaryOffsetShort(out);
        TemporaryOffsetShort paletteDictOff = new TemporaryOffsetShort(out);

        //Material dict
        NNSPatriciaTreeWriter.writeNNSPATRICIATree(out, materials, 4);
        //We allocate the ptr table ourselves because we first want to write the text/mat info since their offsets are mere uint16_t's
        List<TemporaryOffset> materialPtrs = PointerTable.allocatePointerTable(materials.size(), out, 0, false);
		NNSPatriciaTreeWriter.writeNNSPATRICIATreeNames(out, materials);

        NNSG3DAssociatedNameTree textureDict = new NNSG3DAssociatedNameTree();
        NNSG3DAssociatedNameTree paletteDict = new NNSG3DAssociatedNameTree();

        for (int i = 0; i < materials.size(); i++) {
            NitroMaterialResource matRsc = materials.get(i);

            Texture tex = matRsc.texture;

            if (tex != null) {
                textureDict.addNameBinding(matRsc.textureName, i);
                paletteDict.addNameBinding(matRsc.paletteName, i);
            }
			else {
				String texSkipReason = "Texture could not be resolved";
				if (matRsc.textureName == null){
					texSkipReason = "Material is untextured";
				}
				System.out.println("Skipping material texture of mat " + matRsc.name + " (Reason: " + texSkipReason + ")");
			}
        }

        textureDictOff.setHere();
        textureDict.write(out, 0);

        paletteDictOff.setHere();
        paletteDict.write(out, 0);

        for (int i = 0; i < materials.size(); i++) {
            materialPtrs.get(i).setHere();
            out.write(materials.get(i).getBytes());
        }

        out.close();
        return out.toByteArray();
    }
}
